package com.ivor.kriptex.tor;

import android.content.Context;
import android.os.Build;
import android.util.Base64;
import android.util.Log;

import com.ivor.kriptex.R;
import com.ivor.kriptex.crypto.AdvancedCrypto;
import com.ivor.kriptex.utils.Util;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.digest.DigestUtils;
import org.spongycastle.asn1.ASN1OutputStream;
import org.spongycastle.asn1.x509.RSAPublicKeyStructure;
import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Tor {

    private static String torname = "ctor";
    private static String tordirname = "tordata";
    private static String torservdir = "torserv";
    private static String torCfg = "torcfg";
    private static int HIDDEN_SERVICE_VERSION = 3;
    private static Tor instance = null;
    private Context mContext;
    private static int mSocksPort = 9151;
    private static int mHttpPort = 8191;
    private String mDomain = "";
    private ArrayList<Listener> mListeners;
    private ArrayList<LogListener> mLogListeners;
    private String status = "";
    private boolean mReady = false;

    private File mTorDir;

    private Process mProcessTor;

    private AtomicBoolean mRunning = new AtomicBoolean(false);

    private Thread mTorThread;

    private Tor(Context c) {

        this.mContext = c;

        mListeners = new ArrayList<>();
        mLogListeners = new ArrayList<>();

        mTorDir = new File(c.getFilesDir(), "tor");
        if (!mTorDir.exists()) {
            mTorDir.mkdir();
        }

        // hostname file varies between v2 and v3 hidden services
        File hsDir = getServiceDir();
        String hostname = "";
        if (HIDDEN_SERVICE_VERSION == 3) {
            File v3pub = new File(hsDir, "hs_ed25519_public_key");
            if (v3pub.exists()) {
                hostname = Util.filestr(v3pub).trim();
            }
        }
        if (hostname.length() == 0) {
            // fallback to legacy 'hostname' file
            hostname = Util.filestr(new File(hsDir, "hostname")).trim();
        }
        mDomain = hostname;
        log(mDomain);
    }

    public void kill() {
        if (mProcessTor != null) {
            mProcessTor.destroy();
            try {
                mProcessTor.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mProcessTor = null;
        }
        if (mTorThread != null) {
            mTorThread.interrupt();
            mTorThread = null;
        }
        mRunning.set(false);
    }

    /**
     * start the tor thread
     */
    public void start() {
        if (mRunning.get()) return; // if already running, don't do anything

        Server.getInstance(mContext).setServiceRegistered(false);
        mReady = false;
        mTorThread = new Thread() {
            @Override
            public void run() {
                try {
                    log("kill");
                    Native.killTor();

                    log("install");
                    extractFile(mContext, R.raw.tor, torname);

                    //log("delete on exit");
                    //context.getFileStreamPath(torname).deleteOnExit();

                    log("set executable");
                    mContext.getFileStreamPath(torname).setExecutable(true);

                    log("make dir");
                    File tordir = new File(mTorDir, tordirname);
                    tordir.mkdirs();

                    log("make service");
                    File torsrv = new File(mTorDir, torservdir);
                    torsrv.mkdirs();

                    log("configure");
                    PrintWriter torcfg = new PrintWriter(mContext.openFileOutput(torCfg, Context.MODE_PRIVATE));
                    //torcfg.println("Log debug stdout");
//                    torcfg.println("Log notice stdout");
                    torcfg.println("DataDirectory " + tordir.getAbsolutePath());
                    torcfg.println("SOCKSPort " + mSocksPort);
                    torcfg.println("HTTPTunnelPort " + mHttpPort);
                    torcfg.println("HiddenServiceDir " + torsrv.getAbsolutePath());
                    // Force v3 hidden services for modern Tor builds
                    torcfg.println("HiddenServiceVersion 3");
                    torcfg.println("HiddenServicePort " + getHiddenServicePort() + " " + Server.getInstance(mContext).getSocketName());
                    torcfg.println("HiddenServicePort " + getFileServerPort() + " 127.0.0.1:" + getFileServerPort());
                    torcfg.println();
                    torcfg.close();
                    log(Util.filestr(new File(mContext.getFilesDir(), torCfg)));

                    log("start: " + new File(torname).getAbsolutePath());

                    // Prefer a packaged helper placed under nativeLibraryDir (jniLibs) if available.
                    String nativeLibDir = mContext.getApplicationInfo().nativeLibraryDir;
                    File packagedHelper = new File(nativeLibDir, torname);
                    // Some packaged libraries may be named like libctor.so; check that too.
                    File packagedHelperAlt = new File(nativeLibDir, "lib" + torname + ".so");
                    String execPath;
                    boolean packagedExists = packagedHelper.exists();
                    boolean packagedAltExists = packagedHelperAlt.exists();
                    boolean packagedExec = packagedExists && packagedHelper.canExecute();
                    boolean packagedAltExec = packagedAltExists && packagedHelperAlt.canExecute();
                    log("nativeLibDir=" + nativeLibDir + " packagedExists=" + packagedExists + " packagedExec=" + packagedExec + " packagedAltExists=" + packagedAltExists + " packagedAltExec=" + packagedAltExec);
                    if (packagedExec) {
                        execPath = packagedHelper.getAbsolutePath();
                        log("using packaged helper: " + execPath);
                    } else if (packagedAltExec) {
                        execPath = packagedHelperAlt.getAbsolutePath();
                        log("using packaged helper (alt): " + execPath);
                    } else {
                        // fallback to the extracted file in app files dir
                        File extracted = mContext.getFileStreamPath(torname);
                        try {
                            extracted.setExecutable(true);
                        } catch (Exception e) {
                            // ignore
                        }
                        execPath = extracted.getAbsolutePath();
                        log("using extracted helper: " + execPath);
                    }

                    String[] command = new String[]{
                            execPath,
                            "-f", mContext.getFileStreamPath(torCfg).getAbsolutePath()
                    };

                    StringBuilder sb = new StringBuilder();
                    for (String s : command) {
                        sb.append(s);
                        sb.append(" ");
                    }

                    log("Command: " + sb.toString());

                    mRunning.set(true);

                    mProcessTor = Runtime.getRuntime().exec(command);
                    BufferedReader torReader = new BufferedReader(new InputStreamReader(mProcessTor.getInputStream()));
                    while (true) {
                        if (Thread.interrupted()) {
                            log("Tor thread interrupted");
                            break;
                        }
                        final String line = torReader.readLine();
                        if (line == null) break;
                        log(line);
                        status = line;

                        boolean ready2 = mReady;

                        if (line.contains("100%")) {
                            ls(mTorDir);
                            mDomain = Util.filestr(new File(torsrv, "hostname")).trim();
                            log(mDomain);
                            try {
                                for (Listener l : mListeners) {
                                    if (l != null) l.onChange();
                                }
                            } catch (Exception e) {
                            }
                            ready2 = true;

                            Server.getInstance(mContext).checkServiceRegistered();
                        }
                        mReady = ready2;
                        try {
                            for (LogListener ll : mLogListeners) {
                                if (ll != null) {
                                    ll.onLog();
                                }
                            }
                        } catch (Exception e) {

                        }
                    }
                } catch (Exception ex) {
                    // Log the error and notify listeners so UI can update instead of remaining stuck.
                    ex.printStackTrace();
                    status = "Tor startup failed: " + (ex.getMessage() != null ? ex.getMessage() : ex.toString());
                    log(status);
                    try {
                        for (Listener l : mListeners) {
                            if (l != null) l.onChange();
                        }
                    } catch (Exception e) {
                        // ignore listener exceptions
                    }
                    try {
                        for (LogListener ll : mLogListeners) {
                            if (ll != null) ll.onLog();
                        }
                    } catch (Exception e) {
                        // ignore
                    }
                    // ensure not running
                    mRunning.set(false);
                    // attempt to let Server perform a check in case it wants to recover
                    try {
                        Server.getInstance(mContext).checkServiceRegistered();
                    } catch (Exception e) {
                        // ignore
                    }
                    // do not rethrow; we want the app to continue running and reflect the error
                }
                mRunning.set(false);
                log("Tor thread finished");
            }
        };
        mTorThread.start();
    }

    public static Tor getInstance(Context context) {
        if (instance == null) {
            instance = new Tor(context.getApplicationContext());
        }
        return instance;
    }

    static String computeID(RSAPublicKeySpec pubKey) {
        RSAPublicKeyStructure myKey = new RSAPublicKeyStructure(pubKey.getModulus(), pubKey.getPublicExponent());
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ASN1OutputStream as = new ASN1OutputStream(bs);
        try {
            as.writeObject(myKey.toASN1Object());
        } catch (IOException ex) {
            // TODO: error handling? ignore error?
            throw new Error(ex);
        }
        byte[] b = bs.toByteArray();
        b = DigestUtils.getSha1Digest().digest(b);
        return new Base32().encodeAsString(b).toLowerCase().substring(0, 16);
    }

    public static int getHiddenServicePort() {
        return 31512;
    }

    public static int getFileServerPort() {
        return 8088;
    }

    private void log(String s) {
        Log.d("Tor", "Data: " + s);
    }

    void ls(File f) {
        log(f.toString());
        if (f.isDirectory()) {
            for (File s : f.listFiles()) {
                ls(s);
            }
        }
    }

    public static int getSocksPort() {
        return mSocksPort;
    }

    public static int getHttpPort() {
        return mHttpPort;
    }

    public String getOnion() {
        return mDomain.trim();
    }

    public String getID() {
        return mDomain.replace(".onion", "").trim();
    }

    public void addListener(Listener l) {
        if (l != null && !mListeners.contains(l)) {
            mListeners.add(l);
            l.onChange();
        }
    }

    public void removeListener(Listener l) {
        mListeners.remove(l);
    }

    private void extractFile(Context context, int id, String name) {
        try {
            InputStream i = context.getResources().openRawResource(id);
            OutputStream o = context.openFileOutput(name, Context.MODE_PRIVATE);
            int read;
            byte[] buffer = new byte[4096];
            while ((read = i.read(buffer)) > 0) {
                o.write(buffer, 0, read);
            }
            i.close();
            o.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            //throw new Error(ex);
        }
    }

    public File getServiceDir() {
        return new File(mTorDir, torservdir);
    }

    private KeyFactory getKeyFactory() {
//        if (Security.getProvider("BC") == null) {
        Security.addProvider(new BouncyCastleProvider());
//        }
        try {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                return KeyFactory.getInstance("RSA", "BC");
            } else {
                return KeyFactory.getInstance("RSA");
            }
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public String readPrivateKeyFile() {
        if (HIDDEN_SERVICE_VERSION == 3) {
            File f = new File(getServiceDir(), "hs_ed25519_secret_key");
            if (f.exists()) return Util.filestr(f);
            // some Tor builds may use a different name; fall back
            File alt = new File(getServiceDir(), "private_key");
            return Util.filestr(alt);
        } else {
            return Util.filestr(new File(getServiceDir(), "private_key"));
        }
    }

    public RSAPrivateKey getPrivateKey() {
        String priv = readPrivateKeyFile();
        priv = priv.replace("-----BEGIN RSA PRIVATE KEY-----\n", "");
        priv = priv.replace("-----END RSA PRIVATE KEY-----", "");
        priv = priv.replaceAll("\\s", "");
        byte[] data = Base64.decode(priv, Base64.DEFAULT);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
        try {
            return (RSAPrivateKey) getKeyFactory().generatePrivate(keySpec);
        } catch (InvalidKeySpecException ex) {
            throw new Error(ex);
        }
    }

    private RSAPrivateKeySpec getPrivateKeySpec() {
        try {
            return getKeyFactory().getKeySpec(getPrivateKey(), RSAPrivateKeySpec.class);
        } catch (InvalidKeySpecException ex) {
            throw new Error(ex);
        }
    }

    private RSAPublicKeySpec getPublicKeySpec() {
        return new RSAPublicKeySpec(getPrivateKeySpec().getModulus(), BigInteger.valueOf(65537));
    }

    public RSAPublicKey getPublicKey() {
        try {
            return (RSAPublicKey) getKeyFactory().generatePublic(getPublicKeySpec());
        } catch (InvalidKeySpecException ex) {
            throw new Error(ex);
        }
    }

    private String computeOnion() {
        return computeID(getPublicKeySpec()) + ".onion";
    }

    public byte[] getPubKeySpec() {
        return getPrivateKeySpec().getModulus().toByteArray();
    }

    public byte[] sign(byte[] msg) {
        try {
            Signature signature;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                signature = Signature.getInstance("SHA1withRSA", "BC");
            } else {
                signature = Signature.getInstance("SHA1withRSA");
            }
            signature.initSign(getPrivateKey());
            signature.update(msg);
            return signature.sign();
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    public void stop() {
        kill();
    }

    public String encryptByPublicKey(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchProviderException {
        Cipher encrypt;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        } else {
            encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        }
        encrypt.init(Cipher.ENCRYPT_MODE, getPublicKey());
        return AdvancedCrypto.toHex(encrypt.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public String encryptByPublicKey(String data, byte[] pubKeySpecBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, InvalidKeySpecException, NoSuchProviderException {
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(pubKeySpecBytes), BigInteger.valueOf(65537));
        PublicKey publicKey = getKeyFactory().generatePublic(publicKeySpec);

        Cipher encrypt;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        } else {
            encrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        }
        encrypt.init(Cipher.ENCRYPT_MODE, publicKey);
        return AdvancedCrypto.toHex(encrypt.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }

    public String decryptByPrivateKey(String data) throws NoSuchPaddingException, NoSuchAlgorithmException, BadPaddingException, IllegalBlockSizeException, InvalidKeyException, NoSuchProviderException {
        Cipher decrypt;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
        } else {
            decrypt = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        }
        decrypt.init(Cipher.DECRYPT_MODE, getPrivateKey());
        return new String(decrypt.doFinal(AdvancedCrypto.toByte(data)), StandardCharsets.UTF_8);
    }

    public PublicKey convertKeySpec(byte[] pubkey) {
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(pubkey), BigInteger.valueOf(65537));
        PublicKey publicKey;
        try {
            publicKey = getKeyFactory().generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException ex) {
            ex.printStackTrace();
            return null;
        }
        return publicKey;
    }

    boolean checkSig(String id, byte[] pubkey, byte[] sig, byte[] msg) {
        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(new BigInteger(pubkey), BigInteger.valueOf(65537));

        if (!id.equals(computeID(publicKeySpec))) {
            log("invalid id");
            return false;
        }

        PublicKey publicKey;
        try {
            publicKey = getKeyFactory().generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException ex) {
            ex.printStackTrace();
            return false;
        }

        try {
            Signature signature;
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                signature = Signature.getInstance("SHA1withRSA", "BC");
            } else {
                signature = Signature.getInstance("SHA1withRSA");
            }
            signature.initVerify(publicKey);
            signature.update(msg);
            return signature.verify(sig);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    void test() {
        try {
            String domain = Util.filestr(new File(getServiceDir(), "hostname")).trim();

            log(Util.filestr(new File(getServiceDir(), "hostname")).trim());
            log(computeID(getPublicKeySpec()));
            log(computeOnion());
            log(Util.filestr(new File(getServiceDir(), "hostname")).trim());

            log(Base64.encodeToString(getPubKeySpec(), Base64.DEFAULT));
            log("pub " + Base64.encodeToString(getPubKeySpec(), Base64.DEFAULT));

            byte[] msg = "alkjdalwkdjaw".getBytes();
            log("msg " + Base64.encodeToString(msg, Base64.DEFAULT));

            byte[] sig = sign(msg);
            log("sig " + Base64.encodeToString(sig, Base64.DEFAULT));

            log("chk " + checkSig(getID(), getPubKeySpec(), sig, msg));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void addLogListener(LogListener l) {
        if (!mLogListeners.contains(l)) {
            mLogListeners.add(l);
        }
    }

    public String getStatus() {
        return status;
    }

    public boolean isReady() {
        return mReady;
    }

    public void removeLogListener(LogListener ll) {
        mLogListeners.remove(ll);
    }

    public interface Listener {
        void onChange();
    }

    public interface LogListener {
        void onLog();
    }
}
