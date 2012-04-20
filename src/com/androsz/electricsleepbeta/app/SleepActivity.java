package com.androsz.electricsleepbeta.app;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.alarmclock.Alarm;
import com.androsz.electricsleepbeta.alarmclock.AlarmClock;
import com.androsz.electricsleepbeta.alarmclock.Alarms;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class SleepActivity extends HostActivity {

	private static final String TAG = SleepActivity.class.getSimpleName();

	private class DimScreenTask extends AsyncTask<Void, Void, Void> {

		private boolean mScreenDimmed = false;
		public boolean isScreenDimmed() {
			return mScreenDimmed;
		}

		private int timer = 0;

		@Override
		protected Void doInBackground(final Void... params) {
			// just wait without blocking the main thread!
			while (!isCancelled() && timer < DIM_SCREEN_AFTER_MS) {
				try {
					timer += 1000;
					Thread.sleep(1000);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onCancelled() {
			mScreenDimmed = false;
			Log.d(TAG, "onCancelled");
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(final Void results) {
			Log.d(TAG, "onPostExecute");
			if (!isCancelled()) {
				Log.d(TAG, "!isCancelled");
				mScreenDimmed = true;
				// after we have waited, dim the screen on the main thread!
				startActivity(new Intent(SleepActivity.this,
						DimSleepActivity.class));
			}
		}

		@Override
		protected void onPreExecute() {
			mScreenDimmed = false;
			// notify the user that we've received that they need a dimmed
			// screen
			setToast(R.string.screen_will_dim);
		}
	}

	private void setToast(int stringId) {
		Toast.makeText(this, stringId, Toast.LENGTH_SHORT).show();
	}

	private static final int DIM_SCREEN_AFTER_MS = 15000;

	private static final String SLEEP_CHART = "sleepChart";

	public static final String SYNC_CHART = "com.androsz.electricsleepbeta.SYNC_CHART";

	public static final String UPDATE_CHART = "com.androsz.electricsleepbeta.UPDATE_CHART";

	private final BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) > 0;
			airplaneModeNagChanged();
		}
	};

	private SleepMonitoringService mMonitoringService = null;

	DimScreenTask dimScreenTask;

	private SleepChart sleepChart;

	private final BroadcastReceiver sleepStoppedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			finish();
		}
	};

	/** Boolean that indicates whether or not the service is bound. */
	private AtomicBoolean mServiceBound;

	private TextView buttonSleepDim;
	private TextView buttonSleepPluggedIn;
	private TextView textAlarmStatus;
	private TextView textAlarmStatusSub;

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			sleepChart.sync(intent.getDoubleExtra(
					SleepMonitoringService.EXTRA_X, 0), intent.getDoubleExtra(
					SleepMonitoringService.EXTRA_Y, 0));
		}
	};

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	boolean pluggedIn = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mServiceBound = new AtomicBoolean();

		setTitle(R.string.monitoring_sleep);
		airplaneModeOn = Settings.System.getInt(getContentResolver(),
				Settings.System.AIRPLANE_MODE_ON, 0) != 0;

		registerReceiver(sleepStoppedReceiver, new IntentFilter(
				SleepMonitoringService.SLEEP_STOPPED));

		sleepChart = (SleepChart) findViewById(R.id.sleep_movement_chart);
		sleepChart.setVisibility(View.VISIBLE);

		// Start the sleep monitoring service
		startService(new Intent(this, SleepMonitoringService.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_monitoring_sleep, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(sleepStoppedReceiver);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_alarms:
			openAlarms();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void openAlarms() {
		startActivity(new Intent(this, AlarmClock.class));
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_tracking_stop:
			sendBroadcast(new Intent(SleepMonitoringService.STOP_AND_SAVE_SLEEP));
			finish();
			break;
		case R.id.text_alarm_status:
		case R.id.text_alarm_status_sub:
		case R.id.layout_alarm_status:
			openAlarms();
			break;
		}
	}

	@Override
	protected void onPause() {
		unregisterReceiver(airplaneModeChangedReceiver);
		unregisterReceiver(updateChartReceiver);
		unregisterReceiver(batteryChangedReceiver);
		cancelDimScreenTask();
		if (mServiceBound.compareAndSet(true, false)) {
			unbindService(serviceConnection);
		}
		isUserLeaving = false;
		super.onPause();
	}

	boolean isUserLeaving = false;

	@Override
	protected void onUserLeaveHint() {
		Log.d(TAG, "onUserLeaveHint()");
		isUserLeaving = true;
		super.onUserLeaveHint();
	}

	private void cancelDimScreenTask() {
		Log.d(TAG, "cancelDimScreenTask");
		// cancel the dim screen task if it hasn't completed
		if (dimScreenTask != null) {
			Log.d(TAG, "dimScreenTask != null");
			dimScreenTask.cancel(true);

			// only show this toast is the user is leaving and the screen has
			// not been dimmed by the dimScreenTask.
			if (!dimScreenTask.isScreenDimmed() && (isUserLeaving || isFinishing())) {
				Log.d(TAG, "!screenDimmed && isUserLeaving");
				setToast(R.string.warning_dim_sleep_mode_can_only_occur_on_the_sleep_screen_);
			}
		}
	}

	boolean airplaneModeOn = false;
	BroadcastReceiver airplaneModeChangedReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			airplaneModeOn = intent.getBooleanExtra("state", false);
			airplaneModeNagChanged();
		}

	};

	private void airplaneModeNagChanged() {
		final int visibility = (pluggedIn || airplaneModeOn ? View.GONE
				: View.VISIBLE);

		buttonSleepPluggedIn.setVisibility(visibility);
	}

	@Override
	protected void onResume() {
		textAlarmStatus = (TextView) findViewById(R.id.text_alarm_status);
		textAlarmStatusSub = (TextView) findViewById(R.id.text_alarm_status_sub);
		buttonSleepDim = (TextView) findViewById(R.id.text_sleep_dim);
		buttonSleepPluggedIn = (TextView) findViewById(R.id.text_sleep_plugged_in);

		registerReceiver(airplaneModeChangedReceiver, new IntentFilter(
				Intent.ACTION_AIRPLANE_MODE_CHANGED));
		registerReceiver(batteryChangedReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		registerReceiver(updateChartReceiver, new IntentFilter(UPDATE_CHART));

		if (mServiceBound.compareAndSet(false, true)) {
			bindService(new Intent(this, SleepMonitoringService.class),
					serviceConnection, Context.BIND_AUTO_CREATE);
		}
		super.onResume();
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {

		public void onServiceConnected(ComponentName className, IBinder iBinder) {
			SleepMonitoringService.ServiceBinder binder = (SleepMonitoringService.ServiceBinder) iBinder;
			mMonitoringService = binder.getService();

			sleepChart.setCalibrationLevelAndRedraw(mMonitoringService
					.getAlarmTriggerSensitivity());
			sleepChart.sync(mMonitoringService.getData());
			final boolean useAlarm = mMonitoringService.getUseAlarm();
			final boolean forceScreenOn = mMonitoringService.getForceScreenOn();

			// dims the screen while in this activity and
			// forceScreenOn is
			// enabled
			if (forceScreenOn) {
				buttonSleepDim.setVisibility(View.VISIBLE);

				cancelDimScreenTask();
				dimScreenTask = new DimScreenTask();
				dimScreenTask.execute();

			} else {
				buttonSleepDim.setVisibility(View.GONE);
			}

			new AsyncTask<Void, Void, String[]>() {
				@Override
				protected String[] doInBackground(Void... params) {
					String[] result = null;
					final Alarm alarm = Alarms
							.calculateNextAlert(SleepActivity.this);
					if (alarm != null) {
						final Calendar alarmTime = Calendar.getInstance();
						alarmTime.setTimeInMillis(alarm.time);

						java.text.DateFormat df = DateFormat
								.getDateFormat(SleepActivity.this);
						df = DateFormat.getTimeFormat(SleepActivity.this);
						final String dateTime = df.format(alarmTime.getTime());
						final int alarmWindow = mMonitoringService
								.getAlarmWindow();
						alarmTime.add(Calendar.MINUTE, -1 * alarmWindow);
						final String dateTimePre = df.format(alarmTime
								.getTime());
						result = new String[] { dateTimePre, dateTime };
					}
					return result;
				}

				@Override
				protected void onPostExecute(String[] result) {
					if (textAlarmStatus != null) {
						if (result != null) {

							if (useAlarm) {
								textAlarmStatus.setText(getString(
										R.string.alarm_status_range, result[0],
										result[1]));
								textAlarmStatus
										.setCompoundDrawablesWithIntrinsicBounds(
												getResources()
														.getDrawable(
																R.drawable.ic_alarm_pressed),
												null, null, null);
								textAlarmStatusSub
										.setText(R.string.attempt_to_use_smartwake_);
							} else {
								textAlarmStatus
										.setCompoundDrawablesWithIntrinsicBounds(
												getResources()
														.getDrawable(
																R.drawable.ic_alarm_neutral),
												null, null, null);
								textAlarmStatus.setText(result[0]);
								textAlarmStatusSub
										.setText(R.string.not_using_smartwake_);
							}

						} else {
							textAlarmStatus
									.setCompoundDrawablesWithIntrinsicBounds(
											getResources().getDrawable(
													R.drawable.ic_alarm_none),
											null, null, null);
							textAlarmStatus.setText(R.string.no_alarm);
							textAlarmStatusSub.setText(R.string.sleep_no_alarm);
						}
					}
				}
			}.execute();
		}

		public void onServiceDisconnected(ComponentName className) {
			mMonitoringService = null;
		}
	};
}
