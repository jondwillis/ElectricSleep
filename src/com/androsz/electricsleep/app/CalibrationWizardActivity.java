package com.androsz.electricsleep.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

import com.androsz.electricsleep.R;

public class CalibrationWizardActivity extends CustomTitlebarWizardActivity {

	private class AlarmCalibrationTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {

			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepMonitoringService.class);
			stopService(i);
			i.putExtra("testModeRate", ALARM_CALIBRATION_TIME);
			i.putExtra("alarm", SettingsActivity.MAX_ALARM_SENSITIVITY);
			startService(i);
		}

		@Override
		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CalibrateAlarmActivity.class), R.id.alarmTest);
		}
	}

	private class ScreenBugCalibrationTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {

			// try {
			// Thread.sleep(10000);
			// } catch (final InterruptedException e) {
			// e.printStackTrace();
			// }
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			// sendBroadcast(new Intent(
			// CheckForScreenBugAccelerometerService.BUG_PRESENT));
		}

		@Override
		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CheckForScreenBugActivity.class), R.id.screenTest);
		}
	}

	private double alarmTriggerCalibration;
	private boolean screenBugPresent;

	private static AsyncTask<Void, Void, Void> currentTask;

	public static final int ALARM_CALIBRATION_TIME = 500;

	@Override
	protected int getWizardLayoutId() {
		return R.layout.wizard_calibration;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent data) {
		if (resultCode == CalibrateForResultActivity.CALIBRATION_FAILED) {
			if (currentTask != null) {
				currentTask.cancel(true);
			} else {
				stopService(new Intent(this, SleepMonitoringService.class));
			}
			return;
		}
		switch (requestCode) {
		case R.id.alarmTest:
			alarmTriggerCalibration = data.getDoubleExtra("y", 0);
			stopService(new Intent(this, SleepMonitoringService.class));
			break;
		case R.id.screenTest:
			screenBugPresent = data.getAction().equals(
					CheckForScreenBugAccelerometerService.BUG_PRESENT);
			break;
		}
		viewFlipper.showNext();
		setupNavigationButtons();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onFinishWizardActivity() {
		final SharedPreferences.Editor ed = getSharedPreferences(
				SettingsActivity.PREFERENCES, 0).edit();
		ed.putFloat(getString(R.string.pref_alarm_trigger_sensitivity),
				(float) alarmTriggerCalibration);
		ed.putBoolean(getString(R.string.pref_force_screen), screenBugPresent);
		ed.commit();

		final SharedPreferences.Editor ed2 = getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE)
				.edit();
		ed2.putInt(SettingsActivity.PREFERENCES_ENVIRONMENT, getResources()
				.getInteger(R.integer.prefs_version));
		ed2.commit();

		trackEvent("alarm-level",
				(int) Math.round(alarmTriggerCalibration * 100));
		trackEvent("screen-bug", screenBugPresent ? 1 : 0);
		finish();
	}

	@Override
	protected void onPrepareLastSlide() {
		final TextView textViewAlarm = (TextView) findViewById(R.id.alarmResult);
		textViewAlarm.setText(String.format("%.2f", alarmTriggerCalibration));
		final TextView textViewScreen = (TextView) findViewById(R.id.screenResult);
		textViewScreen.setText(screenBugPresent + "");
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		viewFlipper.setDisplayedChild(savedState.getInt("child"));

		alarmTriggerCalibration = savedState.getDouble("alarm");
		screenBugPresent = savedState.getBoolean("screenBug");

		setupNavigationButtons();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("child", viewFlipper.getDisplayedChild());

		outState.putDouble("alarm", alarmTriggerCalibration);
		outState.putBoolean("screenBug", screenBugPresent);
	}

	@Override
	protected boolean onWizardActivity() {
		boolean didActivity = false;
		final int currentChildId = viewFlipper.getCurrentView().getId();
		switch (currentChildId) {
		case R.id.alarmTest:
			currentTask = new AlarmCalibrationTask().execute(null, null, null);
			didActivity = true;
			break;
		case R.id.screenTest:
			currentTask = new ScreenBugCalibrationTask().execute(null, null,
					null);
			didActivity = true;
			break;
		}
		return didActivity;
	}
}
