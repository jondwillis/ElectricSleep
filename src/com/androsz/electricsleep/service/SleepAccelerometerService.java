package com.androsz.electricsleep.service;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.ui.SleepActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class SleepAccelerometerService extends Service implements
		SensorEventListener {

	private SensorManager sensorManager;
	private Sensor accelerometer;

	private PowerManager powerManager;
	private WakeLock partialWakeLock;

	private NotificationManager notificationManager;

	private UpdateSensorRunnable updateSensorRunnable;
	private Handler serviceHandler;

	public void onCreate() {
		super.onCreate();
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		partialWakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, toString());

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		int icon = R.drawable.icon;
		CharSequence tickerText = getText(R.string.notification_sleep_started);
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		notification.flags = Notification.FLAG_ONGOING_EVENT;

		Context context = getApplicationContext();
		CharSequence contentTitle = getText(R.string.notification_sleep_title);
		CharSequence contentText = getText(R.string.notification_sleep_text);
		Intent notificationIntent = new Intent(this, SleepActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notificationManager.notify(1, notification);

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		partialWakeLock.acquire();

		// Obtain a reference to system-wide sensor event manager.
		sensorManager = (SensorManager) getApplicationContext()
				.getSystemService(Context.SENSOR_SERVICE);

		// Get the default sensor for accel
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Register for events.
		sensorManager.registerListener(this, accelerometer,
				SensorManager.SENSOR_DELAY_GAME);

		updateSensorRunnable = new UpdateSensorRunnable();
		serviceHandler = new Handler();

		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy() {
		super.onDestroy();

		// Unregister from SensorManager.
		sensorManager.unregisterListener(this);

		partialWakeLock.release();

		serviceHandler.removeCallbacks(updateSensorRunnable);

		notificationManager.cancel(1);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	private float[] lastAccel = { 0, 0, 0 };
	private float[] currentAccel = { 0, 0, 0 };
	private float diffTotal = 0;
	private long lastChartUpdateTime = 0;
	private long lastTime = System.currentTimeMillis();

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;

		currentAccel = event.values;
		serviceHandler.post(updateSensorRunnable);
	}

	private class UpdateSensorRunnable implements Runnable {
		@Override
		public void run() {
			long curTime = System.currentTimeMillis();
			long diffTime = curTime - lastTime;
			float rawDiff = (java.lang.Math.abs((currentAccel[0] - lastAccel[0])
					+ (currentAccel[1] - lastAccel[1])
					+ (currentAccel[2] - lastAccel[2])));
			
			diffTotal += (rawDiff * (diffTime));
lastAccel = currentAccel;
			lastTime = curTime;

			if ((curTime - lastChartUpdateTime) > UPDATE_FREQUENCY) {
				Intent i = new Intent(SleepActivity.UPDATE_CHART);
				i.putExtra("movement",
						java.lang.Math.min(diffTotal, MAX_SENSITIVITY));
				sendBroadcast(i);
				diffTotal = 0;
				lastChartUpdateTime = curTime;
			}
		}
	}

	public static final long UPDATE_FREQUENCY = 1000;
	public static final float MAX_SENSITIVITY = (float) (UPDATE_FREQUENCY/50f);

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
