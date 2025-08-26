package io.realm;

import java.util.ArrayList;

public class RealmQuery<T> {
    public RealmQuery<T> equalTo(String fieldName, String value) { return this; }
    public RealmQuery<T> equalTo(String fieldName, long value) { return this; }
    public RealmQuery<T> notEqualTo(String fieldName, long value) { return this; }
    public RealmQuery<T> contains(String fieldName, String value, Case c) { return this; }
    public T findFirst() { return null; }
    public RealmResults<T> findAll() { return new RealmResults<>(); }
    public Number max(String fieldName) { return 0; }

    public RealmQuery<T> or() { return this; }
    public RealmQuery<T> and() { return this; }
    public RealmQuery<T> beginGroup() { return this; }
    public RealmQuery<T> endGroup() { return this; }
    public RealmQuery<T> sort(String fieldName, Sort order) { return this; }
    public long count() { return 0; }
}
