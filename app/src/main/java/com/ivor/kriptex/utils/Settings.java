/* Kriptex - Secure P2P Messenger (rebranded) */

package com.ivor.kriptex.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.res.Resources;
import android.util.Log;

import com.ivor.kriptex.R;

public class Settings {

    public static SharedPreferences getPrefs(Context c) {

        c = c.getApplicationContext();

        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(c);

        try {
            PreferenceManager.setDefaultValues(c, R.xml.prefs, false);
        } catch (Resources.NotFoundException e) {
            // Log and continue — avoid crashing the app at startup if the prefs XML cannot be read
            Log.e("Settings", "prefs resource not found", e);
        }

        return p;

    }

    public static void putBoolean(Context context, String key, boolean value) {
        SharedPreferences pref = getPrefs(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static void puString(Context context, String key, String value) {
        SharedPreferences pref = getPrefs(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(key, value);
        editor.commit();
    }

}
