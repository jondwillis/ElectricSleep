package com.androsz.electricsleepbeta.content;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;

public class StartSleepReceiver extends BroadcastReceiver {

	public final static String START_SLEEP = "com.androsz.electricsleepbeta.START_SLEEP";

	public final static String EXTRA_ALARM = "alarm";

	public final static String EXTRA_SENSOR_DELAY = "sensorDelay";
	public final static String EXTRA_USE_ALARM = "useAlarm";
	public final static String EXTRA_ALARM_WINDOW = "alarmWindow";
	public final static String EXTRA_AIRPLANE_MODE = "airplaneMode";
	public final static String EXTRA_SILENT_MODE = "silentMode";
	public final static String EXTRA_FORCE_SCREEN_ON = "forceScreenOn";

	public static void enforceCalibrationBeforeStartingSleep(
			final Context context, final Intent service, final Intent activity) {
		final SharedPreferences userPrefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(
				SettingsActivity.PREFERENCES_ENVIRONMENT, 0);
		String message = "";
		if (prefsVersion == 0) {
			message = context.getString(R.string.message_not_calibrated);
		} else if (prefsVersion != context.getResources().getInteger(
				R.integer.prefs_version)) {
			message = context.getString(R.string.message_prefs_not_compatible);
			context.getSharedPreferences(SettingsActivity.PREFERENCES, 0)
					.edit().clear().commit();
			PreferenceManager.setDefaultValues(context,
					SettingsActivity.PREFERENCES, 0, R.xml.settings, true);
		}

		if (message.length() > 0) {
			message += context
					.getString(R.string.message_recommend_calibration);
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		} else if (service != null && activity != null) {
			context.startService(service);
			context.startActivity(activity);
		}
	}

	@Override
	public void onReceive(final Context context, final Intent intent) {
		new AsyncTask<Void, Void, Void>() {

			Intent serviceIntent;

			@Override
			protected void onPostExecute(Void result) {
				enforceCalibrationBeforeStartingSleep(context, serviceIntent,
						new Intent(context, SleepActivity.class)
								.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			}

			@Override
			protected Void doInBackground(Void... params) {
				final SharedPreferences userPrefs = context
						.getSharedPreferences(SettingsActivity.PREFERENCES, 0);
				final double alarmTriggerSensitivity = userPrefs
						.getFloat(
								context.getString(R.string.pref_alarm_trigger_sensitivity),
								-1);
				final int sensorDelay = Integer.parseInt(userPrefs.getString(
						context.getString(R.string.pref_sensor_delay), ""
								+ SensorManager.SENSOR_DELAY_NORMAL));
				final boolean useAlarm = userPrefs.getBoolean(
						context.getString(R.string.pref_use_alarm), false);
				final int alarmWindow = Integer.parseInt(userPrefs.getString(
						context.getString(R.string.pref_alarm_window), "-1"));
				final boolean airplaneMode = userPrefs.getBoolean(
						context.getString(R.string.pref_airplane_mode), false);
				final boolean silentMode = userPrefs.getBoolean(
						context.getString(R.string.pref_silent_mode), false);
				final boolean forceScreenOn = userPrefs.getBoolean(
						context.getString(R.string.pref_force_screen), false);

				serviceIntent = new Intent(context,
						SleepMonitoringService.class);
				serviceIntent.putExtra(EXTRA_ALARM, alarmTriggerSensitivity);
				serviceIntent.putExtra(EXTRA_SENSOR_DELAY, sensorDelay);
				serviceIntent.putExtra(EXTRA_USE_ALARM, useAlarm);
				serviceIntent.putExtra(EXTRA_ALARM_WINDOW, alarmWindow);
				serviceIntent.putExtra(EXTRA_AIRPLANE_MODE, airplaneMode);
				serviceIntent.putExtra(EXTRA_SILENT_MODE, silentMode);
				serviceIntent.putExtra(EXTRA_FORCE_SCREEN_ON, forceScreenOn);

				return null;
			}

		}.execute();
	}
}
