package com.novoda.merlin;

import android.content.Context;

public class Merlin {
    public static class Builder {
        public Builder withConnectableCallbacks() { return this; }
        public Builder withDisconnectableCallbacks() { return this; }
        public Builder withAllCallbacks() { return this; }
        public Merlin build(Context ctx) { return new Merlin(); }
    }

    public void bind() { /* noop */ }
    public void unbind() { /* noop */ }
    public void registerConnectable(Runnable r) { /* noop */ }
    public void registerDisconnectable(Runnable r) { /* noop */ }
}
