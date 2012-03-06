package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.viewpagerindicator.TitleProvider;

public class CalibrationWizardActivity extends WizardActivity {

	public static final int LIGHT_SLEEP_CALIBRATION_INTERVAL = 500;

	public CalibrateLightSleepFragment calibrateLightSleepFragment;// =
																	// instantiateFragment2(R.layout.wizard_calibration_lightsleep);

	public CheckForScreenBugFragment checkForScreenBugFragment;// =
																// instantiateFragment1(R.layout.wizard_calibration_screenbug);

	double lightSleepTrigger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	private boolean isScreenBugPresent;

	// hack-ish but necessary because lockscreens can differ
	public static Intent BUG_PRESENT_INTENT = null;

	private class WizardPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

		public WizardPagerAdapter(FragmentManager fm) {
			super(fm);
			calibrateLightSleepFragment = new CalibrateLightSleepFragment();
			checkForScreenBugFragment = new CheckForScreenBugFragment();
		}

		private String[] titles = new String[] { "Start", "Test 1",
				"Test 2", "Done" };

		@Override
		public String getTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return new CalibrationAboutFragment();
			case 1:
				return calibrateLightSleepFragment;
			case 2:
				return checkForScreenBugFragment;
			case 3:
				return new CalibrationResultsFragment();

			default:
				throw new IllegalStateException("Could not find the correct fragment.");
			}
		}
	}

	@Override
	protected PagerAdapter getPagerAdapter() {
		return new WizardPagerAdapter(getSupportFragmentManager());
	}

	@Override
	protected void onFinishWizardActivity() throws IllegalStateException {
		final SharedPreferences.Editor ed = getSharedPreferences(SettingsActivity.PREFERENCES, 0)
				.edit();
		ed.putBoolean(getString(R.string.pref_force_screen), isScreenBugPresent);
		ed.commit();

		if (ed.commit()) {
			final SharedPreferences.Editor ed2 = getSharedPreferences(
					SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE).edit();
			ed2.putInt(SettingsActivity.PREFERENCES_ENVIRONMENT,
					getResources().getInteger(R.integer.prefs_version));
			ed2.commit();

			trackEvent("alarm-level", (int) Math.round(lightSleepTrigger * 100));
			trackEvent("screen-bug", isScreenBugPresent ? 1 : 0);
			finish();
		} else {
			trackEvent("calibration-fail", 0);
		}
	}

	@Override
	protected void onPrepareLastSlide() {
		findViewById(R.id.rightButton).setVisibility(View.VISIBLE);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		lightSleepTrigger = savedState.getDouble("alarm");
		isScreenBugPresent = savedState.getBoolean("screenBug");
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putDouble("alarm", lightSleepTrigger);
		outState.putBoolean("screenBug", isScreenBugPresent);
	}

	@Override
	public void onLeftButtonClick(View v) {
		super.onLeftButtonClick(v);
	}

	@Override
	protected void onPerformWizardAction(int index) {
		findViewById(R.id.rightButton).setVisibility(View.INVISIBLE);
		if (index == 1) {
			calibrateLightSleepFragment.startCalibration(this);

			checkForScreenBugFragment.stopCalibration(this);

		} else if (index == 2) {

			checkForScreenBugFragment.startCalibration(this);

			calibrateLightSleepFragment.stopCalibration(this);

		} else {

			calibrateLightSleepFragment.stopCalibration(this);

			checkForScreenBugFragment.stopCalibration(this);

		}
	}
}
