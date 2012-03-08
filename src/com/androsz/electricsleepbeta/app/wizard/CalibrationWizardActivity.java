package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
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

    private WizardPagerAdapter mWizardPagerAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mWizardPagerAdapter = new WizardPagerAdapter(getSupportFragmentManager());
    }

    private boolean isScreenBugPresent;

    // hack-ish but necessary because lockscreens can differ
    public static Intent BUG_PRESENT_INTENT = null;

    private class WizardPagerAdapter extends FragmentPagerAdapter implements
            TitleProvider {

        public WizardPagerAdapter(FragmentManager fm) {
            super(fm);
            calibrateLightSleepFragment = new CalibrateLightSleepFragment();
            checkForScreenBugFragment = new CheckForScreenBugFragment();
        }

        private String[] titles = new String[] { "Start", "Accel 1", "Accel 2",
                "Screen", "Done" };

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

    @Override
    protected PagerAdapter getPagerAdapter() {
        return new WizardPagerAdapter(getSupportFragmentManager());
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
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPerformWizardAction(int index) {
        if (index == FRAG_LIGHT_SLEEP) {
            calibrateLightSleepFragment.startCalibration(this);
            checkForScreenBugFragment.stopCalibration(this);

        } else if (index == FRAG_SCREEN_BUG) {
            checkForScreenBugFragment.startCalibration(this);
            calibrateLightSleepFragment.stopCalibration(this);

        } else {
            calibrateLightSleepFragment.stopCalibration(this);
            checkForScreenBugFragment.stopCalibration(this);

        }
    }
}
