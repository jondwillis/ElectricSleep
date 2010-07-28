package com.androsz.electricsleep.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.androsz.electricsleep.service.SleepAccelerometerService;

public class CalibrateForResultActivity extends Activity {

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			CalibrateForResultActivity.this.setResult((int) intent
					.getDoubleExtra("y", 0));
			finish();
		}
	};

	@Override
	public void onBackPressed() {
		setFailed();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		// do nothing to prevent onWindowFocusChanged to be called and
		// subsequent failure of the test
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ProgressBar progress = new ProgressBar(this);
		setContentView(progress);
	}

	@Override
	protected void onPause() {
		unregisterReceiver(updateChartReceiver);
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(updateChartReceiver, new IntentFilter(
				SleepActivity.UPDATE_CHART));
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (!hasFocus) {
			setFailed();
		}
	}

	private void setFailed() {
		this.setResult(-1);
		stopService(new Intent(this, SleepAccelerometerService.class));
		finish();
	}
}
