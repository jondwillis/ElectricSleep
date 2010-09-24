package com.androsz.electricsleep.service;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.ui.SaveSleepActivity;
import com.androsz.electricsleep.ui.SettingsActivity;
import com.androsz.electricsleep.ui.SleepActivity;
import com.androsz.electricsleep.util.Alarm;
import com.androsz.electricsleep.util.AlarmDatabase;

public class SleepAccelerometerService extends Service implements
		SensorEventListener {
	public static final String POKE_SYNC_CHART = "com.androsz.electricsleep.POKE_SYNC_CHART";
	public static final String POKE_SAVE_SLEEP = "com.androsz.electricsleep.POKE_SAVE_SLEEP";
	public static final String ALARM_TRIGGERED = "com.androsz.electricsleep.ALARM_TRIGGERED";

	private static final int NOTIFICATION_ID = 0x1337a;

	public ArrayList<Double> currentSeriesX = new ArrayList<Double>();
	public ArrayList<Double> currentSeriesY = new ArrayList<Double>();

	private SensorManager sensorManager;

	private PowerManager powerManager;
	private WakeLock partialWakeLock;

	private NotificationManager notificationManager;

	private long lastChartUpdateTime = System.currentTimeMillis();
	private long lastOnSensorChangedTime = System.currentTimeMillis();
	private long totalTimeBetweenSensorChanges = 0;
	private int totalNumberOfSensorChanges = 0;

	private int minSensitivity = SettingsActivity.DEFAULT_MIN_SENSITIVITY;
	private int maxSensitivity = SettingsActivity.DEFAULT_MAX_SENSITIVITY;
	private int alarmTriggerSensitivity = SettingsActivity.DEFAULT_ALARM_SENSITIVITY;

	private boolean useAlarm = false;
	private int alarmWindow = 30;

	private int updateInterval = 30000;

	private Date dateStarted;

	public static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;

	private final BroadcastReceiver pokeSyncChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (currentSeriesX.size() > 0 && currentSeriesY.size() > 0) {
				final Intent i = new Intent(SleepActivity.SYNC_CHART);
				i.putExtra("currentSeriesX", currentSeriesX);
				i.putExtra("currentSeriesY", currentSeriesY);
				i.putExtra("min", minSensitivity);
				i.putExtra("max", maxSensitivity);
				i.putExtra("alarm", alarmTriggerSensitivity);
				sendBroadcast(i);
			}
		}
	};

	private final BroadcastReceiver pokeSaveSleepReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final Intent saveIntent = new Intent(SaveSleepActivity.SAVE_SLEEP);

			saveIntent.putExtra("currentSeriesX", currentSeriesX);
			saveIntent.putExtra("currentSeriesY", currentSeriesY);
			saveIntent.putExtra("min", minSensitivity);
			saveIntent.putExtra("max", maxSensitivity);
			saveIntent.putExtra("alarm", alarmTriggerSensitivity);

			// send start/end time as well
			final DateFormat sdf = DateFormat.getDateTimeInstance(
					DateFormat.SHORT, DateFormat.SHORT, Locale.getDefault());
			DateFormat sdf2 = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					DateFormat.SHORT, Locale.getDefault());
			final Date now = new Date();
			if (dateStarted.getDate() == now.getDate()) {
				sdf2 = DateFormat.getTimeInstance(DateFormat.SHORT);
			}
			saveIntent.putExtra("name", sdf.format(dateStarted) + " "
					+ getText(R.string.to) + " " + sdf2.format(now));

			sendBroadcast(saveIntent);
		}
	};

	private void createNotification() {
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		final int icon = R.drawable.icon;
		final CharSequence tickerText = getText(R.string.notification_sleep_started);
		final long when = System.currentTimeMillis();

		final Notification notification = new Notification(icon, tickerText,
				when);

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		final Context context = getApplicationContext();
		final CharSequence contentTitle = getText(R.string.notification_sleep_title);
		final CharSequence contentText = getText(R.string.notification_sleep_text);
		final Intent notificationIntent = new Intent(this, SleepActivity.class);
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notificationManager.notify(NOTIFICATION_ID, notification);
	}

	private void obtainWakeLock() {
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		partialWakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, toString());
		partialWakeLock.acquire();
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		// not used
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		registerReceiver(pokeSyncChartReceiver, new IntentFilter(
				POKE_SYNC_CHART));

		registerReceiver(pokeSaveSleepReceiver, new IntentFilter(
				POKE_SAVE_SLEEP));

		registerAccelerometerListener();

		obtainWakeLock();

		createNotification();

		dateStarted = new Date();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(pokeSyncChartReceiver);
		unregisterReceiver(pokeSaveSleepReceiver);

		sensorManager.unregisterListener(SleepAccelerometerService.this);

		partialWakeLock.release();

		notificationManager.cancel(NOTIFICATION_ID);

		// saveSleepData();
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		final long currentTime = System.currentTimeMillis();
		final long timeSinceLastSensorChange = currentTime
				- lastOnSensorChangedTime;

		totalNumberOfSensorChanges++;
		totalTimeBetweenSensorChanges += timeSinceLastSensorChange;

		final long deltaTime = currentTime - lastChartUpdateTime;
		if (deltaTime > updateInterval) {
			final double averageTimeBetweenUpdates = totalTimeBetweenSensorChanges
					/ totalNumberOfSensorChanges;
			final double x = currentTime;
			final double y = java.lang.Math.max(
					minSensitivity,
					java.lang.Math.min(maxSensitivity, deltaTime
							/ averageTimeBetweenUpdates));

			// this should help reduce both runtime memory and saved data memory
			// later on.
			final int yOneAgoIndex = currentSeriesY.size() - 1;
			final int yTwoAgoIndex = currentSeriesY.size() - 2;
			boolean syncChart = false;
			if (yTwoAgoIndex > 0) {
				final double oneAgo = currentSeriesY.get(yOneAgoIndex);
				final double twoAgo = currentSeriesY.get(yTwoAgoIndex);
				if (Math.round(oneAgo) == Math.round(twoAgo)
						&& Math.round(oneAgo) == Math.round(y)) {
					currentSeriesX.remove(yOneAgoIndex);
					currentSeriesY.remove(yOneAgoIndex);
					// flag to sync instead of update
					syncChart = true;
				}
			}

			currentSeriesX.add(x);
			currentSeriesY.add(y);

			if (syncChart) {
				pokeSyncChartReceiver.onReceive(this, null);
			} else {
				final Intent i = new Intent(SleepActivity.UPDATE_CHART);
				i.putExtra("x", x);
				i.putExtra("y", y);
				i.putExtra("min", minSensitivity);
				i.putExtra("max", maxSensitivity);
				i.putExtra("alarm", alarmTriggerSensitivity);
				sendBroadcast(i);
			}

			totalNumberOfSensorChanges = 0;
			totalTimeBetweenSensorChanges = 0;

			lastChartUpdateTime = currentTime;

			if (useAlarm) {
				final AlarmDatabase adb = new AlarmDatabase(
						getContentResolver(), "com.android.deskclock");
				final Alarm alarm = adb.getNearestEnabledAlarm();
				final Calendar alarmTime = alarm.getNearestAlarmDate();
				alarmTime.add(Calendar.MINUTE, alarmWindow * -1);
				final long alarmMillis = alarmTime.getTimeInMillis();
				if (currentTime >= alarmMillis && y >= alarmTriggerSensitivity) {
					alarm.time = currentTime;
					
					final Intent saveActivityIntent = new Intent(this,
							SaveSleepActivity.class);
					saveActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(saveActivityIntent);
					sensorManager
							.unregisterListener(SleepAccelerometerService.this);
					
					com.androsz.electricsleep.util.AlarmDatabase.triggerAlarm(
							this, alarm);
				}
			}
		}
		lastOnSensorChangedTime = currentTime;
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags,
			final int startId) {
		updateInterval = intent.getIntExtra("interval", updateInterval);
		minSensitivity = intent.getIntExtra("min", minSensitivity);
		maxSensitivity = intent.getIntExtra("max", maxSensitivity);
		alarmTriggerSensitivity = intent.getIntExtra("alarm",
				alarmTriggerSensitivity);

		useAlarm = intent.getBooleanExtra("useAlarm", useAlarm);
		alarmWindow = intent.getIntExtra("alarmWindow", alarmWindow);

		return startId;
	}

	private void registerAccelerometerListener() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SENSOR_DELAY);
	}
}
