package com.stfalcon.imageviewer;

import android.content.Context;

public class StfalconImageViewer<T> {
    public StfalconImageViewer(Context c, java.util.List<T> data, Object listener) {}
    public void show() {}

    public static class Builder<T> {
        public interface ImageLoader<T> {
            void load(android.widget.ImageView imageView, T model);
        }
        public Builder(Context c, java.util.List<T> data, ImageLoader<T> loader) {}
        public Builder<T> withOverlayView(android.view.View v) { return this; }
        public Builder<T> withStartPosition(int p) { return this; }
        public Builder<T> allowZooming(boolean z) { return this; }
        public Builder<T> withImageChangeListener(java.util.function.Consumer<Integer> c) { return this; }
        public StfalconImageViewer<T> build() { return new StfalconImageViewer<>(null, null, null); }
    }

    public static class With<T> {
    }

    public StfalconImageViewer<T> withOverlayView(android.view.View v) { return this; }

    public void show(boolean animated) { /* ignore arg */ }
}
