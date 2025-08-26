package com.theartofdev.edmodo.cropper;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public class CropImageView extends View {
    public enum Guidelines { OFF, ON }

    public CropImageView(Context c) { super(c); }
    public CropImageView(Context c, AttributeSet a) { super(c, a); }
}
