package com.theartofdev.edmodo.cropper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

public class CropImage {
    public static final int PICK_IMAGE_CHOOSER_REQUEST_CODE = 203;
    public static final int CROP_IMAGE_ACTIVITY_REQUEST_CODE = 201;
    public static final int CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204;

    public static CropImageActivityBuilder activity() { return new CropImageActivityBuilder(); }

    public static ActivityResult getActivityResult(Intent data) { return new ActivityResult(data == null ? null : data.getData()); }

    public static class ActivityResult {
        private Uri uri;
        public ActivityResult(Uri u) { this.uri = u; }
        public Uri getUri() { return uri; }
    }

    public static class CropImageActivityBuilder {
        public CropImageActivityBuilder setGuidelines(CropImageView.Guidelines g) { return this; }
        public CropImageActivityBuilder setOutputUri(Uri u) { return this; }
        public CropImageActivityBuilder setFixAspectRatio(boolean f) { return this; }
        public void start(Activity a) { /* noop stub */ }
    }
}
