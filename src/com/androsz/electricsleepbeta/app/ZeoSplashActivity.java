package com.androsz.electricsleepbeta.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBar;
import android.view.View;
import android.widget.CheckBox;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.wizard.WelcomeTutorialWizardActivity;

public class ZeoSplashActivity extends HostActivity {

    @Override
    protected int getContentAreaLayoutId() {
        return R.layout.activity_returning_zeo_splash;
    }

    public void onClick(View v) {
        switch (v.getId()) {
        }
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        final ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(false);

        PreferenceManager.setDefaultValues(ZeoSplashActivity.this,
                R.xml.settings, false);
        final SharedPreferences userPrefs = getSharedPreferences(
                SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE);
        final int prefsVersion = userPrefs.getInt(
                SettingsActivity.PREFERENCES_ENVIRONMENT, 0);
        if (prefsVersion == 0) {
            finishAndStartHome();
            startActivity(new Intent(ZeoSplashActivity.this,
                    WelcomeTutorialWizardActivity.class).putExtra("required",
                    true));
        } else if (prefsVersion < getResources().getInteger(
                R.integer.prefs_version_electricsleep_renamed)) {
            // the first time a user who used ElectricSleep before it was
            // renamed revisits the app, this will catch them, display this
            // activity, and then do some maintenance of
            // preferences.
            userPrefs
                    .edit()
                    .remove(SettingsActivity.PREFERENCES_KEY_DONT_SHOW_ZEO)
                    .putInt(SettingsActivity.PREFERENCES_ENVIRONMENT,
                            getResources().getInteger(R.integer.prefs_version))
                    .commit();
        } else {
            finishAndStartHome();
        }
    }

    public void finishAndStartHome() {
        startActivity(new Intent(ZeoSplashActivity.this, HomeActivity.class));
        finish();
    }
}
