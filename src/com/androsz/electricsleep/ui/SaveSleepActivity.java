package com.androsz.electricsleep.ui;

import java.io.IOException;
import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class SaveSleepActivity extends CustomTitlebarActivity {

	public static final String SAVE_SLEEP = "com.androsz.electricsleep.SAVE_SLEEP";

	private final BroadcastReceiver saveSleepReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(final Context context, final Intent intent) {
			// shdb.addSleep(
			// sdf.format(dateStarted) + " to " + sdf2.format(now),
			// currentSeriesX, currentSeriesY, minSensitivity,
			// maxSensitivity, alarmTriggerSensitivity);
			final SleepHistoryDatabase shdb = new SleepHistoryDatabase(
					SaveSleepActivity.this);

			try {
				final int min = intent.getIntExtra("min",
						SettingsActivity.DEFAULT_MIN_SENSITIVITY);
				final long result = shdb.addSleep(SaveSleepActivity.this,
						intent.getStringExtra("name"),
						(ArrayList<Double>) intent
								.getSerializableExtra("currentSeriesX"),
						(ArrayList<Double>) intent
								.getSerializableExtra("currentSeriesY"), min,
						intent.getIntExtra("max",
								SettingsActivity.DEFAULT_MAX_SENSITIVITY),
						intent.getIntExtra("alarm",
								SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

				final Intent reviewSleepIntent = new Intent(
						getApplicationContext(), ReviewSleepActivity.class);

				final Uri data = Uri.withAppendedPath(
						SleepContentProvider.CONTENT_URI,
						String.valueOf(result));
				reviewSleepIntent.setData(data);

				reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_NEW_TASK);

				startActivity(reviewSleepIntent);
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	};

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_save;
	}

	@Override
	// @SuppressWarnings("unchecked")
	protected void onCreate(final Bundle savedInstanceState) {

		registerReceiver(saveSleepReceiver, new IntentFilter(SAVE_SLEEP));

		new AlertDialog.Builder(this)
				.setMessage("Do you wish to save this sleep history?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								sendBroadcast(new Intent(
										SleepAccelerometerService.POKE_SAVE_SLEEP));
								stopService(new Intent(SaveSleepActivity.this,
										SleepAccelerometerService.class));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int id) {
						final Intent intent = new Intent(SaveSleepActivity.this, HomeActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
						stopService(new Intent(SaveSleepActivity.this,
								SleepAccelerometerService.class));
						dialog.cancel();
						return;
					}
				}).show();
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(saveSleepReceiver);

		super.onDestroy();
	}
}
