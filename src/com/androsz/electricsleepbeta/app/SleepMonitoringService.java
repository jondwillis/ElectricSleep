package com.androsz.electricsleepbeta.app;

import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.model.PointD;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.IBinder;
import android.os.PowerManager;
import android.provider.Settings;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.alarmclock.Alarm;
import com.androsz.electricsleepbeta.alarmclock.Alarms;
import com.androsz.electricsleepbeta.app.wizard.CalibrationWizardActivity;
import com.androsz.electricsleepbeta.content.StartSleepReceiver;
import com.androsz.electricsleepbeta.util.WakeLockManager;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SleepMonitoringService extends Service implements SensorEventListener {
	private final class UpdateTimerTask extends TimerTask {
		@Override
		public void run() {
			final long currentTime = System.currentTimeMillis();

			final double x = currentTime;
			final double y = java.lang.Math
					.min(SettingsActivity.MAX_ALARM_SENSITIVITY, maxNetForce);

			final PointD sleepPoint = new PointD(x, y);
			if (sleepData.size() >= MAX_POINTS_IN_A_GRAPH) {
				sleepData.remove(0);
			}

			sleepData.add(sleepPoint);

			// append the two doubles in sleepPoint to file
			try {
				synchronized (DATA_LOCK) {
					final FileOutputStream fos = openFileOutput(SLEEP_DATA, Context.MODE_APPEND);
					fos.write(PointD.toByteArray(sleepPoint));
					fos.close();
				}
			} catch (final IOException e) {
				GoogleAnalyticsTracker.getInstance().trackEvent(Integer.toString(VERSION.SDK_INT),
						Build.MODEL, "sleepMonitorCacheFailWrite : " + e.getMessage(), 0);
			}

			final Intent i = new Intent(SleepActivity.UPDATE_CHART);
			i.putExtra(EXTRA_X, x);
			i.putExtra(EXTRA_Y, y);
			i.putExtra(StartSleepReceiver.EXTRA_ALARM, alarmTriggerSensitivity);
			sendBroadcast(i);

			maxNetForce = 0;

			triggerAlarmIfNecessary(currentTime, y);
		}
	}

	public static final String EXTRA_ALARM_WINDOW = "alarmWindow";

	public static final String EXTRA_ID = "id";
	public static final String EXTRA_NAME = "name";
	public static final String EXTRA_X = "x";
	public static final String EXTRA_Y = "y";
	private final static int INTERVAL = 5000;
	public static int MAX_POINTS_IN_A_GRAPH = 200;
	private static final int NOTIFICATION_ID = 0x1337a;
	public static final String POKE_SYNC_CHART = "com.androsz.electricsleepbeta.POKE_SYNC_CHART";
	// Object for intrinsic lock
	public static final Object[] DATA_LOCK = new Object[0];

	public static final String SERVICE_IS_RUNNING = "serviceIsRunning";

	public static final String SLEEP_DATA = "sleepData";
	public static final String SLEEP_STOPPED = "com.androsz.electricsleepbeta.SLEEP_STOPPED";
	public static final String STOP_AND_SAVE_SLEEP = "com.androsz.electricsleepbeta.STOP_AND_SAVE_SLEEP";

	private boolean airplaneMode = false;

	private double alarmTriggerSensitivity = SettingsActivity.DEFAULT_ALARM_SENSITIVITY;
	private int alarmWindow = 30;

	final float alpha = 0.8f;
	private boolean alreadyDeletedResidualFile = false;

	private Date dateStarted;

	private boolean forceScreenOn = false;

	private final float[] gravity = { 0, 0, 0 };

	private double maxNetForce = SettingsActivity.DEFAULT_MIN_SENSITIVITY;
	private int ringerModeBackup = AudioManager.RINGER_MODE_NORMAL;

	public int sensorDelay = SensorManager.SENSOR_DELAY_NORMAL;

	private final BroadcastReceiver serviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (action.equals(POKE_SYNC_CHART)) {
				final Intent i = new Intent(SleepActivity.SYNC_CHART);
				i.putExtra(SLEEP_DATA, sleepData);
				i.putExtra(StartSleepReceiver.EXTRA_ALARM, alarmTriggerSensitivity);
				i.putExtra(EXTRA_ALARM_WINDOW, alarmWindow);
				i.putExtra(StartSleepReceiver.EXTRA_USE_ALARM, useAlarm);
				i.putExtra(StartSleepReceiver.EXTRA_FORCE_SCREEN_ON, forceScreenOn);
				i.putExtra(StartSleepReceiver.EXTRA_FORCE_SCREEN_ON, forceScreenOn);
				sendBroadcast(i);
			} else if (action.equals(STOP_AND_SAVE_SLEEP)) {
				final Intent saveIntent = addExtrasToSaveSleepIntent(new Intent(
						SleepMonitoringService.this, SaveSleepActivity.class));
				startActivity(saveIntent);
				sendBroadcast(new Intent(Alarms.CANCEL_SNOOZE));
				final long now = System.currentTimeMillis();
				try {
					final Alarm alarm = Alarms.calculateNextAlert(context);
					if (now > alarm.time + 60 * alarmWindow * 1000) {
						Alarms.setTimeToIgnore(context, alarm, alarm.time);
						Alarms.setNextAlert(context);
					}
				} catch (final NullPointerException npe) {
					// there are no enabled alarms
				}
				stopSelf();
			} else {
				if (action.equals(Alarms.CANCEL_SNOOZE)) {
					final long now = System.currentTimeMillis();
					try {
						final Alarm alarm = Alarms.getAlarm(context.getContentResolver(),
								intent.getIntExtra(Alarms.ALARM_ID, -1));
						if (now > alarm.time + 60 * alarmWindow * 1000) {
							Alarms.setTimeToIgnore(context, alarm, alarm.time);
							Alarms.setNextAlert(context);
						}
					} catch (final NullPointerException npe) {
						// there are no enabled alarms
					}
				}
				createSaveSleepNotification();
				stopSelf();
			}
		}
	};

	private boolean silentMode = false;

	private final ArrayList<PointD> sleepData = new ArrayList<PointD>();

	private int testModeRate = Integer.MIN_VALUE;

	private int updateInterval = INTERVAL;;

	Timer updateTimer;

	private boolean useAlarm = false;

	int waitForSensorsToWarmUp = 0;

	private Intent addExtrasToSaveSleepIntent(final Intent saveIntent) {
		saveIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		saveIntent.putExtra(EXTRA_ID, hashCode());
		saveIntent.putExtra(StartSleepReceiver.EXTRA_ALARM, alarmTriggerSensitivity);

		// send start/end time as well
		final DateFormat sdf = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
				Locale.getDefault());
		DateFormat sdf2 = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT,
				Locale.getDefault());
		final Date now = new Date();
		if (dateStarted.getDate() == now.getDate()) {
			sdf2 = DateFormat.getTimeInstance(DateFormat.SHORT);
		}
		saveIntent.putExtra(EXTRA_NAME, sdf.format(dateStarted) + " " + getText(R.string.to) + " "
				+ sdf2.format(now));
		return saveIntent;
	}

	private void createSaveSleepNotification() {
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		final int icon = R.drawable.home_btn_sleep_pressed;
		final CharSequence tickerText = getText(R.string.notification_save_sleep_ticker);
		final long when = System.currentTimeMillis();

		final Notification notification = new Notification(icon, tickerText, when);

		notification.flags = Notification.FLAG_AUTO_CANCEL;

		final Context context = getApplicationContext();
		final CharSequence contentTitle = getText(R.string.notification_save_sleep_title);
		final CharSequence contentText = getText(R.string.notification_save_sleep_text);
		final Intent notificationIntent = addExtrasToSaveSleepIntent(new Intent(this,
				SaveSleepActivity.class));
		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

		notificationManager.notify(hashCode(), notification);
		startActivity(notificationIntent);
	}

	private Notification createServiceNotification() {
		final int icon = R.drawable.icon_small;
		final CharSequence tickerText = getText(R.string.notification_sleep_ticker);
		final long when = System.currentTimeMillis();

		final Notification notification = new Notification(icon, tickerText, when);

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		final CharSequence contentTitle = getText(R.string.notification_sleep_title);
		final CharSequence contentText = getText(R.string.notification_sleep_text);
		Intent notificationIntent = null;

		// prevents the user from entering SleepActivity from the notification
		// when in test mode
		if (this.testModeRate == Integer.MIN_VALUE) {
			notificationIntent = new Intent(this, SleepActivity.class);
		} else {
			notificationIntent =  new Intent(this, CalibrationWizardActivity.class);
		}
		notificationIntent
				.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				0);

		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);

		return notification;
	}

	private void obtainWakeLock() {
		// if forcescreenon is on, hold a dim wakelock, otherwise, partial.
		final int wakeLockType = forceScreenOn ? (PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ON_AFTER_RELEASE | PowerManager.ACQUIRE_CAUSES_WAKEUP)

		: PowerManager.PARTIAL_WAKE_LOCK;

		WakeLockManager.acquire(this, "sleepMonitoring", wakeLockType);
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

		final IntentFilter filter = new IntentFilter(Alarms.ALARM_DISMISSED_BY_USER_ACTION);
		filter.addAction(Alarms.ALARM_SNOOZE_CANCELED_BY_USER_ACTION);
		filter.addAction(STOP_AND_SAVE_SLEEP);
		filter.addAction(POKE_SYNC_CHART);

		registerReceiver(serviceReceiver, filter);

		updateTimer = new Timer();

		dateStarted = new Date();
	}

	@Override
	public void onDestroy() {

		unregisterAccelerometerListener();

		WakeLockManager.release("sleepMonitoring");

		unregisterReceiver(serviceReceiver);

		// tell monitoring activities that sleep has ended
		sendBroadcast(new Intent(SLEEP_STOPPED));

		stopForeground(true);
		updateTimer.cancel();

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				toggleSilentMode(false);
				toggleAirplaneMode(false);
				final SharedPreferences.Editor ed = getSharedPreferences(SERVICE_IS_RUNNING,
						Context.MODE_PRIVATE).edit();
				ed.putBoolean(SERVICE_IS_RUNNING, false);
				ed.commit();
				return null;
			}
		}.execute();

		super.onDestroy();
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				synchronized (gravity) {

					if (waitForSensorsToWarmUp < 5) {
						if (waitForSensorsToWarmUp == 4) {
							waitForSensorsToWarmUp++;
							try {
								updateTimer.scheduleAtFixedRate(new UpdateTimerTask(),
										updateInterval, updateInterval);
							} catch (IllegalStateException ise) {
								// user stopped monitoring really quickly after
								// starting.
							}
							gravity[0] = event.values[0];
							gravity[1] = event.values[1];
							gravity[2] = event.values[2];
						}
						waitForSensorsToWarmUp++;
						return;
					}

					gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
					gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
					gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

					final double curX = event.values[0] - gravity[0];
					final double curY = event.values[1] - gravity[1];
					final double curZ = event.values[2] - gravity[2];

					final double mAccelCurrent = Math.sqrt(curX * curX + curY * curY + curZ * curZ);

					final double absAccel = Math.abs(mAccelCurrent);
					maxNetForce = absAccel > maxNetForce ? absAccel : maxNetForce;
				}
			}
		}).start();
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent != null && startId == 1) {
			testModeRate = intent.getIntExtra("testModeRate", Integer.MIN_VALUE);

			updateInterval = testModeRate == Integer.MIN_VALUE ? intent.getIntExtra("interval",
					INTERVAL) : testModeRate;

			sensorDelay = intent.getIntExtra(StartSleepReceiver.EXTRA_SENSOR_DELAY,
					SensorManager.SENSOR_DELAY_FASTEST);

			alarmTriggerSensitivity = intent.getDoubleExtra(StartSleepReceiver.EXTRA_ALARM,
					SettingsActivity.DEFAULT_ALARM_SENSITIVITY);

			useAlarm = intent.getBooleanExtra(StartSleepReceiver.EXTRA_USE_ALARM, false);
			alarmWindow = intent.getIntExtra(StartSleepReceiver.EXTRA_ALARM_WINDOW, 0);

			airplaneMode = intent.getBooleanExtra(StartSleepReceiver.EXTRA_AIRPLANE_MODE, false);
			silentMode = intent.getBooleanExtra(StartSleepReceiver.EXTRA_SILENT_MODE, false);

			forceScreenOn = intent.getBooleanExtra(StartSleepReceiver.EXTRA_FORCE_SCREEN_ON, false);

			startForeground(NOTIFICATION_ID, createServiceNotification());

			obtainWakeLock();

			registerAccelerometerListener();

			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					toggleSilentMode(true);
					toggleAirplaneMode(true);

					if (!alreadyDeletedResidualFile) {
						// TODO: doesn't happen more than once? right?
						deleteFile(SleepMonitoringService.SLEEP_DATA);
						alreadyDeletedResidualFile = true;
					}
					final SharedPreferences.Editor ed = getSharedPreferences(SERVICE_IS_RUNNING,
							Context.MODE_PRIVATE).edit();
					ed.putBoolean(SERVICE_IS_RUNNING, true);
					ed.commit();
					return null;
				}
			}.execute();
		}
		return startId;
	}

	private void registerAccelerometerListener() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), sensorDelay);
	}

	private void toggleAirplaneMode(final boolean enabling) {
		if (airplaneMode) {
			Settings.System.putInt(getContentResolver(), Settings.System.AIRPLANE_MODE_ON,
					enabling ? 1 : 0);
			final Intent intent = new Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED);
			intent.putExtra("state", enabling);
			sendBroadcast(intent);
		}
	}

	private void toggleSilentMode(final boolean enabling) {
		if (silentMode) {
			final AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			if (enabling) {
				ringerModeBackup = audioManager.getRingerMode();
				audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
			} else {
				audioManager.setRingerMode(ringerModeBackup);
			}
		}
	}

	private void triggerAlarmIfNecessary(final long currentTime, final double y) {
		if (useAlarm && y >= alarmTriggerSensitivity) {
			// TODO: stop calling calculateNextAlert here... battery waster
			final Alarm alarm = Alarms.calculateNextAlert(this);
			if (alarm != null) {
				final Calendar alarmTime = Calendar.getInstance();
				alarmTime.setTimeInMillis(alarm.time);
				alarmTime.add(Calendar.MINUTE, alarmWindow * -1);
				final long alarmMillis = alarmTime.getTimeInMillis();
				if (currentTime >= alarmMillis) {
					final SharedPreferences alarmPrefs = getSharedPreferences(
							SettingsActivity.PREFERENCES, 0);
					final int id = alarmPrefs.getInt(Alarms.PREF_SNOOZE_ID, -1);
					// if not already snoozing off ANY alarm, trigger the
					// alarm
					if (id == -1) {
						// add 1 second delay to make it less likely that we
						// skip the alarm
						Alarms.enableAlert(this, alarm, System.currentTimeMillis() + 1000);
					}
				}
			}
		}
	}

	private void unregisterAccelerometerListener() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
	}
}
