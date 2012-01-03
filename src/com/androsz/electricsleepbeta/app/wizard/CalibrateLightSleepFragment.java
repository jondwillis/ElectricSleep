package com.androsz.electricsleepbeta.app.wizard;

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
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.widget.DecimalSeekBar;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class CalibrateLightSleepFragment extends LayoutFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (savedInstanceState == null) {
			sleepChart = (SleepChart) getActivity().findViewById(R.id.calibration_sleep_chart);
		} else {
			sleepChart = (SleepChart) savedInstanceState.getParcelable(SLEEP_CHART);
		}

		final DecimalSeekBar seekBar = (DecimalSeekBar) getActivity().findViewById(
				R.id.calibration_level_seekbar);
		seekBar.setMax((int) SettingsActivity.MAX_ALARM_SENSITIVITY);
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(final SeekBar seekBar, final int progress,
					final boolean fromUser) {
				if (fromUser) {
					sleepChart.setCalibrationLevel(progress / DecimalSeekBar.PRECISION);
				}
			}

			@Override
			public void onStartTrackingTouch(final SeekBar seekBar) {
			}

			@Override
			public void onStopTrackingTouch(final SeekBar seekBar) {
			}
		});

		sleepChart.setCalibrationLevel(SettingsActivity.DEFAULT_ALARM_SENSITIVITY);

		getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	private static final String SLEEP_CHART = "sleepChart";

	SleepChart sleepChart;

	private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(final Context context, final Intent intent) {

			sleepChart = (SleepChart) getActivity().findViewById(R.id.calibration_sleep_chart);

			// inlined for efficiency
			sleepChart.xySeriesMovement.setXY((List<org.achartengine.model.PointD>) intent
					.getSerializableExtra(SleepMonitoringService.SLEEP_DATA));
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
			getActivity().findViewById(R.id.calibration_sleep_chart).setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.calibration_level_seekbar).setVisibility(View.VISIBLE);
			getActivity().findViewById(R.id.warming_up_text).setVisibility(View.GONE);
			if (sleepChart != null) {
				final DecimalSeekBar seekBar = (DecimalSeekBar) getActivity().findViewById(
						R.id.calibration_level_seekbar);
				seekBar.setProgress((float) sleepChart.getCalibrationLevel());
				sleepChart.sync(intent.getDoubleExtra(SleepMonitoringService.EXTRA_X, 0),
						intent.getDoubleExtra(SleepMonitoringService.EXTRA_Y, 0),
						sleepChart.getCalibrationLevel());
			}
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		getActivity().registerReceiver(updateChartReceiver,
				new IntentFilter(SleepActivity.UPDATE_CHART));
		getActivity().registerReceiver(syncChartReceiver,
				new IntentFilter(SleepActivity.SYNC_CHART));
		getActivity().sendBroadcast(new Intent(SleepMonitoringService.POKE_SYNC_CHART));
	}

	@Override
	public void onPause() {
		super.onPause();
		end();
		getActivity().unregisterReceiver(updateChartReceiver);
		getActivity().unregisterReceiver(syncChartReceiver);
	}

	@Override
	public void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(SLEEP_CHART, sleepChart);
	}

	public void end() {
		if (sleepChart != null) {
			// getActivity().lightSleepTrigger =
			// sleepChart.getCalibrationLevel();
			getActivity().stopService(new Intent(getActivity(), SleepMonitoringService.class));
			sleepChart.clearMovement();
			// sleepChart.setVisibility(View.INVISIBLE);
			// findViewById(R.id.calibration_level_seekbar).setVisibility(View.INVISIBLE);
			// findViewById(R.id.warming_up_text).setVisibility(View.VISIBLE);
		}
	}

	public void begin() {
			final Intent i = new Intent(getActivity(), SleepMonitoringService.class);
			getActivity().stopService(i);
			i.putExtra("testModeRate", CalibrationWizardActivity.LIGHT_SLEEP_CALIBRATION_INTERVAL);
			i.putExtra("alarm", SettingsActivity.MAX_ALARM_SENSITIVITY);
			getActivity().startService(i);
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.wizard_calibration_lightsleep;
	}

}