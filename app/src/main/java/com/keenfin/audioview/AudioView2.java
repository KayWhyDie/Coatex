package com.keenfin.audioview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class AudioView2 extends LinearLayout {

    public AudioView2(Context context) {
        super(context);
        init(context, null);
    }

    public AudioView2(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, @Nullable AttributeSet attrs) {
        // Inflate custom layout if provided via attribute
        if (attrs != null) {
            // keep simple: inflate a default layout if needed
            LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, this, true);
        }
    }

    public boolean attached() {
        return true;
    }

    public void setUpControls() {
        // no-op stub
    }

    public void setDataSource(String path) {
        // no-op stub
    }
}
