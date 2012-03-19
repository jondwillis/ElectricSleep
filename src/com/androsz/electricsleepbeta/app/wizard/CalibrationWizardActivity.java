package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.viewpagerindicator.TitleProvider;

public class CalibrationWizardActivity extends WizardActivity {

	public static final int LIGHT_SLEEP_CALIBRATION_INTERVAL = 500;

	public CalibrateLightSleepFragment calibrateLightSleepFragment;

	public CheckForScreenBugFragment checkForScreenBugFragment;

	private static final int FRAG_ABOUT = 0;
	private static final int FRAG_LIGHT_SLEEP_INSTRUCT = 1;
	private static final int FRAG_LIGHT_SLEEP = 2;
	private static final int FRAG_SCREEN_BUG = 3;
	private static final int FRAG_RESULTS = 4;

	private boolean mHasUserChangedCalibration;
	private boolean mHasScreenBugCalibrated;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			mHasUserChangedCalibration =
                savedInstanceState.getBoolean("mHasUserChangedCalibration");
			mHasScreenBugCalibrated = savedInstanceState.getBoolean("mHasScreenBugCalibrated");
		} else {
			mHasUserChangedCalibration = false;
			mHasScreenBugCalibrated = false;
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("mHasUserChangedCalibration", mHasUserChangedCalibration);
        outState.putBoolean("mHasScreenBugCalibrated", mHasScreenBugCalibrated);
    }

	private class WizardPagerAdapter extends FragmentPagerAdapter implements
			TitleProvider {

		public WizardPagerAdapter(FragmentManager manager) {
			super(manager);

			calibrateLightSleepFragment =
                (CalibrateLightSleepFragment) manager.findFragmentByTag(
                    makeFragmentName(FRAG_LIGHT_SLEEP));

			if (calibrateLightSleepFragment == null) {
				calibrateLightSleepFragment = new CalibrateLightSleepFragment();
			}
			calibrateLightSleepFragment.setCalibratorStateListener(
                new CalibratorStateListener() {
                    @Override
                    public void onCalibrationComplete(boolean success) {
                        setForwardNavigationEnabled(success);
                        mHasUserChangedCalibration = true;
                    }
                });

            checkForScreenBugFragment =
                (CheckForScreenBugFragment) manager.findFragmentByTag(
                    makeFragmentName(FRAG_SCREEN_BUG));
            if (checkForScreenBugFragment == null) {
				checkForScreenBugFragment = new CheckForScreenBugFragment();
			}
			checkForScreenBugFragment.setCalibratorStateListener(
                new CalibratorStateListener() {
                    @Override
                    public void onCalibrationComplete(boolean success) {
                        setForwardNavigationEnabled(success);
                        mHasScreenBugCalibrated = true;
                    }
                });
		}

		private String[] titles = new String[] {
            "Calibration", "Placement", "Sensitivity Test", "Standby Test", "Setup Complete"
        };

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
			mWizardPagerAdapter = new WizardPagerAdapter(
					getSupportFragmentManager());
		}
		return mWizardPagerAdapter;
	}

	@Override
	protected void onFinishWizardActivity() throws IllegalStateException {
		final SharedPreferences.Editor ed2 = getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE)
				.edit();
		ed2.putInt(SettingsActivity.PREFERENCES_ENVIRONMENT, getResources()
				.getInteger(R.integer.prefs_version));
		ed2.commit();

		finish();
	}

	protected void setupNavigationButtons(int index) {
		super.setupNavigationButtons(index);
		if (index == FRAG_LIGHT_SLEEP) {
			setForwardNavigationEnabled(mHasUserChangedCalibration);
		} else if (index == FRAG_SCREEN_BUG) {
			setForwardNavigationEnabled(mHasScreenBugCalibrated);
		} else {
			setForwardNavigationEnabled(true);
		}
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

		} else if (index == FRAG_SCREEN_BUG) {
			if (calibrateLightSleepFragment != null) {
				calibrateLightSleepFragment.stopCalibration(this);
			}

			if (checkForScreenBugFragment != null) {
				checkForScreenBugFragment.startCalibration(this);
			}

		} else {
			// not on a wizard page. stop all.
			if (calibrateLightSleepFragment != null) {
				calibrateLightSleepFragment.stopCalibration(this);
			}

			if (checkForScreenBugFragment != null) {
				checkForScreenBugFragment.stopCalibration(this);
			}

		}
	}

	private void setForwardNavigationEnabled(boolean enabled) {
		findViewById(R.id.rightButton).setEnabled(enabled);
		setPagingEnabled(enabled);
	}

	@Override
	protected void onPrepareLastSlide() {
		// TODO Auto-generated method stub

	}

}
