package com.androsz.electricsleep.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class CalibrationWizardActivity extends CustomTitlebarActivity {
	private ViewFlipper viewFlipper;

	private int minCalibration;
	private int maxCalibration;
	private int alarmTriggerCalibration;

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_calibrate;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);

		viewFlipper.setDisplayedChild(savedState.getInt("child"));

		minCalibration = savedState.getInt("min");
		maxCalibration = savedState.getInt("max");
		alarmTriggerCalibration = savedState.getInt("alarm");

		setupNavigationButtons();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("child", viewFlipper.getDisplayedChild());

		outState.putInt("min", minCalibration);
		outState.putInt("max", maxCalibration);
		outState.putInt("alarm", alarmTriggerCalibration);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		viewFlipper = (ViewFlipper) findViewById(R.id.wizardViewFlipper);
		setupNavigationButtons();
	}

	private void setupNavigationButtons() {
		Button leftButton = (Button) findViewById(R.id.leftButton);
		Button rightButton = (Button) findViewById(R.id.rightButton);
		int lastChildIndex = viewFlipper.getChildCount() - 1;
		int displayedChildIndex = viewFlipper.getDisplayedChild();
		if (displayedChildIndex == 0) {
			leftButton.setText(R.string.exit);
			rightButton.setText(R.string.next);
		} else if (displayedChildIndex == lastChildIndex) {
			leftButton.setText(R.string.previous);
			rightButton.setText(R.string.finish);
		} else if (displayedChildIndex > 0
				&& displayedChildIndex < lastChildIndex) {
			leftButton.setText(R.string.previous);
			rightButton.setText(R.string.next);
		}
	}

	@Override
	public void onBackPressed() {
		onLeftButtonClick(null);
	}

	public void onLeftButtonClick(View v) {

		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_left_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_left_out));

		if (viewFlipper.getDisplayedChild() != 0) {
			viewFlipper.showPrevious();
		} else {
			super.onBackPressed();
		}

		setupNavigationButtons();
	}

	public void onRightButtonClick(View v) {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_right_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out));

		int lastChildIndex = viewFlipper.getChildCount() - 1;
		int displayedChildIndex = viewFlipper.getDisplayedChild();

		if (displayedChildIndex == lastChildIndex) {
			SharedPreferences.Editor ed = PreferenceManager
					.getDefaultSharedPreferences(getBaseContext()).edit();
			ed.putInt(getString(R.string.pref_minimum_sensitivity),
					this.minCalibration);
			ed.putInt(getString(R.string.pref_maximum_sensitivity),
					this.maxCalibration);
			ed.putInt(getString(R.string.pref_alarm_trigger_sensitivity),
					this.alarmTriggerCalibration);
			ed.commit();
			finish();
		} else {
			if (!doWizardActivity()) {
				viewFlipper.showNext();
				setupNavigationButtons();
			}
		}
	}

	private boolean doWizardActivity() {
		boolean didActivity = false;
		int currentChildId = viewFlipper.getCurrentView().getId();
		Intent i = new Intent(this, SleepAccelerometerService.class);
		switch (currentChildId) {
		case R.id.minTest:
			i.putExtra("interval", 30000);
			i.putExtra("min", 0);
			i.putExtra("max", 100);

			startService(i);
			startActivityForResult(new Intent(this,
					CalibrateForResultActivity.class), R.id.minTest);
			didActivity = true;
			break;
		case R.id.maxTest:
			i.putExtra("interval", 10000);
			i.putExtra("min", minCalibration);
			i.putExtra("max", 100);

			startService(i);
			startActivityForResult(new Intent(this,
					CalibrateForResultActivity.class), R.id.maxTest);
			didActivity = true;
			break;
		case R.id.alarmTest:
			i.putExtra("interval", 10000);
			i.putExtra("min", minCalibration);
			i.putExtra("max", maxCalibration);

			startService(i);
			startActivityForResult(new Intent(this,
					CalibrateForResultActivity.class), R.id.alarmTest);
			didActivity = true;
			break;
		}
		return didActivity;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		stopService(new Intent(this, SleepAccelerometerService.class));
		if (resultCode == -1) {
			Toast.makeText(this, "Calibration failed. Try again.",
					Toast.LENGTH_SHORT).show();
			return;
		}
		switch (requestCode) {
		case R.id.minTest:
			minCalibration = resultCode;
			TextView textViewMin = (TextView) findViewById(R.id.minResult);
			textViewMin.setText("" + minCalibration);
			Toast.makeText(this,
					"Calibration succeeded with result: " + minCalibration,
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.maxTest:
			maxCalibration = resultCode;
			TextView textViewMax = (TextView) findViewById(R.id.maxResult);
			textViewMax.setText("" + maxCalibration);
			Toast.makeText(this,
					"Calibration succeeded with result: " + maxCalibration,
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.alarmTest:
			alarmTriggerCalibration = resultCode;
			TextView textViewAlarm = (TextView) findViewById(R.id.alarmResult);
			textViewAlarm.setText("" + alarmTriggerCalibration);
			Toast.makeText(
					this,
					"Calibration succeeded with result: "
							+ alarmTriggerCalibration, Toast.LENGTH_SHORT)
					.show();
			break;
		default:
			Toast.makeText(this, "No calibration was specified? bug?",
					Toast.LENGTH_SHORT).show();
			return;
		}
		viewFlipper.showNext();
		setupNavigationButtons();
	}

}
