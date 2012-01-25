package com.androsz.electricsleepbeta.app;

import android.app.Notification;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.wizard.CalibrationWizardActivity;
import com.androsz.electricsleepbeta.util.WakeLockManager;

public class CheckForScreenBugAccelerometerService extends Service implements SensorEventListener {

	private final class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
				serviceHandler.postDelayed(setScreenIsOffRunnable, 6666);
				serviceHandler.postDelayed(turnScreenOnFallbackRunnable, 10000);
			}
		}
	}

	public static final String BUG_NOT_PRESENT = "BUG_NOT_PRESENT";

	public static final String BUG_PRESENT = "BUG_PRESENT";

	private static final int NOTIFICATION_ID = 0x1337b;

	private boolean bugPresent = true;
	boolean didNotTurnScreenOn = true;
	private PowerManager powerManager;
	private boolean screenIsOff = false;

	private ScreenReceiver screenOnOffReceiver;
	Handler serviceHandler = new Handler();

	Runnable setScreenIsOffRunnable = new Runnable() {
		@Override
		public void run() {
			screenIsOff = true;
		}
	};

	Runnable turnScreenOnFallbackRunnable = new Runnable() {
		@Override
		public void run() {
			if (bugPresent) {
				CalibrationWizardActivity.BUG_PRESENT_INTENT = new Intent(BUG_PRESENT);
			}
			turnScreenOn();
		}
	};
	

	private Notification createServiceNotification() {
		final int icon = R.drawable.icon_small;
		final CharSequence tickerText = getString(R.string.notification_screenbug_ticker);
		final long when = System.currentTimeMillis();

		final Notification notification = new Notification(icon, tickerText, when);

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		final CharSequence contentTitle = getString(R.string.notification_sleep_title);
		final CharSequence contentText = getString(R.string.notification_sleep_text);
		Intent notificationIntent = new Intent();
		
		notificationIntent
				.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		final PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				0);

		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);

		return notification;
	}

	@Override
	public void onAccuracyChanged(final Sensor sensor, final int accuracy) {
		// Not used.
	}

	@Override
	public IBinder onBind(final Intent intent) {
		// Not used
		return null;
	}

	@Override
	public void onDestroy() {

		unregisterAccelerometerListener();

		unregisterReceiver(screenOnOffReceiver);

		serviceHandler.removeCallbacks(setScreenIsOffRunnable);
		serviceHandler.removeCallbacks(turnScreenOnFallbackRunnable);

		// check here so that certain devices keep their screen on for at least
		// 5 seconds (from turnScreenOn)
		if (didNotTurnScreenOn) {
			WakeLockManager.release("screenBugPartial");
			WakeLockManager.release("screenBugDim");
		}
		super.onDestroy();
	}

	@Override
	public void onSensorChanged(final SensorEvent event) {
		if (!powerManager.isScreenOn() && screenIsOff && bugPresent) {
			CalibrationWizardActivity.BUG_PRESENT_INTENT = new Intent(BUG_NOT_PRESENT);
			turnScreenOn();
		}
	}

	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (startId == 1) {
			bugPresent = true;
			screenIsOff = false;
			final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			screenOnOffReceiver = new ScreenReceiver();
			registerReceiver(screenOnOffReceiver, filter);
			powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
			startForeground(NOTIFICATION_ID, createServiceNotification());
			WakeLockManager.acquire(this, "screenBugPartial", PowerManager.PARTIAL_WAKE_LOCK);
			registerAccelerometerListener();
		}
		return startId;
	}

	private void registerAccelerometerListener() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		sensorManager.registerListener(this,
				sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	private void turnScreenOn() {
		didNotTurnScreenOn = false;
		WakeLockManager.acquire(this, "screenBugDim", PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, 5000);
		stopSelf();
	}

	private void unregisterAccelerometerListener() {
		final SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(this);
	}
}
