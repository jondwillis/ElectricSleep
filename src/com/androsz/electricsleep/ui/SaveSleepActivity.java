package com.androsz.electricsleep.ui;

import java.io.IOException;
import java.util.ArrayList;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.service.SleepAccelerometerService;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

public class SaveSleepActivity extends CustomTitlebarActivity {

	public static final String SAVE_SLEEP = "com.androsz.electricsleep.SAVE_SLEEP";
	@Override
	// @SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {


		registerReceiver(saveSleepReceiver, new IntentFilter(
			SAVE_SLEEP));
		
		new AlertDialog.Builder(this)
				.setMessage("Do you wish to save this sleep history?")
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								sendBroadcast(new Intent(SleepAccelerometerService.POKE_SAVE_SLEEP));
								stopService(new Intent(SaveSleepActivity.this, SleepAccelerometerService.class));
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//homeactivity with new top
						stopService(new Intent(SaveSleepActivity.this, SleepAccelerometerService.class));
						dialog.cancel();
						return;
					}
				}).show();
		super.onCreate(savedInstanceState);
	}
	
	protected void onDestroy()
	{
		unregisterReceiver(saveSleepReceiver);
		
		super.onDestroy();
	}

	private final BroadcastReceiver saveSleepReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			//shdb.addSleep(
					// sdf.format(dateStarted) + " to " + sdf2.format(now),
					// currentSeriesX, currentSeriesY, minSensitivity,
					// maxSensitivity, alarmTriggerSensitivity);
			final SleepHistoryDatabase shdb = new SleepHistoryDatabase(
					SaveSleepActivity.this);
			
			try {
				long result = shdb.addSleep(SaveSleepActivity.this, intent.getStringExtra("name"),
				(ArrayList<Double>)intent.getSerializableExtra("currentSeriesX"),
				(ArrayList<Double>)intent.getSerializableExtra("currentSeriesY"),
				intent.getIntExtra("min", 0),
				intent.getIntExtra("max", 100),
				intent.getIntExtra("alarm", -1));
				
				final Intent reviewSleepIntent = new Intent(
						getApplicationContext(), ReviewSleepActivity.class);
				
				final Uri data = Uri.withAppendedPath(
						SleepContentProvider.CONTENT_URI, String
								.valueOf(result));
				reviewSleepIntent.setData(data);
				
				reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
						| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
				
				startActivity(reviewSleepIntent);
			} catch (IOException e) {
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
}
