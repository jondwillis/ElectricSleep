package com.androsz.electricsleep.app;

import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.service.SleepAccelerometerService;
import com.androsz.electricsleep.util.Alarm;
import com.androsz.electricsleep.util.AlarmDatabase;
import com.androsz.electricsleep.view.SleepChartView;

public class SleepActivity extends CustomTitlebarActivity {

	private class WaitForSeriesDataProgressDialog extends ProgressDialog {
		public WaitForSeriesDataProgressDialog(final Context context) {
			super(context);
		}

		public WaitForSeriesDataProgressDialog(final Context context,
				final int theme) {
			// super(context);
			super(context, theme);
		}

		@Override
		public void onBackPressed() {
			SleepActivity.this.onBackPressed();
		}
	}

	public static final String UPDATE_CHART = "com.androsz.electricsleep.UPDATE_CHART";

	public static final String SYNC_CHART = "com.androsz.electricsleep.SYNC_CHART";

	private SleepChartView sleepChartView;

	private ProgressDialog waitForSeriesData;

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {

			if (sleepChartView != null) {
				sleepChartView.syncByAdding(intent.getDoubleExtra("x", 0),
						intent.getDoubleExtra("y", 0),
						intent.getIntExtra("min",
								SettingsActivity.DEFAULT_MIN_SENSITIVITY),
						intent.getIntExtra("alarm",
								SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

				if (sleepChartView.makesSenseToDisplay()
						&& waitForSeriesData != null) {
					waitForSeriesData.dismiss();
					waitForSeriesData = null;
				}
			}
		}
	};

	private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(final Context context, final Intent intent) {

			sleepChartView = (SleepChartView) findViewById(R.id.sleep_movement_chart);
			// sleepChartView.xySeriesMovement.mX = (List<Double>) intent
			// .getSerializableExtra("currentSeriesX");

			// sleepChartView.xySeriesMovement.mY = (List<Double>) intent
			// .getSerializableExtra("currentSeriesY");

			sleepChartView.syncByCopying((List<Double>) intent
					.getSerializableExtra("currentSeriesX"),
					(List<Double>) intent
							.getSerializableExtra("currentSeriesY"), intent
							.getIntExtra("min",
									SettingsActivity.DEFAULT_MIN_SENSITIVITY),
					intent.getIntExtra("alarm",
							SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

			if (sleepChartView.makesSenseToDisplay()
					&& waitForSeriesData != null) {
				waitForSeriesData.dismiss();
				waitForSeriesData = null;
			} else {
				showWaitForSeriesDataIfNeeded();
			}
		}
	};

	private final BroadcastReceiver sleepStoppedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			finish();
		}
	};

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		this.setTitle(R.string.monitoring_sleep);
		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_stop_default);
		showTitleButton2(R.drawable.ic_title_alarm);
		registerReceiver(sleepStoppedReceiver, new IntentFilter(
				SleepAccelerometerService.SLEEP_STOPPED));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sleepStoppedReceiver);
	}

	@Override
	protected void onPause() {
		if (waitForSeriesData != null && waitForSeriesData.isShowing()) {
			waitForSeriesData.dismiss();
		}
		unregisterReceiver(updateChartReceiver);
		unregisterReceiver(syncChartReceiver);
		super.onPause();
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		try {
			super.onRestoreInstanceState(savedState);
		} catch (final java.lang.RuntimeException rte) {
			// sendBroadcast(new
			// Intent(SleepAccelerometerService.POKE_SYNC_CHART));
		}
		sleepChartView = (SleepChartView) savedState
				.getSerializable("sleepChartView");

	}

	@Override
	protected void onResume() {
		super.onResume();

		// sendBroadcast(new Intent(SleepAccelerometerService.POKE_SYNC_CHART));
		// addChartView();
		registerReceiver(updateChartReceiver, new IntentFilter(UPDATE_CHART));
		registerReceiver(syncChartReceiver, new IntentFilter(SYNC_CHART));
		sendBroadcast(new Intent(SleepAccelerometerService.POKE_SYNC_CHART));

		try {
			final AlarmDatabase adb = new AlarmDatabase(getContentResolver(),
					"com.android.deskclock");
			final Alarm alarm = adb.getNearestEnabledAlarm();
			final Calendar alarmTime = alarm.getNearestAlarmDate();

			java.text.DateFormat df = DateFormat.getDateFormat(this);// .getDateFormat(this);
			String dateTime = df.format(alarmTime.getTime());
			df = DateFormat.getTimeFormat(this);
			dateTime = df.format(alarmTime.getTime()) + " on " + dateTime;
			// CharSequence cs = "":
			// StringBuilder sb = new StringBuilder();
			// Formatter formatter = new Formatter();
			Toast.makeText(this, "Bound to alarm @ " + dateTime,
					Toast.LENGTH_LONG).show();
		} catch (final Exception e) {

		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("sleepChartView", sleepChartView);
	}

	public void onTitleButton1Click(final View v) {

		new AlertDialog.Builder(this)
				.setMessage("Are you sure you want to end sleep monitoring?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								sendBroadcast(new Intent(
										SleepAccelerometerService.STOP_AND_SAVE_SLEEP));
								finish();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int id) {
					}
				}).show();
	}

	public void onTitleButton2Click(final View v) {
		startActivity(AlarmDatabase.changeAlarmSettings(getPackageManager()));
	}

	private void showWaitForSeriesDataIfNeeded() {
		if (sleepChartView == null || !sleepChartView.makesSenseToDisplay()) {
			if (waitForSeriesData == null || !waitForSeriesData.isShowing()) {
				waitForSeriesData = new WaitForSeriesDataProgressDialog(this);
				waitForSeriesData
						.setMessage(getText(R.string.dialog_wait_for_sleep_data_message));
				// waitForSeriesData.setContentView(R.layout.dialog_wait_for_data);
				waitForSeriesData.setButton(DialogInterface.BUTTON_NEGATIVE,
						getString(R.string.stop),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface arg0,
									final int arg1) {

								stopService(new Intent(SleepActivity.this,
										SleepAccelerometerService.class));
								SleepActivity.this.finish();
							}
						});
				waitForSeriesData.setButton(DialogInterface.BUTTON_NEUTRAL,
						getString(R.string.dismiss),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface arg0,
									final int arg1) {

								waitForSeriesData.dismiss();
							}
						});
				waitForSeriesData.show();
			}
		}
	}
}
