package com.androsz.electricsleepbeta.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class ZeoSplashActivity extends HostActivity {

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_zeo_splash;
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_learn_more:
			learnMore(this);
			break;
		case R.id.button_get_started:
			getStarted();
			break;
		}
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		final ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		
		PreferenceManager.setDefaultValues(ZeoSplashActivity.this, R.xml.settings, false);
		final SharedPreferences userPrefs = getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(SettingsActivity.PREFERENCES_ENVIRONMENT, 0);
		if (prefsVersion == 0) {
			finishAndStartHome();
			startActivity(new Intent(ZeoSplashActivity.this, WelcomeTutorialWizardActivity.class)
					.putExtra("required", true));
		} else {
			final boolean dontShowZeoMessage = userPrefs.getBoolean(
					SettingsActivity.PREFERENCES_KEY_DONT_SHOW_ZEO, false);
			if (dontShowZeoMessage) {
				finishAndStartHome();
			}
		}

		// because this layout is also used in Welcome Tutorial, where these are
		// GONE
		findViewById(R.id.button_get_started).setVisibility(View.VISIBLE);
		findViewById(R.id.checkbox_dont_show_again).setVisibility(View.VISIBLE);
	}

	public void getStarted() {
		CheckBox cbxDontShowAgain = (CheckBox) findViewById(R.id.checkbox_dont_show_again);
		final SharedPreferences.Editor ed = getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE).edit();
		ed.putBoolean(SettingsActivity.PREFERENCES_KEY_DONT_SHOW_ZEO, cbxDontShowAgain.isChecked());
		ed.commit();
		
		finishAndStartHome();
	}

	public void finishAndStartHome() {
		startActivity(new Intent(ZeoSplashActivity.this, HomeActivity.class));
		finish();
	}

	public static void learnMore(Context c) {
		c.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.myzeo.com/mobile")));
	}
}
