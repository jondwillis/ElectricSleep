package com.androsz.electricsleepbeta.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.wizard.WelcomeTutorialWizardActivity;

public class ZeoSplashActivity extends HostActivity {

    @Override
    protected int getContentAreaLayoutId() {
        return R.layout.activity_returning_zeo_splash;
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
        final int currentPrefsVersion = getResources().getInteger(
                R.integer.prefs_version);
        Log.d("Current version: " + currentPrefsVersion + ", coming from " + prefsVersion);
        
        if(prefsVersion >= currentPrefsVersion)
        {
         // the user isn't new or returning. skip straight to the home screen.   
            finishAndStartHome();
            return;
        } else if (prefsVersion == 0)
        {
            //the user is new, direct them to the start tutorial
            finishAndStartTutorial(true);
            return;
        } else {
            //the user has just upgraded from a previous version, show the change log
            userPrefs
                    .edit()
                    .remove(SettingsActivity.PREFERENCES_KEY_DONT_SHOW_ZEO)
                    .putInt(SettingsActivity.PREFERENCES_ENVIRONMENT,
                            getResources().getInteger(R.integer.prefs_version))
                    .commit();
        }

        findViewById(R.id.button_continue_to_tutorial).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finishAndStartTutorial(false);
                    }
                });

        findViewById(R.id.button_skip_tutorial).setOnClickListener(
                new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        finishAndStartHome();
                    }
                });
    }

    public void finishAndStartTutorial(boolean required) {
        finishAndStartHome();
        startActivity(new Intent(ZeoSplashActivity.this,
                WelcomeTutorialWizardActivity.class).putExtra("required",
                required));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    public void finishAndStartHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
