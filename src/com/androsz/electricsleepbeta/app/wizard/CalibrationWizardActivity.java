package com.androsz.electricsleepbeta.app.wizard;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.widget.DecimalSeekBar;
import com.androsz.electricsleepbeta.widget.SleepChart;
import com.viewpagerindicator.TitleProvider;

public class CalibrationWizardActivity extends WizardActivity {
	public static final int LIGHT_SLEEP_CALIBRATION_INTERVAL = 500;

	private CalibrateLightSleepFragment calibrateLightSleepFragment = new CalibrateLightSleepFragment(
			R.layout.activity_calibrate_alarm);

	private CheckForScreenBugFragment checkForScreenBugFragment = new CheckForScreenBugFragment(R.layout.activity_check_for_screen_bug);

	private double lightSleepTrigger;

	private boolean isScreenBugPresent;

	private class LayoutFragment extends Fragment {

		private int layoutId;

		public LayoutFragment(int layoutId) {
			this.layoutId = layoutId;
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			// TODO Auto-generated method stub
			View myFragmentView = inflater.inflate(layoutId, container, false);

			return myFragmentView;
		}
	}

	private class CalibrateLightSleepFragment extends LayoutFragment {
		public CalibrateLightSleepFragment(int layoutId) {
			super(layoutId);
		}

		@Override
		public void onActivityCreated(Bundle savedInstanceState) {
			super.onActivityCreated(savedInstanceState);

			if (savedInstanceState == null) {
				sleepChart = (SleepChart) findViewById(R.id.calibration_sleep_chart);
			} else {
				sleepChart = (SleepChart) savedInstanceState.getParcelable(SLEEP_CHART);
			}

			final DecimalSeekBar seekBar = (DecimalSeekBar) findViewById(R.id.calibration_level_seekbar);
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

			getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		}

		private static final String SLEEP_CHART = "sleepChart";

		SleepChart sleepChart;

		private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
			@SuppressWarnings("unchecked")
			@Override
			public void onReceive(final Context context, final Intent intent) {

				sleepChart = (SleepChart) findViewById(R.id.calibration_sleep_chart);

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
				 * CalibrateAlarmActivity.this .setResult(
				 * CALIBRATION_SUCCEEDED, new Intent().putExtra("y",
				 * sleepChart.getCalibrationLevel()));
				 */
				findViewById(R.id.calibration_sleep_chart).setVisibility(View.VISIBLE);
				findViewById(R.id.calibration_level_seekbar).setVisibility(View.VISIBLE);
				findViewById(R.id.warming_up_text).setVisibility(View.GONE);
				if (sleepChart != null) {
					final DecimalSeekBar seekBar = (DecimalSeekBar) findViewById(R.id.calibration_level_seekbar);
					seekBar.setProgress((float) sleepChart.getCalibrationLevel());
					sleepChart.sync(intent.getDoubleExtra(SleepMonitoringService.EXTRA_X, 0),
							intent.getDoubleExtra(SleepMonitoringService.EXTRA_Y, 0),
							sleepChart.getCalibrationLevel());
				}
			}
		};

		@Override
		public void onConfigurationChanged(Configuration newConfig) {
			// TODO
			// do not call super to prevent onWindowFocusChanged to be called
			// and
			// subsequent failure of the test
			super.onConfigurationChanged(newConfig);
		}

		@Override
		public void onResume() {
			super.onResume();
			registerReceiver(updateChartReceiver, new IntentFilter(SleepActivity.UPDATE_CHART));
			registerReceiver(syncChartReceiver, new IntentFilter(SleepActivity.SYNC_CHART));
			sendBroadcast(new Intent(SleepMonitoringService.POKE_SYNC_CHART));
		}

		@Override
		public void onSaveInstanceState(final Bundle outState) {
			super.onSaveInstanceState(outState);
			outState.putParcelable(SLEEP_CHART, sleepChart);
		}

		public void end() {
			lightSleepTrigger = sleepChart.getCalibrationLevel();
			stopService(new Intent(getActivity(), SleepMonitoringService.class));
		}

		public void begin() {
			final Intent i = new Intent(getActivity(), SleepMonitoringService.class);
			stopService(i);
			i.putExtra("testModeRate", LIGHT_SLEEP_CALIBRATION_INTERVAL);
			i.putExtra("alarm", SettingsActivity.MAX_ALARM_SENSITIVITY);
			startService(i);
		}

	}

	// hack-ish but necessary because lockscreens can differ
	public static Intent BUG_PRESENT_INTENT = null;

	private class CheckForScreenBugFragment extends LayoutFragment {

		public CheckForScreenBugFragment(int layoutId) {
			super(layoutId);
		}

		@Override
		public void onResume() {
			super.onResume();

			final Intent i = new Intent(getActivity(), CheckForScreenBugAccelerometerService.class);

			// this replaces the need for broadcast receivers.
			// the service updates BUG_PRESENT_INTENT, THEN our activity is
			// alerted.
			if (BUG_PRESENT_INTENT != null) {
				stopService(i);
				BUG_PRESENT_INTENT = null;
				finish();
			} else {
				startService(i);
			}
		}

	}

	private class WizardPagerAdapter extends FragmentPagerAdapter implements TitleProvider {

		public WizardPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		private String[] titles = new String[] { "Why", "Instructions", "Light Sleep",
				"Screen Test", "Done" };

		@Override
		public String getTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public Fragment getItem(int position) {

			switch (position) {
			case 0:
				return new LayoutFragment(R.layout.wizard_calibration_about);
			case 1:
				return new LayoutFragment(R.layout.wizard_calibration_instructions);
			case 2:
				return calibrateLightSleepFragment;
			case 3:
				return checkForScreenBugFragment;
			case 4:
				return new LayoutFragment(R.layout.wizard_calibration_results);

			default:
				throw new IllegalStateException("Could not find the correct fragment.");
			}
		}
	}

	@Override
	protected PagerAdapter getPagerAdapter() {
		return new WizardPagerAdapter(getSupportFragmentManager());
	}

	@Override
	protected void onFinishWizardActivity() throws IllegalStateException {
		final SharedPreferences.Editor ed = getSharedPreferences(SettingsActivity.PREFERENCES, 0)
				.edit();
		ed.putFloat(getString(R.string.pref_alarm_trigger_sensitivity), (float) lightSleepTrigger);
		ed.putBoolean(getString(R.string.pref_force_screen), isScreenBugPresent);
		ed.commit();

		if (ed.commit()) {
			final SharedPreferences.Editor ed2 = getSharedPreferences(
					SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE).edit();
			ed2.putInt(SettingsActivity.PREFERENCES_ENVIRONMENT,
					getResources().getInteger(R.integer.prefs_version));
			ed2.commit();

			trackEvent("alarm-level", (int) Math.round(lightSleepTrigger * 100));
			trackEvent("screen-bug", isScreenBugPresent ? 1 : 0);
			finish();
		} else {
			trackEvent("calibration-fail", 0);
		}
	}

	@Override
	protected void onPrepareLastSlide() {
		final TextView textViewAlarm = (TextView) findViewById(R.id.alarmResult);
		textViewAlarm.setText(String.format("%.2f", lightSleepTrigger));
		final TextView textViewScreen = (TextView) findViewById(R.id.screenResult);
		textViewScreen.setText(isScreenBugPresent + "");
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		lightSleepTrigger = savedState.getDouble("alarm");
		isScreenBugPresent = savedState.getBoolean("screenBug");

		setupNavigationButtons();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putDouble("alarm", lightSleepTrigger);
		outState.putBoolean("screenBug", isScreenBugPresent);
	}

	@Override
	public void onLeftButtonClick(View v) {
		super.onLeftButtonClick(v);
	}

	@Override
	protected boolean onPerformWizardAction() {
		switch (getCurrentWizardIndex()) {
		case 2:
			calibrateLightSleepFragment.begin();
			break;
		case 3:
			break;
		}
		return false;
	}

}
