package com.androsz.electricsleepbeta.app;

import java.util.List;

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
import com.androsz.electricsleepbeta.util.PointD;
import com.androsz.electricsleepbeta.widget.DecimalSeekBar;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class CalibrateAlarmActivity extends CalibrateForResultActivity {

	private static final String SLEEP_CHART = "sleepChart";

	SleepChart sleepChart;

	private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(final Context context, final Intent intent) {

			sleepChart = (SleepChart) findViewById(R.id.calibration_sleep_chart);

			// inlined for efficiency
			sleepChart.xySeriesMovement.xyList = (List<PointD>) intent
					.getSerializableExtra(SleepMonitoringService.SLEEP_DATA);
			sleepChart.reconfigure();
			sleepChart.repaint();
		}
	};

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			/*
			 * CalibrateAlarmActivity.this .setResult( CALIBRATION_SUCCEEDED,
			 * new Intent().putExtra("y", sleepChart.getCalibrationLevel()));
			 */
			CalibrateAlarmActivity.this.findViewById(
					R.id.calibration_sleep_chart).setVisibility(View.VISIBLE);
			CalibrateAlarmActivity.this.findViewById(
					R.id.calibration_level_seekbar).setVisibility(View.VISIBLE);
			CalibrateAlarmActivity.this.findViewById(R.id.warming_up_text)
					.setVisibility(View.GONE);
			if (sleepChart != null) {
				final DecimalSeekBar seekBar = (DecimalSeekBar) findViewById(R.id.calibration_level_seekbar);
				seekBar.setProgress((float) sleepChart.getCalibrationLevel());
				sleepChart.sync(intent.getDoubleExtra(
						SleepMonitoringService.EXTRA_X, 0), intent
						.getDoubleExtra(SleepMonitoringService.EXTRA_Y, 0),
						sleepChart.getCalibrationLevel());
			}
		}
	};

	@Override
	protected Intent getAssociatedServiceIntent() {
		return new Intent(this, SleepMonitoringService.class);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calibrate_alarm);

		sleepChart = (SleepChart) findViewById(R.id.calibration_sleep_chart);
		final DecimalSeekBar seekBar = (DecimalSeekBar) findViewById(R.id.calibration_level_seekbar);
		seekBar.setMax((int) SettingsActivity.MAX_ALARM_SENSITIVITY);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(final SeekBar seekBar,
					final int progress, final boolean fromUser) {
				if (fromUser) {
					sleepChart.setCalibrationLevel(progress
							/ DecimalSeekBar.PRECISION);
				}
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
			}
		});

		sleepChart
				.setCalibrationLevel(SettingsActivity.DEFAULT_ALARM_SENSITIVITY);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	public void onDoneClick(final View v) {
		CalibrateAlarmActivity.this.setResult(CALIBRATION_SUCCEEDED,
				new Intent().putExtra(SleepMonitoringService.EXTRA_Y,
						sleepChart.getCalibrationLevel()));
		finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(updateChartReceiver);
		unregisterReceiver(syncChartReceiver);
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		try {
			super.onRestoreInstanceState(savedState);
		} catch (final java.lang.RuntimeException rte) {
			// sendBroadcast(new
			// Intent(SleepMonitoringService.POKE_SYNC_CHART));
		}
		sleepChart = (SleepChart) savedState.getParcelable(SLEEP_CHART);
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(updateChartReceiver, new IntentFilter(
				SleepActivity.UPDATE_CHART));
		registerReceiver(syncChartReceiver, new IntentFilter(
				SleepActivity.SYNC_CHART));
		sendBroadcast(new Intent(SleepMonitoringService.POKE_SYNC_CHART));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(SLEEP_CHART, sleepChart);
	}

}