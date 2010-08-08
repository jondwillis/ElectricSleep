package com.androsz.electricsleep.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.androsz.electricsleep.R;

public class SettingsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		final SharedPreferences.Editor ed = getSharedPreferences(
				getString(R.string.prefs_version), Context.MODE_PRIVATE).edit();
		ed.putInt(getString(R.string.prefs_version), getResources().getInteger(
				R.integer.prefs_version));
		ed.commit();
	}

	private void initializePrefs() {

		final SharedPreferences userPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		final int minSensitivity = userPrefs.getInt(
				getString(R.string.pref_minimum_sensitivity), 0);
		final int maxSensitivity = userPrefs.getInt(
				getString(R.string.pref_maximum_sensitivity), 100);
		final int alarmSensitivity = userPrefs.getInt(
				getString(R.string.pref_alarm_trigger_sensitivity), 30);

		if (!areSensitivitiesValid(minSensitivity, maxSensitivity, alarmSensitivity)) {
			final SharedPreferences.Editor ed = userPrefs.edit();
			ed.putInt(getString(R.string.pref_minimum_sensitivity), 0);
			ed.putInt(getString(R.string.pref_maximum_sensitivity), 100);
			ed.putInt(getString(R.string.pref_alarm_trigger_sensitivity), 30);
			ed.commit();
		}
	}

	public static boolean areSensitivitiesValid(int minSensitivity,
			int maxSensitivity, int alarmSensitivity) {
		if (maxSensitivity < 0 || minSensitivity < 0 || alarmSensitivity < 0) {
			return false;
		}
		
		if(minSensitivity > alarmSensitivity || minSensitivity > maxSensitivity)
		{
			return false;
		}
		
		if(alarmSensitivity > maxSensitivity || alarmSensitivity < minSensitivity)
		{
			return false;
		}
		
		if(maxSensitivity < alarmSensitivity || maxSensitivity < minSensitivity)
		{
			return false;
		}
		
		return true;
	}
}
