package com.ivor.kriptex;

import android.util.Log;

import androidx.multidex.MultiDexApplication;

import com.ivor.kriptex.tor.FileServer;
import com.ivor.kriptex.tor.Tor;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

import cn.dreamtobe.filedownloader.OkHttp3Connection;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmSchema;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

public class MainApp extends MultiDexApplication {

    private static final String TAG = "MainApp";

    @Override
    public void onCreate() {
        super.onCreate();

        Realm.init(this);

        // The RealmConfiguration is created using the builder pattern.
        // The Realm file will be located in Context.getFilesDir() with name "myrealm.realm"
        RealmConfiguration config = new RealmConfiguration.Builder()
                .name("myrealm.realm")
                .schemaVersion(2)
                .migration((realm, oldVersion, newVersion) -> {

                    Log.d(TAG, "onCreate: Old Version: " + oldVersion + " New Version: " + newVersion);

                    if (oldVersion == 0 && newVersion == 1) {
                        RealmSchema schema = realm.getSchema();
                        schema.get("Contact")
                                .addField("pubKey", byte[].class);
//                        oldVersion++;
                    }

                    if (oldVersion == 1 && newVersion == 2) {
                        RealmSchema schema = realm.getSchema();
                        Log.d(TAG, "onCreate: changing contact schema and lastOnelineTime");
                        schema.get("Contact")
                                .addField("lastOnlineTime", Long.class);
//                        oldVersion++;
                    }
                })
                .build();

        Realm.setDefaultConfiguration(config);

//        InetSocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", Tor.getSocksPort());
//        Proxy proxyTor = new Proxy(Proxy.Type.SOCKS, proxyAddr);

        InetSocketAddress proxyAddr = new InetSocketAddress("127.0.0.1", Tor.getHttpPort());
        Proxy proxyTor = new Proxy(Proxy.Type.HTTP, proxyAddr);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .proxy(proxyTor)
                .connectTimeout(300, TimeUnit.SECONDS)
                .readTimeout(300, TimeUnit.SECONDS);

        // Init the FileDownloader with the OkHttp3Connection.Creator.
        FileDownloader.setupOnApplicationOnCreate(this)
                .connectionCreator(new OkHttp3Connection.Creator(builder));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void testFileServer() throws IOException {
        FileServer mFileServer = FileServer.getInstance(this, Tor.getFileServerPort(), false);
        mFileServer.start(10000, false);
        Log.d(TAG, "FileServer was stopped, now started again");
    }
}
