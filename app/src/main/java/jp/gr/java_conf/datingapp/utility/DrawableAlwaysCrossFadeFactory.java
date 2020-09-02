package jp.gr.java_conf.datingapp.utility;

import android.graphics.drawable.Drawable;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.request.transition.DrawableCrossFadeTransition;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.request.transition.TransitionFactory;

public class DrawableAlwaysCrossFadeFactory implements TransitionFactory<Drawable> {
    private DrawableCrossFadeTransition fd = new DrawableCrossFadeTransition(300, true);
    @Override
    public Transition<Drawable> build(DataSource dataSource, boolean isFirstResource) {
        return fd;
    }
}
