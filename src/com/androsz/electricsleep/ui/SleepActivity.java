package com.androsz.electricsleep.ui;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.chart.SleepMovementChart;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class SleepActivity extends CustomTitlebarActivity {

	public static final String UPDATE_CHART = "com.androsz.electricsleep.UPDATE_CHART";

	public static final String TYPE = "type";

	private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();

	private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();

	private XYSeries mCurrentSeries;

	private XYSeriesRenderer mCurrentRenderer;

	private String mDateFormat;

	private GraphicalView mChartView;

	private boolean isStarted = false;

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		mDataset = (XYMultipleSeriesDataset) savedState
				.getSerializable("dataset");
		mRenderer = (XYMultipleSeriesRenderer) savedState
				.getSerializable("renderer");
		mCurrentSeries = (XYSeries) savedState
				.getSerializable("current_series");
		mCurrentRenderer = (XYSeriesRenderer) savedState
				.getSerializable("current_renderer");
		mDateFormat = savedState.getString("date_format");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("dataset", mDataset);
		outState.putSerializable("renderer", mRenderer);
		outState.putSerializable("current_series", mCurrentSeries);
		outState.putSerializable("current_renderer", mCurrentRenderer);
		outState.putString("date_format", mDateFormat);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!isStarted) {
			this.registerReceiver(chartUpdateReceiver, new IntentFilter(
					UPDATE_CHART));

			this
					.startService(new Intent(this,
							SleepAccelerometerService.class));

			String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
			XYSeries series = new XYSeries(seriesTitle);
			mDataset.addSeries(series);
			mCurrentSeries = series;
			XYSeriesRenderer renderer = new XYSeriesRenderer();
			mRenderer.addSeriesRenderer(renderer);
			mCurrentRenderer = renderer;
			isStarted = true;
		}

		if (mChartView == null) {
			LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
			mChartView = ChartFactory.getLineChartView(this, mDataset,
					mRenderer);
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		} else {
			mChartView.repaint();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	public int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	private BroadcastReceiver chartUpdateReceiver = new BroadcastReceiver() {

		private int count = 0;

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null && intent.hasExtra("accelValues")) {
				float[] sensorValues = intent.getFloatArrayExtra("accelValues");
				mCurrentSeries.add(count++, sensorValues[0]);
				if (mChartView != null && hasWindowFocus()) {
					mChartView.repaint();
				}
			}

			// startActivity(sleepMovementChart.execute(context));

			// contentTxt.setText(String.valueOf(level) + "%");
		}
	};
}
