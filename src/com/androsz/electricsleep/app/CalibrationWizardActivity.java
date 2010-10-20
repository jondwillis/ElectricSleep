package com.androsz.electricsleep.app;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class CalibrationWizardActivity extends CustomTitlebarWizardActivity
		implements OnInitListener {

	private class DelayedStartAlarmCalibrationTask extends
			AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {

			try {
				Thread.sleep(DELAYED_START_TIME);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(final Void result) {
			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.move_once));
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
			i.putExtra("interval", ALARM_CALIBRATION_TIME);
			i.putExtra("min", minCalibration);
			i.putExtra("alarm", SettingsActivity.DEFAULT_MAX_SENSITIVITY);
			startService(i);
		}

		@Override
		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CalibrateForResultActivity.class), R.id.alarmTest);

			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.starting_in));
		}
	}

	private class DelayedStartMinCalibrationTask extends
			AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {

			try {
				Thread.sleep(DELAYED_START_TIME);
			} catch (final InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
		}

		@Override
		protected void onPostExecute(final Void result) {
			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.remain_still));
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
			i.putExtra("interval", MINIMUM_CALIBRATION_TIME);
			i.putExtra("min", SettingsActivity.DEFAULT_MIN_SENSITIVITY);
			i.putExtra("alarm", SettingsActivity.DEFAULT_MAX_SENSITIVITY);
			startService(i);
		}

		@Override
		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CalibrateForResultActivity.class), R.id.minTest);

			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.starting_in));
		}
	}

	private int minCalibration;
	private int alarmTriggerCalibration;

	private TextToSpeech textToSpeech;

	private boolean ttsAvailable = false;

	private boolean useTTS = true;
	private static AsyncTask<Void, Void, Void> currentTask;
	private static final int TEST_TTS_INSTALLED = 0x1337;

	public static final int MINIMUM_CALIBRATION_TIME = 30000;

	private static final int ALARM_CALIBRATION_TIME = 5000;

	private static final int DELAYED_START_TIME = 5000;

	private void checkTextToSpeechInstalled() {
		final Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TEST_TTS_INSTALLED);
	}

	public void onTitleButton1Click(final View v) {
		int messageId = (useTTS = !useTTS) ? R.string.message_tts_on
				: R.string.message_tts_off;

		showTitleButton1(useTTS ? android.R.drawable.ic_lock_silent_mode_off : android.R.drawable.ic_lock_silent_mode);
		notifyUser(getString(messageId));
	}

	@Override
	protected int getWizardLayoutId() {
		return R.layout.wizard_calibration;
	}

	private void notifyUser(final String message) {
		notifyUser(message, true);
	}

	private void notifyUser(final String message, final boolean toast) {
		if (ttsAvailable && useTTS) {
			textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null);
		}
		if (toast) {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onActivityResult(final int requestCode, final int resultCode,
			final Intent data) {
		if (resultCode == CalibrateForResultActivity.CALIBRATION_FAILED) {
			notifyUser(getString(R.string.calibration_failed));
			if (currentTask != null) {
				currentTask.cancel(true);
			} else {
				stopService(new Intent(this, SleepAccelerometerService.class));
			}
			return;
		}
		switch (requestCode) {
		case R.id.minTest:
			minCalibration = resultCode;
			notifyUser(getString(R.string.minimum_sensitivity_set_to) + " "
					+ minCalibration);
			stopService(new Intent(this, SleepAccelerometerService.class));
			break;
		case R.id.alarmTest:
			alarmTriggerCalibration = resultCode;
			final float ratioAlarm = (float) Math.abs(ALARM_CALIBRATION_TIME
					- MINIMUM_CALIBRATION_TIME)
					/ MINIMUM_CALIBRATION_TIME;
			alarmTriggerCalibration += minCalibration / ratioAlarm;
			notifyUser(getString(R.string.alarm_trigger_sensitivity_set_to)
					+ " " + alarmTriggerCalibration);
			stopService(new Intent(this, SleepAccelerometerService.class));
			break;
		case TEST_TTS_INSTALLED:
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				// success, create the TTS instance
				if (textToSpeech == null) {
					textToSpeech = new TextToSpeech(this, this);
				}
			} else {
				// missing data, install it
				final Intent installIntent = new Intent();
				installIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installIntent);
			}
			return;
		}
		viewFlipper.showNext();
		setupNavigationButtons();
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showTitleButton1(android.R.drawable.ic_lock_silent_mode_off);
		checkTextToSpeechInstalled();
	}

	@Override
	public void onInit(final int arg0) {
		if (arg0 == TextToSpeech.SUCCESS) {
			if (textToSpeech.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE) {
				textToSpeech.setLanguage(Locale.US);
				ttsAvailable = true;
				return;
			}
		}
		ttsAvailable = true;
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		viewFlipper.setDisplayedChild(savedState.getInt("child"));

		minCalibration = savedState.getInt("min");
		alarmTriggerCalibration = savedState.getInt("alarm");

		useTTS = savedState.getBoolean("useTTS");

		if (textToSpeech == null) {
			textToSpeech = new TextToSpeech(this, this);
		}

		setupNavigationButtons();
	}

	protected void onPrepareLastSlide() {
		final TextView textViewMin = (TextView) findViewById(R.id.minResult);
		textViewMin.setText("" + minCalibration);
		final TextView textViewAlarm = (TextView) findViewById(R.id.alarmResult);
		textViewAlarm.setText("" + alarmTriggerCalibration);
	}

	protected void onFinishWizardActivity() {
		final SharedPreferences.Editor ed = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext()).edit();
		ed.putInt(getString(R.string.pref_minimum_sensitivity), minCalibration);
		ed.putInt(getString(R.string.pref_alarm_trigger_sensitivity),
				alarmTriggerCalibration);
		ed.commit();

		final SharedPreferences.Editor ed2 = getSharedPreferences(
				getString(R.string.prefs_version), Context.MODE_PRIVATE).edit();
		ed2.putInt(getString(R.string.prefs_version), getResources()
				.getInteger(R.integer.prefs_version));
		ed2.commit();
		ed.commit();
		finish();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("child", viewFlipper.getDisplayedChild());

		outState.putInt("min", minCalibration);
		outState.putInt("alarm", alarmTriggerCalibration);
		outState.putBoolean("useTTS", useTTS);
	}

	@Override
	protected boolean onWizardActivity() {
		boolean didActivity = false;
		final int currentChildId = viewFlipper.getCurrentView().getId();
		switch (currentChildId) {
		case R.id.minTest:
			currentTask = new DelayedStartMinCalibrationTask().execute(null,
					null, null);
			didActivity = true;
			break;
		case R.id.alarmTest:
			currentTask = new DelayedStartAlarmCalibrationTask().execute(null,
					null, null);
			didActivity = true;
			break;
		}
		return didActivity;
	}
}
