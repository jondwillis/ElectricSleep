package com.androsz.electricsleepbeta.app;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
import com.androsz.electricsleepbeta.app.wizard.CalibrationWizardActivity;
import com.androsz.electricsleepbeta.preference.HostPreferenceActivity;

public class SettingsActivity extends HostPreferenceActivity {

	@Override
	public void onBuildHeaders(List<Header> target) {
		super.onBuildHeaders(target);


		final SharedPreferences serviceIsRunningPrefs = getSharedPreferences(
				SleepMonitoringService.SERVICE_IS_RUNNING, Context.MODE_PRIVATE);
		if (serviceIsRunningPrefs.getBoolean("serviceIsRunning", false)) {
			Toast.makeText(this, R.string.changes_made_to_these_settings,
					Toast.LENGTH_LONG).show();
		}
		
		try {
			// Set the software version shown in the preference header.
			PackageInfo packageInfo = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			for (Header header : target) {
				if (header.getTitle(getResources()).equals(
						getText(R.string.title_version))) {
					header.summary = packageInfo.versionName;
				}
			}
		} catch (NameNotFoundException e) {
			this.trackEvent(
					"Retrieving VersionName failed for SettingsActivity-headers.",
					1);
		}
	}

	private static final int ALARM_STREAM_TYPE_BIT = 1 << AudioManager.STREAM_ALARM;

    /** The default alarm trigger threshold. */
	public static float DEFAULT_ALARM_SENSITIVITY = 0.33F;

	public static float DEFAULT_MIN_SENSITIVITY = 0.0F;

	public static float MAX_ALARM_SENSITIVITY = 1.0F;

	private static final String KEY_ALARM_IN_SILENT_MODE = "alarm_in_silent_mode";

	public static final String KEY_ALARM_SNOOZE = "snooze_duration";

	public static final String KEY_VOLUME_BEHAVIOR = "volume_button_setting";

	// name of the preferences file that holds main preferences
	// this is actually what android uses as default..
	public static String PREFERENCES = "com.androsz.electricsleepbeta_preferences";
	// name of the preferences file that holds environmental preferences
	// example: show the user a donate message once
	public static final String PREFERENCES_ENVIRONMENT = "prefsVersion";
	public static final String PREFERENCES_KEY_DONT_SHOW_ZEO = "dontShowZeoMessage";
	public static final String PREFERENCES_KEY_HISTORY_VIEW_AS_LIST = "historyViewType";

	@Override
	protected int getContentAreaLayoutId() {
		final SharedPreferences serviceIsRunningPrefs = getSharedPreferences(
				SleepMonitoringService.SERVICE_IS_RUNNING, Context.MODE_PRIVATE);
		if (serviceIsRunningPrefs.getBoolean("serviceIsRunning", false)) {
			Toast.makeText(this, R.string.changes_made_to_these_settings,
					Toast.LENGTH_LONG).show();
		}
		return R.xml.settings;
	}

	@Override
	protected int getHeadersResourceId() {
		return R.xml.settings_headers;
	}

	/**
	 * This fragment shows the preferences for the first header.
	 */
	public static class AlarmsPreferenceFragment extends PreferenceFragment
			implements Preference.OnPreferenceChangeListener {
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			// Load the preferences from an XML resource
			addPreferencesFromResource(R.xml.settings_fragment_alarms);
			refresh(this.getActivity());
		}

		private void refresh(Context c) {
			final CheckBoxPreference alarmInSilentModePref = (CheckBoxPreference) findPreference(KEY_ALARM_IN_SILENT_MODE);
			final int silentModeStreams = Settings.System.getInt(
					c.getContentResolver(),
					Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);
			alarmInSilentModePref
					.setChecked((silentModeStreams & ALARM_STREAM_TYPE_BIT) == 0);

			final ListPreference snooze = (ListPreference) findPreference(KEY_ALARM_SNOOZE);
			snooze.setSummary(snooze.getEntry());
			snooze.setOnPreferenceChangeListener(this);
		}

		@Override
		public boolean onPreferenceChange(final Preference pref,
				final Object newValue) {
			final ListPreference listPref = (ListPreference) pref;
			final int idx = listPref.findIndexOfValue((String) newValue);
			listPref.setSummary(listPref.getEntries()[idx]);
			return true;
		}

		@Override
		public boolean onPreferenceTreeClick(
				final PreferenceScreen preferenceScreen,
				final Preference preference) {
			if (KEY_ALARM_IN_SILENT_MODE.equals(preference.getKey())) {
				final CheckBoxPreference pref = (CheckBoxPreference) preference;
				int ringerModeStreamTypes = Settings.System.getInt(
						preferenceScreen.getContext().getContentResolver(),
						Settings.System.MODE_RINGER_STREAMS_AFFECTED, 0);

				if (pref.isChecked()) {
					ringerModeStreamTypes &= ~ALARM_STREAM_TYPE_BIT;
				} else {
					ringerModeStreamTypes |= ALARM_STREAM_TYPE_BIT;
				}

				Settings.System.putInt(preferenceScreen.getContext()
						.getContentResolver(),
						Settings.System.MODE_RINGER_STREAMS_AFFECTED,
						ringerModeStreamTypes);

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

			findPreference(getText(R.string.pref_calibration))
					.setOnPreferenceClickListener(
							new OnPreferenceClickListener() {

								@Override
								public boolean onPreferenceClick(
										final Preference preference) {
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

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getNeedToLoadOldStylePreferences()) {
			try {
				// Set the software version shown in the preference header.
				PackageInfo packageInfo = getPackageManager().getPackageInfo(
						getPackageName(), 0);
				findPreference(getText(R.string.title_version)).setSummary(
						packageInfo.versionName);
			} catch (NameNotFoundException e) {
				this.trackEvent(
						"Retrieving VersionName failed for SettingsActivity-oldstyle.",
						1);
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		new Thread(new Runnable() {

			@Override
			public void run() {
				final SharedPreferences.Editor ed = getSharedPreferences(
						PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE).edit();
				ed.putInt(PREFERENCES_ENVIRONMENT,
						getResources().getInteger(R.integer.prefs_version));
				ed.commit();
			}
		}).start();
	}
}
