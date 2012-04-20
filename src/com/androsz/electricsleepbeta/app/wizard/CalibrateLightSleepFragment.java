package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.widget.DecimalSeekBar;
import com.androsz.electricsleepbeta.widget.SleepChart;
import com.androsz.electricsleepbeta.widget.VerticalSeekBar;

public class CalibrateLightSleepFragment extends Calibrator {

	private float mAlarmTrigger;

	private VerticalSeekBar mSeekBar;
	private View mWarmingUp;

	SleepChart mSleepChart;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity a = getActivity();

		if (savedInstanceState != null) {
			mAlarmTrigger = savedInstanceState.getFloat("mAlarmTrigger");
		} else {
			mAlarmTrigger = a.getSharedPreferences(
					SettingsActivity.PREFERENCES, 0).getFloat(
					a.getString(R.string.pref_alarm_trigger_sensitivity),
					SettingsActivity.DEFAULT_ALARM_SENSITIVITY);
		}

		a.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private void resetViews(final Activity a) {
		mSleepChart = (SleepChart) a.findViewById(R.id.calibration_sleep_chart);
		mSleepChart.clear();
		mSeekBar = (VerticalSeekBar) a
				.findViewById(R.id.calibration_level_seekbar);

		mWarmingUp = a.findViewById(R.id.warming_up_text);

		mSleepChart.setVisibility(View.INVISIBLE);
		mSeekBar.setVisibility(View.INVISIBLE);

		mSleepChart.setCalibrationLevelAndRedraw(mAlarmTrigger);

		mSeekBar.setMax(SettingsActivity.MAX_ALARM_SENSITIVITY);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(final SeekBar seekBar,
					final int progress, final boolean fromUser) {
				if (fromUser) {
					// if the user scrolls above the bounds of the SeekBar,
					// progress can be out of bounds. Clamp it.
					mAlarmTrigger = Math.min(
							SettingsActivity.MAX_ALARM_SENSITIVITY, progress
									/ DecimalSeekBar.PRECISION);
					mSleepChart.setCalibrationLevelAndRedraw(mAlarmTrigger);
				}
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
				saveCalibrationLevel(a);
			}
		});
		mSeekBar.setProgress(mAlarmTrigger);
	}

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (mSleepChart != null) {

				mSleepChart.sync(intent.getDoubleExtra(
						SleepMonitoringService.EXTRA_X, 0), intent
						.getDoubleExtra(SleepMonitoringService.EXTRA_Y, 0));
				mSleepChart.setVisibility(View.VISIBLE);
				mSeekBar.setVisibility(View.VISIBLE);
				mWarmingUp.setVisibility(View.GONE);
				if (calibrationStateListener != null) {
					calibrationStateListener.onCalibrationComplete(true);
				}
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		resetViews(getActivity());
		getActivity().registerReceiver(updateChartReceiver,
				new IntentFilter(SleepActivity.UPDATE_CHART));

	}

	@Override
	public void onPause() {
		super.onPause();
		Activity a = getActivity();

		a.unregisterReceiver(updateChartReceiver);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat("mAlarmTrigger", mAlarmTrigger);
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.wizard_calibration_lightsleep;
	}

	@Override
	public void startCalibration(Activity context) {
		final Intent i = new Intent(context, SleepMonitoringService.class);
		context.stopService(i);
		i.putExtra("testModeRate",
				CalibrationWizardActivity.LIGHT_SLEEP_CALIBRATION_INTERVAL);
		i.putExtra("alarm", SettingsActivity.MAX_ALARM_SENSITIVITY);
		context.startService(i);
	}

	@Override
	public void stopCalibration(Activity context) {
		if (context.stopService(new Intent(context,
				SleepMonitoringService.class))) {
			SleepChart sleepChart = (SleepChart) context
					.findViewById(R.id.calibration_sleep_chart);
			if (sleepChart != null) {
				sleepChart.clear();
				VerticalSeekBar seekBar = (VerticalSeekBar) context
						.findViewById(R.id.calibration_level_seekbar);
				View warmingUp = context.findViewById(R.id.warming_up_text);

				sleepChart.setVisibility(View.INVISIBLE);
				seekBar.setVisibility(View.INVISIBLE);
				warmingUp.setVisibility(View.VISIBLE);
			}
		}
	}

	private void saveCalibrationLevel(Activity a) {
		// Save the trigger sensitivity
		a.getSharedPreferences(SettingsActivity.PREFERENCES, 0)
				.edit()
				.putFloat(a.getString(R.string.pref_alarm_trigger_sensitivity),
						mAlarmTrigger).commit();
	}

}