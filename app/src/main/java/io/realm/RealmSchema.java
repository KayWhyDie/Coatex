package io.realm;

public class RealmSchema {
	public RealmObjectSchema get(String name) { return new RealmObjectSchema(); }

	public static class RealmObjectSchema {
		public RealmObjectSchema addField(String name, Class<?> type) { return this; }
	}
}
