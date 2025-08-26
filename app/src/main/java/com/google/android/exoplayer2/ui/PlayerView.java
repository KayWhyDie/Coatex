package com.google.android.exoplayer2.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.google.android.exoplayer2.SimpleExoPlayer;

public class PlayerView extends FrameLayout {
    public PlayerView(Context context) { super(context); }
    public PlayerView(Context context, AttributeSet attrs) { super(context, attrs); }
    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); }

    public void setPlayer(SimpleExoPlayer player) { /* noop */ }
    public void resume() { /* noop */ }
    public void pause() { /* noop */ }
}
