package com.androsz.electricsleep.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.ui.SleepActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.text.format.DateFormat;

public class SleepAccelerometerService extends Service implements
		SensorEventListener {
	public static final String POKE_UPDATE_CHART = "com.androsz.electricsleep.POKE_UPDATE_CHART";
	private final int notificationId = 0x1337;

	public ArrayList<Double> currentSeriesX = new ArrayList<Double>();
	public ArrayList<Double> currentSeriesY = new ArrayList<Double>();

	private SensorManager sensorManager;
	private Sensor accelerometer;

	private PowerManager powerManager;
	private WakeLock partialWakeLock;

	private NotificationManager notificationManager;

	// private UpdateSensorRunnable updateSensorRunnable;
	// private Handler serviceHandler;

	public void onCreate() {
		super.onCreate();

		registerReceiver(pokeUpdateChartReceiver, new IntentFilter(
				POKE_UPDATE_CHART));

		registerAccelerometerListener();

		obtainWakeLock();

		createNotification();
	}

	private void registerAccelerometerListener() {
		// Obtain a reference to system-wide sensor event manager.
		sensorManager = (SensorManager) getApplicationContext()
				.getSystemService(Context.SENSOR_SERVICE);

		// Get the default sensor for accel
		accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		// Register for events.
		sensorManager.registerListener(this, accelerometer, SENSOR_DELAY);
	}

	private void createNotification() {
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
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
				notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText,
				contentIntent);

		notificationManager.notify(notificationId, notification);
	}

	private void obtainWakeLock() {
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		partialWakeLock = powerManager.newWakeLock(
				PowerManager.PARTIAL_WAKE_LOCK, toString());
		partialWakeLock.acquire();
	}

	public void onDestroy() {
		super.onDestroy();

		unregisterReceiver(pokeUpdateChartReceiver);

		// Unregister from SensorManager.
		sensorManager.unregisterListener(this);

		partialWakeLock.release();

		notificationManager.cancel(notificationId);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	private float netForce = 0;
	private int netForceAdded = 0;
	private long lastChartUpdateTime = System.currentTimeMillis();
	private long lastOnSensorChangedTime = System.currentTimeMillis();
	private double gravity = SensorManager.STANDARD_GRAVITY;
	private long totalTimeBetweenSensorChanges = 0;
	private int totalNumberOfSensorChanges = 0;

	@Override
	public void onSensorChanged(SensorEvent event) {

		long currentTime = System.currentTimeMillis();
		long timeSinceLastSensorChange = currentTime - lastOnSensorChangedTime;

		totalNumberOfSensorChanges++;
		totalTimeBetweenSensorChanges += timeSinceLastSensorChange;

		long deltaTime = currentTime - lastChartUpdateTime;
		if (deltaTime > UPDATE_FREQUENCY) {
			double averageTimeBetweenUpdates = totalTimeBetweenSensorChanges
					/ totalNumberOfSensorChanges;
			currentSeriesX.add((double) currentTime);
			currentSeriesY.add((double) deltaTime / averageTimeBetweenUpdates);

			Intent i = new Intent(SleepActivity.UPDATE_CHART);
			i.putExtra("currentSeriesX", currentSeriesX);
			i.putExtra("currentSeriesY", currentSeriesY);
			sendBroadcast(i);

			totalNumberOfSensorChanges = 0;
			totalTimeBetweenSensorChanges = 0;

			lastChartUpdateTime = currentTime;
		}

		lastOnSensorChangedTime = currentTime;

		/*
		 * double x = Math.pow(event.values[0], 2); // X axis double y =
		 * Math.pow(event.values[1], 2); // Y axis double z =
		 * Math.pow(event.values[2], 2); // Z axis double force =
		 * Math.sqrt(x+y+z)-gravity;
		 * 
		 * netForce += java.lang.Math.abs(force); netForceAdded++;
		 * 
		 * long deltaTime = currentTime - lastChartUpdateTime; if (deltaTime >
		 * UPDATE_FREQUENCY) { Intent i = new
		 * Intent(SleepActivity.UPDATE_CHART); currentSeriesX.add((double)
		 * currentTime); currentSeriesY.add((double)
		 * 0);//java.lang.Math.min((netForceAdded
		 * /((float)(currentTime-lastOnSensorChangedTime))), MAX_SENSITIVITY));
		 * i.putExtra("currentSeriesX", currentSeriesX);
		 * i.putExtra("currentSeriesY", currentSeriesY);
		 * 
		 * sendBroadcast(i); //netForce = 0; //netForceAdded = 0;
		 * lastChartUpdateTime = currentTime; } lastOnSensorChangedTime =
		 * currentTime;
		 */
	}

	public static final int SENSOR_DELAY = SensorManager.SENSOR_DELAY_UI;
	public static final long UPDATE_FREQUENCY = 10000;
	public static final float MAX_SENSITIVITY = 100;// UPDATE_FREQUENCY / 100;//

	// (float) (UPDATE_FREQUENCY
	// /

	// (100f * (SENSOR_DELAY +
	// 1)));

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private BroadcastReceiver pokeUpdateChartReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent != null) {
				// ONLY update if we have series data. prevents crash on new
				// service startup
				if (currentSeriesX.size() > 0 && currentSeriesY.size() > 0) {
					Intent i = new Intent(SleepActivity.UPDATE_CHART);
					i.putExtra("currentSeriesX", currentSeriesX);
					i.putExtra("currentSeriesY", currentSeriesY);
					sendBroadcast(i);
				}
			}
		}
	};

}
