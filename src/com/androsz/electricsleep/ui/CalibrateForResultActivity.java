package com.androsz.electricsleep.ui;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.androsz.electricsleep.service.SleepAccelerometerService;

public class CalibrateForResultActivity extends Activity {

	public static final int CALIBRATION_FAILED = -0x1337;

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
		// getParent().onConfigurationChanged(newConfig);
		// do nothing to prevent onWindowFocusChanged to be called and
		// subsequent failure of the test
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final LinearLayout container = new LinearLayout(this);
		final ProgressBar progress = new ProgressBar(this);

		container.addView(progress, 72, 72);
		setContentView(container);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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

	/*
	 * @Override public void onWindowFocusChanged(boolean hasFocus) { if
	 * (!hasFocus) { setFailed(); } }
	 */

	private void setFailed() {
		this.setResult(CALIBRATION_FAILED);
		stopService(new Intent(this, SleepAccelerometerService.class));
		finish();
	}
}