package com.androsz.electricsleepbeta.app;

import java.lang.reflect.Field;

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

    /** Constant that defines the version code for ICE_CREAM_SANDWICH
     * (reproduced from: android.os.Build.VERSION_CODES)
     * TODO: after switching to API 14 or 15 remove this constant and use the official one instead.
     */
    private static final int ICE_CREAM_SANDWICH = 14;

    /** Constant taken from ICS defined at:
     * android.view.View#SYSTEM_UI_FLAG_LOW_PROFILE
     * (dimmed status bar and navigation)
     * TODO: after switching to API 14 or 15 remove this constant and use the offical one instead.
     */
    private static final int SYSTEM_UI_FLAG_LOW_PROFILE = 1;

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

		getSupportActionBar().setDisplayShowHomeEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		final LinearLayout blackness = new LinearLayout(this);
		blackness.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		if (Build.VERSION.SDK_INT > ICE_CREAM_SANDWICH) {
			blackness.setSystemUiVisibility(SYSTEM_UI_FLAG_LOW_PROFILE);
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
