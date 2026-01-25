package com.hdaf.eduapp.activities;

import android.animation.ObjectAnimator;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.hdaf.eduapp.R;
import com.hdaf.eduapp.accessibility.TTSManager;
import com.hdaf.eduapp.utils.Constants;

/**
 * Splash screen with animated logo and transition to mode selection.
 */
public class SplashActivity extends AppCompatActivity {

    private ImageView logoImage;
    private TextView appName;
    private TextView tagline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize views
        logoImage = findViewById(R.id.logoImage);
        appName = findViewById(R.id.appName);
        tagline = findViewById(R.id.tagline);

        // Initialize TTS early
        TTSManager.getInstance().initialize(this);

        // Start animations
        startAnimations();

        // Navigate to mode selection after splash duration
        new Handler(Looper.getMainLooper()).postDelayed(this::navigateToModeSelection,
                Constants.SPLASH_DURATION_MS);
    }

    private void startAnimations() {
        // Logo fade in and scale
        ObjectAnimator logoAlpha = ObjectAnimator.ofFloat(logoImage, View.ALPHA, 0f, 1f);
        ObjectAnimator logoScaleX = ObjectAnimator.ofFloat(logoImage, View.SCALE_X, 0.5f, 1f);
        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logoImage, View.SCALE_Y, 0.5f, 1f);

        AnimatorSet logoSet = new AnimatorSet();
        logoSet.playTogether(logoAlpha, logoScaleX, logoScaleY);
        logoSet.setDuration(800);

        // App name fade in
        ObjectAnimator nameAlpha = ObjectAnimator.ofFloat(appName, View.ALPHA, 0f, 1f);
        ObjectAnimator nameTranslate = ObjectAnimator.ofFloat(appName, View.TRANSLATION_Y, 30f, 0f);

        AnimatorSet nameSet = new AnimatorSet();
        nameSet.playTogether(nameAlpha, nameTranslate);
        nameSet.setDuration(600);
        nameSet.setStartDelay(400);

        // Tagline fade in
        ObjectAnimator taglineAlpha = ObjectAnimator.ofFloat(tagline, View.ALPHA, 0f, 1f);
        taglineAlpha.setDuration(500);
        taglineAlpha.setStartDelay(700);

        // Play all animations
        AnimatorSet fullSet = new AnimatorSet();
        fullSet.playTogether(logoSet, nameSet, taglineAlpha);
        fullSet.setInterpolator(new AccelerateDecelerateInterpolator());
        fullSet.start();
    }

    private void navigateToModeSelection() {
        Intent intent = new Intent(this, ModeSelectionActivity.class);
        startActivity(intent);
        finish();
        
        // Smooth transition
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
