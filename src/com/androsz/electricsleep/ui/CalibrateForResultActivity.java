package com.androsz.electricsleep.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.widget.ProgressBar;

import com.androsz.electricsleep.service.SleepAccelerometerService;

public class CalibrateForResultActivity extends Activity {

	private PowerManager powerManager;
	private WakeLock partialWakeLock;

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CalibrateForResultActivity.this.setResult((int) intent
					.getDoubleExtra("y", 0));
			finish();
		}
	};

	private void obtainWakeLock() {
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		partialWakeLock = powerManager.newWakeLock(
				PowerManager.ON_AFTER_RELEASE
						| PowerManager.SCREEN_DIM_WAKE_LOCK, toString());
		partialWakeLock.acquire();
	}

	@Override
	public void onBackPressed() {
		setFailed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//getParent().onConfigurationChanged(newConfig);
		// do nothing to prevent onWindowFocusChanged to be called and
		// subsequent failure of the test
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ProgressBar progress = new ProgressBar(this);
		progress.setIndeterminate(false);
		setContentView(progress);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(updateChartReceiver);
		partialWakeLock.release();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(updateChartReceiver, new IntentFilter(
				SleepActivity.UPDATE_CHART));
		obtainWakeLock();
	}

	/*@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			setFailed();
		}
	}*/

	private void setFailed() {
		this.setResult(-0x1337);
		stopService(new Intent(this, SleepAccelerometerService.class));
		finish();
	}
}