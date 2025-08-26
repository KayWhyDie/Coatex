package com.novoda.merlin;

import android.content.Context;

public class MerlinsBeard {
    public static class Builder {
        public MerlinsBeard build(Context ctx) { return new MerlinsBeard(); }
    }

    public boolean isConnected() { return true; }
}
