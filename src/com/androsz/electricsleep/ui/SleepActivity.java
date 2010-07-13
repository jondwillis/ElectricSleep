package com.androsz.electricsleep.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.chart.SleepMovementChart;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class SleepActivity extends CustomTitlebarActivity {

	public static final String UPDATE_CHART = "com.androsz.electricsleep.UPDATE_CHART";

	private SleepMovementChart sleepMovementChart = new SleepMovementChart();

	@Override
	public int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_sleep;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.startService(new Intent(this, SleepAccelerometerService.class));

		this.registerReceiver(chartUpdateReceiver, new IntentFilter(
				UPDATE_CHART));
	}

	private BroadcastReceiver chartUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			int level = intent.getIntExtra("level", 0);

			sleepMovementChart.execute(context);

			// contentTxt.setText(String.valueOf(level) + "%");
		}
	};
}
