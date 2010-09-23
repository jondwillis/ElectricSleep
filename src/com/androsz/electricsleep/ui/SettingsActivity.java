package com.androsz.electricsleep.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.util.AlarmDatabase;

public class SettingsActivity extends CustomTitlebarPreferenceActivity {

	public static boolean areSensitivitiesValid(int minSensitivity,
			int maxSensitivity, int alarmSensitivity) {
		if (maxSensitivity < 0 || minSensitivity < 0 || alarmSensitivity < 0) {
			return false;
		}

		if (minSensitivity > alarmSensitivity
				|| minSensitivity > maxSensitivity) {
			return false;
		}

		if (alarmSensitivity > maxSensitivity
				|| alarmSensitivity < minSensitivity) {
			return false;
		}

		if (maxSensitivity < alarmSensitivity
				|| maxSensitivity < minSensitivity) {
			return false;
		}

		return true;
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getPreferenceScreen().findPreference(getText(R.string.pref_alarms))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(AlarmDatabase
								.changeAlarmSettings(getPackageManager()));
						// startActivity(new Intent(SettingsActivity.this,
						// AlarmsActivity.class));
						return true;
					}
				});

		getPreferenceScreen().findPreference(getText(R.string.pref_calibration))
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						startActivity(new Intent(SettingsActivity.this, CalibrationWizardActivity.class));
						return true;
					}
				});
	}

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.xml.settings;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		final SharedPreferences.Editor ed = getSharedPreferences(
				getString(R.string.prefs_version), Context.MODE_PRIVATE).edit();
		ed.putInt(getString(R.string.prefs_version),
				getResources().getInteger(R.integer.prefs_version));
		ed.commit();
	}
}
