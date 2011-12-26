package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CalibrateAlarmActivity;
import com.androsz.electricsleepbeta.app.CalibrateForResultActivity;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;
import com.androsz.electricsleepbeta.app.CheckForScreenBugActivity;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;

public class CalibrationWizardActivity extends WizardActivity {

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
					CalibrateAlarmActivity.class), CALIBRATE_REQUEST_CODE);
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
					CheckForScreenBugActivity.class), SCREEN_TEST_REQUEST_CODE);
		}
	}

	public static final int ALARM_CALIBRATION_TIME = 500;

	private final static int CALIBRATE_REQUEST_CODE = 0xDEAD;

	private static AsyncTask<Void, Void, Void> currentTask;
	private final static int SCREEN_TEST_REQUEST_CODE = 0xBEEF;

	private double alarmTriggerCalibration;

	private boolean screenBugPresent;

	@Override
	protected int getWizardLayoutId() {
		return R.layout.wizard_calibration;
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
		if (resultCode == CalibrateForResultActivity.CALIBRATION_FAILED) {
			if (currentTask != null) {
				currentTask.cancel(true);
			} else {
				stopService(new Intent(this, SleepMonitoringService.class));
			}
			return;
		}
		switch (requestCode) {
		case CALIBRATE_REQUEST_CODE:
			alarmTriggerCalibration = data.getDoubleExtra("y", 0);
			stopService(new Intent(this, SleepMonitoringService.class));
			break;
		case SCREEN_TEST_REQUEST_CODE:
			screenBugPresent = data.getAction().equals(
					CheckForScreenBugAccelerometerService.BUG_PRESENT);
			break;
		}
		onLeftButtonClick(null);
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
	protected void onFinishWizardActivity() throws IllegalStateException {
		final SharedPreferences.Editor ed = getSharedPreferences(SettingsActivity.PREFERENCES, 0)
				.edit();
		ed.putFloat(getString(R.string.pref_alarm_trigger_sensitivity),
				(float) alarmTriggerCalibration);
		ed.putBoolean(getString(R.string.pref_force_screen), screenBugPresent);
		ed.commit();

		if (ed.commit()) {
			final SharedPreferences.Editor ed2 = getSharedPreferences(
					SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE).edit();
			ed2.putInt(SettingsActivity.PREFERENCES_ENVIRONMENT,
					getResources().getInteger(R.integer.prefs_version));
			ed2.commit();

			trackEvent("alarm-level", (int) Math.round(alarmTriggerCalibration * 100));
			trackEvent("screen-bug", screenBugPresent ? 1 : 0);
			finish();
		} else {
			trackEvent("calibration-fail", 0);
			throw new IllegalStateException("Calibration failed to write settings...");
		}
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

		alarmTriggerCalibration = savedState.getDouble("alarm");
		screenBugPresent = savedState.getBoolean("screenBug");

		setupNavigationButtons();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putDouble("alarm", alarmTriggerCalibration);
		outState.putBoolean("screenBug", screenBugPresent);
	}

	@Override
	protected boolean onWizardActivity() {
		boolean didActivity = false;
		
		switch (getCurrentWizardIndex()) {
		case R.id.alarmTest:
			currentTask = new AlarmCalibrationTask().execute(null, null, null);
			didActivity = true;
			break;
		case R.id.screenTest:
			currentTask = new ScreenBugCalibrationTask().execute(null, null, null);
			didActivity = true;
			break;
		}
		return didActivity;
	}

	@Override
	protected PagerAdapter getPagerAdapter() {
		// TODO Auto-generated method stub
		return null;
	}
}
