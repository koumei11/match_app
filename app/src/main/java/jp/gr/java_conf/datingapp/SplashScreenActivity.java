package jp.gr.java_conf.datingapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        ObjectAnimator fadeOut1 = ObjectAnimator.ofFloat(imageView, "alpha", 1,0.3f);
        ObjectAnimator fadeOut2 = ObjectAnimator.ofFloat(textView, "alpha", 1,0.3f);
        fadeOut1.setDuration(2000);
        fadeOut2.setDuration(2000);
        ObjectAnimator fadeIn1 = ObjectAnimator.ofFloat(imageView, "alpha", 0.3f,1);
        ObjectAnimator fadeIn2 = ObjectAnimator.ofFloat(textView, "alpha", 0.3f,1);
        fadeIn1.setDuration(2000);
        fadeIn2.setDuration(2000);

        final AnimatorSet mAnimatorSet1 = new AnimatorSet();
        final AnimatorSet mAnimatorSet2 = new AnimatorSet();

        mAnimatorSet1.play(fadeIn1).after(fadeOut1);
        mAnimatorSet1.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimatorSet1.start();
            }
        });
        mAnimatorSet1.start();

        mAnimatorSet2.play(fadeIn2).after(fadeOut2);
        mAnimatorSet2.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mAnimatorSet2.start();
            }
        });
        mAnimatorSet2.start();

        new CountDownTimer(4000, 1000) {

            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(SplashScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();
    }
}
