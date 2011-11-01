package com.androsz.electricsleepbeta.app;

import java.lang.reflect.Field;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class DimSleepActivity extends AnalyticActivity {

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Window win = getWindow();
		final WindowManager.LayoutParams winParams = win.getAttributes();
		winParams.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		// | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
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

		// win.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		win.setAttributes(winParams);

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		final LinearLayout blackness = new LinearLayout(this);
		blackness.setLayoutParams(new LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));

		blackness.setBackgroundColor(Color.BLACK);
		Toast.makeText(this, "Your screen is in dim mode. To exit, press the back button.",
				Toast.LENGTH_LONG).show();
		setContentView(blackness);
	}

	@Override
	protected void onPause() {
		super.onPause();
		finish(); // if we're going away, really go away.
	}

}
