package com.androsz.electricsleep.ui;

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

public class CalibrationWizardActivity extends CustomTitlebarActivity implements
		OnInitListener {
	private ViewFlipper viewFlipper;

	private int minCalibration;
	private int maxCalibration;
	private int alarmTriggerCalibration;

	private TextToSpeech textToSpeech;
	private boolean ttsAvailable = false;
	private boolean useTTS = false;

	private static AsyncTask<Void, Void, Void> currentTask;

	private static final int TEST_TTS_INSTALLED = 0x1337;

	private static final int MINIMUM_CALIBRATION_TIME = 30000;
	private static final int MAXIMUM_CALIBRATION_TIME = 10000;
	private static final int ALARM_CALIBRATION_TIME = 3000;

	private class DelayedStartMinCalibrationTask extends
			AsyncTask<Void, Void, Void> {

		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CalibrateForResultActivity.class), R.id.minTest);

			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.starting_in));
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.remain_still));
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
			i.putExtra("interval", MINIMUM_CALIBRATION_TIME);
			i.putExtra("min", 0);
			i.putExtra("max", 100);
			startService(i);
		}

		@Override
		protected void onCancelled() {
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
		}
	}

	private class DelayedStartMaxCalibrationTask extends
			AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CalibrateForResultActivity.class), R.id.maxTest);

			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.starting_in));
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.start_moving));
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
			i.putExtra("interval", MAXIMUM_CALIBRATION_TIME);
			i.putExtra("min", minCalibration);
			i.putExtra("max", 100);
			startService(i);
		}

		@Override
		protected void onCancelled() {
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
		}
	}

	private class DelayedStartAlarmCalibrationTask extends
			AsyncTask<Void, Void, Void> {

		protected void onPreExecute() {
			startActivityForResult(new Intent(CalibrationWizardActivity.this,
					CalibrateForResultActivity.class), R.id.alarmTest);

			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.starting_in));
		}

		@Override
		protected Void doInBackground(Void... params) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			notifyUser(CalibrationWizardActivity.this
					.getString(R.string.move_once));
			final Intent i = new Intent(CalibrationWizardActivity.this,
					SleepAccelerometerService.class);
			stopService(i);
			i.putExtra("interval", 2500);
			i.putExtra("min", minCalibration);
			i.putExtra("max", maxCalibration);
			startService(i);

		}
	}

	private boolean doWizardActivity() {
		boolean didActivity = false;
		final int currentChildId = viewFlipper.getCurrentView().getId();
		switch (currentChildId) {
		case R.id.minTest:
			currentTask = new DelayedStartMinCalibrationTask().execute(null,
					null, null);
			didActivity = true;
			break;
		case R.id.maxTest:
			currentTask = new DelayedStartMaxCalibrationTask().execute(null,
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

	private void checkTextToSpeechInstalled() {
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, TEST_TTS_INSTALLED);
	}

	private void notifyUser(String message) {
		notifyUser(message, true);
	}

	private void notifyUser(String message, boolean toast) {
		if (ttsAvailable) {
			textToSpeech.speak(message, TextToSpeech.QUEUE_ADD, null);
		}
		if (toast) {
			Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_calibrate;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
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
			minCalibration *= 2;
			notifyUser(getString(R.string.minimum_sensitivity_set_to) + " "
					+ minCalibration);
			stopService(new Intent(this, SleepAccelerometerService.class));
			break;
		case R.id.maxTest:
			maxCalibration = resultCode;
			float ratioMax = 1-(float) (Math.abs(MAXIMUM_CALIBRATION_TIME
					- MINIMUM_CALIBRATION_TIME))
					/ MINIMUM_CALIBRATION_TIME;
			maxCalibration += (minCalibration * ratioMax);
			notifyUser(getString(R.string.maximum_sensitivity_set_to) + " "
					+ maxCalibration);
			stopService(new Intent(this, SleepAccelerometerService.class));
			break;
		case R.id.alarmTest:
			alarmTriggerCalibration = resultCode;
			float ratioAlarm = 1-(float) (Math.abs(ALARM_CALIBRATION_TIME
					- MINIMUM_CALIBRATION_TIME))
					/ MINIMUM_CALIBRATION_TIME;
			alarmTriggerCalibration += (minCalibration * ratioAlarm);
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
				Intent installIntent = new Intent();
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
	public void onBackPressed() {
		onLeftButtonClick(null);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewFlipper = (ViewFlipper) findViewById(R.id.wizardViewFlipper);
		setupNavigationButtons();
	}

	public void onLeftButtonClick(View v) {

		if (viewFlipper.getDisplayedChild() != 0) {
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_left_in));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_left_out));
			viewFlipper.showPrevious();
			setupNavigationButtons();
		} else {
			super.onBackPressed();
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		viewFlipper.setDisplayedChild(savedState.getInt("child"));

		minCalibration = savedState.getInt("min");
		maxCalibration = savedState.getInt("max");
		alarmTriggerCalibration = savedState.getInt("alarm");

		if (textToSpeech == null) {
			textToSpeech = new TextToSpeech(this, this);
		}

		setupNavigationButtons();
	}

	public void onRightButtonClick(View v) {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_right_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out));

		final int lastChildIndex = viewFlipper.getChildCount() - 1;
		final int displayedChildIndex = viewFlipper.getDisplayedChild();

		if (displayedChildIndex == lastChildIndex) {
			final SharedPreferences.Editor ed = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext()).edit();
			ed.putInt(getString(R.string.pref_minimum_sensitivity),
					minCalibration);
			ed.putInt(getString(R.string.pref_maximum_sensitivity),
					maxCalibration);
			ed.putInt(getString(R.string.pref_alarm_trigger_sensitivity),
					alarmTriggerCalibration);
			ed.commit();

			final SharedPreferences.Editor ed2 = getSharedPreferences(
					getString(R.string.prefs_version), Context.MODE_PRIVATE)
					.edit();
			ed2.putInt(getString(R.string.prefs_version), getResources()
					.getInteger(R.integer.prefs_version));
			ed2.commit();
			ed.commit();
			finish();
		} else {
			if (!doWizardActivity()) {
				viewFlipper.showNext();
				setupNavigationButtons();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("child", viewFlipper.getDisplayedChild());

		outState.putInt("min", minCalibration);
		outState.putInt("max", maxCalibration);
		outState.putInt("alarm", alarmTriggerCalibration);
		outState.putBoolean("usetts", useTTS);
	}

	private void setupNavigationButtons() {
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		final Button rightButton = (Button) findViewById(R.id.rightButton);
		final int lastChildIndex = viewFlipper.getChildCount() - 1;
		final int displayedChildIndex = viewFlipper.getDisplayedChild();
		if (displayedChildIndex == 0) {
			leftButton.setText(R.string.exit);
			rightButton.setText(R.string.next);
			checkTextToSpeechInstalled();
		} else if (displayedChildIndex == lastChildIndex) {
			leftButton.setText(R.string.previous);
			rightButton.setText(R.string.finish);

			final TextView textViewMin = (TextView) findViewById(R.id.minResult);
			textViewMin.setText("" + minCalibration);
			final TextView textViewMax = (TextView) findViewById(R.id.maxResult);
			textViewMax.setText("" + maxCalibration);
			final TextView textViewAlarm = (TextView) findViewById(R.id.alarmResult);
			textViewAlarm.setText("" + alarmTriggerCalibration);

		} else if (displayedChildIndex > 0
				&& displayedChildIndex < lastChildIndex) {
			leftButton.setText(R.string.previous);
			rightButton.setText(R.string.next);
		}
	}

	@Override
	public void onInit(int arg0) {
		if (arg0 == TextToSpeech.SUCCESS) {
			if (textToSpeech.isLanguageAvailable(Locale.ENGLISH) == TextToSpeech.LANG_AVAILABLE) {
				textToSpeech.setLanguage(Locale.US);
				ttsAvailable = true;
				return;
			}
		}
		ttsAvailable = true;
	}
}
