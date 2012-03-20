package com.androsz.electricsleepbeta.app;

import java.lang.reflect.Field;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class DimSleepActivity extends AnalyticActivity {
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Window win = getWindow();
		final WindowManager.LayoutParams winParams = win.getAttributes();
		winParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_FULLSCREEN;

		// set screen brightness to the lowest possible without turning screen
		// off
		winParams.screenBrightness = 0.01f;

		// NOTE: this doesn't work on some devices (Motorola Droid, X, 2.. etc)
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			// hack for pre-froyo to set buttonBrightness off
			try {
				final Field buttonBrightness = winParams.getClass().getField("buttonBrightness");
				buttonBrightness.set(winParams, WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF);
			} catch (final Exception e) {
				this.trackEvent("buttonBrightnessFail", 1);
			}
		} else {
			winParams.buttonBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_OFF;
		}

		win.setAttributes(winParams);

		getSupportActionBar().hide();

		final LinearLayout blackness = new LinearLayout(this);
		blackness.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			blackness.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		} else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			blackness.setSystemUiVisibility(View.STATUS_BAR_HIDDEN);
		}

		blackness.setBackgroundColor(Color.BLACK);
		Toast.makeText(this,
				"Your screen is in dim mode. To exit dim mode, touch or press any key.",
				Toast.LENGTH_LONG).show();
		setContentView(blackness);
		try {
			getSupportActionBar().setDisplayShowHomeEnabled(false);
		} catch (NullPointerException npe) {
			getActionBar().setDisplayShowHomeEnabled(false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish(); // if we're going away, really go away.
	}

	@Override
	public void onUserInteraction() {
		// any user interaction ends the activity.
		finish();
	}

}
