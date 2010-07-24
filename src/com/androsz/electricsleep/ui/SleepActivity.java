package com.androsz.electricsleep.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chart.BarChart.Type;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class SleepActivity extends CustomTitlebarActivity {

	public static final String UPDATE_CHART = "com.androsz.electricsleep.UPDATE_CHART";
	public static final String SYNC_CHART = "com.androsz.electricsleep.SYNC_CHART";

	private XYMultipleSeriesDataset mDataset;

	private XYMultipleSeriesRenderer mRenderer;

	private XYSeries mCurrentSeries;

	private XYSeriesRenderer mCurrentRenderer;

	private GraphicalView mChartView;

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

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable("dataset", mDataset);
		outState.putSerializable("renderer", mRenderer);
		outState.putSerializable("current_series", mCurrentSeries);
		outState.putSerializable("current_renderer", mCurrentRenderer);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		startService(new Intent(this, SleepAccelerometerService.class));

		this.setTitle("Monitoring Sleep ("
				+ DateFormat.getDateFormat(this).format(new Date()) + ")");
		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_export);

		buildChart();
	}

	private void buildChart() {
		if (mCurrentSeries == null) {
			XYSeries series = new XYSeries("sleep");
			mCurrentSeries = series;

			mDataset = new XYMultipleSeriesDataset();
			mDataset.addSeries(mCurrentSeries);

			XYSeriesRenderer renderer = new XYSeriesRenderer();
			renderer.setFillBelowLine(true);
			renderer.setFillBelowLineColor(getResources().getColor(
					R.color.foreground1));
			renderer.setColor(Color.TRANSPARENT);
			mCurrentRenderer = renderer;

			mRenderer = new XYMultipleSeriesRenderer();
			mRenderer.addSeriesRenderer(mCurrentRenderer);
			mRenderer.setShowLegend(false);
			mRenderer.setAxisTitleTextSize(22);
			mRenderer.setLabelsTextSize(17);
			mRenderer.setAntialiasing(true);
			if (mRenderer.getOrientation() == Orientation.HORIZONTAL) {
				mRenderer.setXLabels(8);
			} else {
				mRenderer.setXLabels(4);
			}
			mRenderer.setYLabels(0);
			mRenderer.setYTitle("Movement level during sleep");
			mRenderer.setShowGrid(true);
			mRenderer.setAxesColor(Color.WHITE);
			mRenderer.setLabelsColor(Color.WHITE);
		}
	}

	private ProgressDialog waitForSeriesData;

	protected void onResume() {
		super.onResume();
		addChartView();
		registerReceiver(updateChartReceiver, new IntentFilter(UPDATE_CHART));
		registerReceiver(syncChartReceiver, new IntentFilter(SYNC_CHART));
		sendBroadcast(new Intent(SleepAccelerometerService.POKE_SYNC_CHART));
	}

	private void addChartView() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (layout.getChildCount() == 0) {
			waitForSeriesData = new ProgressDialog(this);
			waitForSeriesData
					.setMessage(getText(R.string.message_wait_for_sleep_data));
			waitForSeriesData.show();
			mChartView = ChartFactory.getTimeChartView(this, mDataset,
					mRenderer, "h:mm a");
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	protected void onPause() {
		unregisterReceiver(updateChartReceiver);
		unregisterReceiver(syncChartReceiver);
		super.onPause();
	}

	public void onTitleButton1Click(View v) {
		stopService(new Intent(this, SleepAccelerometerService.class));
	}

	@Override
	public int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	private void redrawChart() {
		if (mCurrentSeries.mX.size() > 1 && mCurrentSeries.mY.size() > 1) {
			waitForSeriesData.dismiss();

			mRenderer.setXAxisMin(mCurrentSeries.mX.get(0));
			mRenderer.setXAxisMax(mCurrentSeries.mX.get(mCurrentSeries.mX
					.size() - 1));
			mRenderer.setYAxisMax(SleepAccelerometerService.MAX_SENSITIVITY);
			mRenderer.setYAxisMin(1);

			mChartView.repaint();
		}
	}

	private BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mCurrentSeries.mX.add(intent.getDoubleExtra("x", 0));
			mCurrentSeries.mY.add(intent.getDoubleExtra("y", 0));

			redrawChart();
		}
	};

	private BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			mCurrentSeries.mX = ((List<Double>) intent
					.getSerializableExtra("currentSeriesX"));

			mCurrentSeries.mY = ((List<Double>) intent
					.getSerializableExtra("currentSeriesY"));

			redrawChart();
		}
	};
}
