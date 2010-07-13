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
	private Notification notification;

	public void onCreate() {
		super.onCreate();
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		partialWakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK,
				"Electric Sleep Accelerometer WakeLock");

		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

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
				SensorManager.SENSOR_DELAY_NORMAL);

		return super.onStartCommand(intent, flags, startId);
	}

	private void notifyMovement(String poop) {
		int icon = R.drawable.icon;
		CharSequence tickerText = poop;
		long when = System.currentTimeMillis();

		Notification notification = new Notification(icon, tickerText, when);

		Context context = getApplicationContext();
		CharSequence contentTitle = poop;
		CharSequence contentText = poop;
		Intent notificationIntent = new Intent(this, SleepActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notificationManager.notify(1, notification);
	}

	public void onDestroy() {
		super.onDestroy();

		// Unregister from SensorManager.
		sensorManager.unregisterListener(this);

		partialWakeLock.release();
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	private float[] lastAccel = { 0, 0, 0 };

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			return;

		float[] currentAccel = event.values;

		/*float diff1 = java.lang.Math.abs(currentAccel[0] - lastAccel[0]);
		float diff2 = java.lang.Math.abs(currentAccel[1] - lastAccel[1]);
		float diff3 = java.lang.Math.abs(currentAccel[2] - lastAccel[2]);
		float sensitivity = 0.01f;
		if (diff1 > sensitivity || diff2 > sensitivity || diff3 > sensitivity)
			notifyMovement(diff1 + " | " + diff2 + " | " + diff3);
		lastAccel = currentAccel;*/
		try {
			PendingIntent.getBroadcast(
					getApplicationContext(),
					0,
					new Intent(SleepActivity.UPDATE_CHART),
					0).send();
		} catch (CanceledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
