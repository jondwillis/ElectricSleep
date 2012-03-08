package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.viewpagerindicator.TitleProvider;

public class CalibrationWizardActivity extends WizardActivity {

	public static final int LIGHT_SLEEP_CALIBRATION_INTERVAL = 500;

	public CalibrateLightSleepFragment calibrateLightSleepFragment;// =
																	// instantiateFragment2(R.layout.wizard_calibration_lightsleep);

	public CheckForScreenBugFragment checkForScreenBugFragment;// =
																// instantiateFragment1(R.layout.wizard_calibration_screenbug);

	private static final int FRAG_ABOUT = 0;
	private static final int FRAG_LIGHT_SLEEP_INSTRUCT = 1;
	private static final int FRAG_LIGHT_SLEEP = 2;
	private static final int FRAG_SCREEN_BUG = 3;
	private static final int FRAG_RESULTS = 4;

	private boolean isScreenBugPresent;

	// hack-ish but necessary because lockscreens can differ
	public static Intent BUG_PRESENT_INTENT = null;

	private class WizardPagerAdapter extends FragmentPagerAdapter implements
			TitleProvider {

		public WizardPagerAdapter(FragmentManager fm) {
			super(fm);
			calibrateLightSleepFragment = (CalibrateLightSleepFragment) fm
					.findFragmentByTag(makeFragmentName(R.id.wizardPager,
							FRAG_LIGHT_SLEEP));
			if (calibrateLightSleepFragment == null) {
				calibrateLightSleepFragment = new CalibrateLightSleepFragment();
			}
			calibrateLightSleepFragment
					.setCalibratorStateListener(new CalibratorStateListener() {

						@Override
						public void onCalibrationComplete(boolean success) {
							setForwardNavigationEnabled(success);
						}
					});

			checkForScreenBugFragment = (CheckForScreenBugFragment) fm
					.findFragmentByTag(makeFragmentName(R.id.wizardPager,
							FRAG_SCREEN_BUG));
			if (checkForScreenBugFragment == null) {
				checkForScreenBugFragment = new CheckForScreenBugFragment();
			}
			checkForScreenBugFragment
					.setCalibratorStateListener(new CalibratorStateListener() {

						@Override
						public void onCalibrationComplete(boolean success) {
							setForwardNavigationEnabled(success);
							if (success) {
								onRightButtonClick(null);
							}
						}
					});
		}

		private String[] titles = new String[] { "Start", "Accel 1", "Accel 2",
				"Screen", "Done" };

		@Override
		public void setPrimaryItem(ViewGroup container, int position,
				Object object) {
			super.setPrimaryItem(container, position, object);
			Log.d("ES", "setPrimaryItem");
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {

			Log.d("ES", "instantiateItem " + position);
			return super.instantiateItem(container, position);
		}

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
			Log.d("ES", "getItem");
			switch (position) {
			case FRAG_ABOUT:
				return new CalibrationAboutFragment();
			case FRAG_LIGHT_SLEEP_INSTRUCT:
				return new CalibrateLightSleepInstructionsFragment();
			case FRAG_LIGHT_SLEEP:
				return calibrateLightSleepFragment;
			case FRAG_SCREEN_BUG:
				return checkForScreenBugFragment;
			case FRAG_RESULTS:
				return new CalibrationResultsFragment();
			default:
				throw new IllegalStateException(
						"Could not find the correct fragment.");
			}
		}
	}

	private PagerAdapter mWizardPagerAdapter;

	@Override
	protected PagerAdapter getPagerAdapter() {
		// check if we already have a cached copy, create it if not.
		if (mWizardPagerAdapter == null) {
			Log.d("ES", "getPagerAdapter");
			mWizardPagerAdapter = new WizardPagerAdapter(
					getSupportFragmentManager());
		}
		return mWizardPagerAdapter;
	}

	@Override
	protected void onFinishWizardActivity() throws IllegalStateException {
		final SharedPreferences.Editor ed = getSharedPreferences(
				SettingsActivity.PREFERENCES, 0).edit();
		ed.putBoolean(getString(R.string.pref_force_screen), isScreenBugPresent);
		ed.commit();

		if (ed.commit()) {
			final SharedPreferences.Editor ed2 = getSharedPreferences(
					SettingsActivity.PREFERENCES_ENVIRONMENT,
					Context.MODE_PRIVATE).edit();
			ed2.putInt(SettingsActivity.PREFERENCES_ENVIRONMENT, getResources()
					.getInteger(R.integer.prefs_version));
			ed2.commit();

			finish();
		} else {
			trackEvent("calibration-fail", 0);
		}
	}

	@Override
	protected void onPrepareLastSlide() {
	}

	@Override
	public void onLeftButtonClick(View v) {
		super.onLeftButtonClick(v);
	}

	@Override
	protected void onPerformWizardAction(int index) {
		if (index == FRAG_LIGHT_SLEEP) {
			if (calibrateLightSleepFragment != null) {
				calibrateLightSleepFragment.startCalibration(this);
			}

			if (checkForScreenBugFragment != null) {
				checkForScreenBugFragment.stopCalibration(this);
			}

			setForwardNavigationEnabled(false);

		} else if (index == FRAG_SCREEN_BUG) {
			if (calibrateLightSleepFragment != null) {
				calibrateLightSleepFragment.stopCalibration(this);
			}

			if (checkForScreenBugFragment != null) {
				checkForScreenBugFragment.startCalibration(this);
			}

			setForwardNavigationEnabled(false);

		} else {
			// not on a wizard page. stop all.
			if (calibrateLightSleepFragment != null) {
				calibrateLightSleepFragment.stopCalibration(this);
			}

			if (checkForScreenBugFragment != null) {
				checkForScreenBugFragment.stopCalibration(this);
			}

			setForwardNavigationEnabled(true);

		}
	}

	private void setForwardNavigationEnabled(boolean enabled) {
		findViewById(R.id.rightButton).setEnabled(enabled);
		setPagingEnabled(enabled);
	}

}
