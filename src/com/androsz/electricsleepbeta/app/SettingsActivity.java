package com.androsz.electricsleepbeta.app;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.preference.HostPreferenceActivity;

public class SettingsActivity extends HostPreferenceActivity {

	private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;
	public static double DEFAULT_ALARM_SENSITIVITY = 0.33;
	public static double DEFAULT_MIN_SENSITIVITY = 0;

	private static final String KEY_ALARM_IN_SILENT_MODE = "alarm_in_silent_mode";

	public static final String KEY_ALARM_SNOOZE = "snooze_duration";

	public static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";

	public static double MAX_ALARM_SENSITIVITY = 1;
	// name of the preferences file that holds ElectricSleep's main preferences
	// this is actually what android uses as default..
	public static String PREFERENCES = "com.androsz.electricsleepbeta_preferences";
	// name of the preferences file that holds environmental preferences
	// example: show the user a donate message once
	public static final String PREFERENCES_ENVIRONMENT = "prefsVersion";

	@Override
	protected int getContentAreaLayoutId() {
		return R.xml.settings;
	}

	@Override
	protected String getPreferencesName() {
		return PREFERENCES;
	}
	
	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class AlarmsPreferenceFragment extends PreferenceFragment implements
	Preference.OnPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.settings_fragment_alarms);
			refresh(this.getActivity());
		}
		

		private void refresh(Context c) {
			final CheckBoxPreference alarmInSilentModePref = (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
			final int silentModeStreams = Settings.System.getInt(c.getContentResolver(),
					Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
			alarmInSilentModePref.setChecked((silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);

			final ListPreference snooze = (ListPreference) findPreference(KEY_ALARM_SNOOZE);
			snooze.setSummary(snooze.getEntry());
			snooze.setOnPreferenceChangeListener(this);
		}
		
		@Override
		public boolean onPreferenceChange(final Preference pref, final Object newValue) {
			final ListPreference listPref = (ListPreference) pref;
			final int idx = listPref.findIndexOfValue((String) newValue);
			listPref.setSummary(listPref.getEntries()[idx]);
			return true;
		}

		@Override
		public boolean onPreferenceTreeClick(final PreferenceScreen preferenceScreen,
				final Preference preference) {
			if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
				final CheckBoxPreference pref = (CheckBoxPreference) preference;
				int ringerModeStreamTypes = Settings.System.getInt(preferenceScreen.getContext().getContentResolver(),
						Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

				if (pref.isChecked()) {
					ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
				} else {
					ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
				}

				Settings.System.putInt(preferenceScreen.getContext().getContentResolver(),
						Settings.System.MODE_RINGER_STREAMS_AFFECTED, ringerModeStreamTypes);

				return true;
			}

			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
	}

	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class SensorsPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.settings_fragment_sensors);
			
			findPreference(getText(R.string.pref_calibration)).setOnPreferenceClickListener(
					new OnPreferenceClickListener() {

						@Override
						public boolean onPreferenceClick(final Preference preference) {
							startActivity(new Intent(getActivity(),
									CalibrationWizardActivity.class));
							return true;
						}
					});
		}
	}

	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class SleepPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.settings_fragment_sleep);
		}
	}

	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class MiscPreferenceFragment extends PreferenceFragment {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.settings_fragment_misc);
		}
	}

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		super.onBuildHeaders(target);
		loadHeadersFromResource(R.xml.settings_headers, target);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		final SharedPreferences serviceIsRunningPrefs = getSharedPreferences(
				SleepMonitoringService.SERVICE_IS_RUNNING, Context.MODE_PRIVATE);
		if (serviceIsRunningPrefs.getBoolean("serviceIsRunning", false)) {
			Toast.makeText(this, R.string.changes_made_to_these_settings, Toast.LENGTH_LONG).show();
		}
	}
	

	@Override
	protected void onDestroy() {
		super.onDestroy();
		new Thread(new Runnable() {

			@Override
			public void run() {
				final SharedPreferences.Editor ed = getSharedPreferences(PREFERENCES_ENVIRONMENT,
						Context.MODE_PRIVATE).edit();
				ed.putInt(PREFERENCES_ENVIRONMENT,
						getResources().getInteger(R.integer.prefs_version));
				ed.commit();
			}
		}).start();
	}
}
