package com.aditya.filebrowser;

import android.app.Activity;
import android.content.Intent;

public class FileChooser {
    public static void chooseFile(Activity activity, int requestCode) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(i, requestCode);
    }
}
