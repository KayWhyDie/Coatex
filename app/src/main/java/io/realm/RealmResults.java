package io.realm;

import java.util.ArrayList;

public class RealmResults<E> extends ArrayList<E> implements OrderedRealmCollection<E> {
    // lightweight stub to satisfy usage in adapters

    public interface ChangeListener<T> {
        void onChange(RealmResults<T> results, Object changeSet);
    }

    public void addChangeListener(ChangeListener<E> listener) { /* noop */ }

    public void removeAllChangeListeners() { /* noop */ }

    public RealmResults<E> where() { return this; }
    public RealmResults<E> equalTo(String field, Object value) { return this; }
    public RealmResults<E> or() { return this; }
    public RealmResults<E> findAll() { return this; }
    public E findFirst() { return this.isEmpty() ? null : this.get(0); }

    public RealmResults<E> sort(String fieldName, Sort sort) { return this; }

    public void deleteAllFromRealm() { this.clear(); }

    public RealmResults<E> contains(String field, String value, Object caseObj) { return this; }
}


