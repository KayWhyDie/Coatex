package io.realm;

public class RealmConfiguration {
    public static class Builder {
        private String name;
        private long schemaVersion;

        public Builder name(String name) { this.name = name; return this; }

        public Builder schemaVersion(long v) { this.schemaVersion = v; return this; }

        public Builder migration(MigrationCallback cb) { /* ignore migration in stub */ return this; }

        public RealmConfiguration build() { return new RealmConfiguration(); }
    }

    public interface MigrationCallback {
        void migrate(Realm realm, long oldVersion, long newVersion);
    }
}
