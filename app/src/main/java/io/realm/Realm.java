package io.realm;

import android.content.Context;

public class Realm {
    public static Realm getDefaultInstance() { return new Realm(); }

    public static void init(Context ctx) { /* noop stub for init */ }

    public static void setDefaultConfiguration(RealmConfiguration config) { /* noop stub */ }

    public void close() {}

    public void beginTransaction() { /* noop stub */ }

    public void commitTransaction() { /* noop stub */ }

    public <T> T createObject(Class<T> clazz, String primaryKey) { return null; }

    public <T> T createObject(Class<T> clazz, long primaryKey) { return null; }

    public <T> T copyToRealm(T object) { return object; }

    public <T> T copyFromRealm(T object) { return object; }

    public <T> RealmQuery<T> where(Class<T> clazz) { return new RealmQuery<>(); }
    public RealmSchema getSchema() { return new RealmSchema(); }
}
