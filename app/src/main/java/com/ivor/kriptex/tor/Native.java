/* Kriptex - Secure P2P Messenger (rebranded) */

package com.ivor.kriptex.tor;

public class Native {

    static {
        System.loadLibrary("app");
    }

    native public static void killTor();

}
