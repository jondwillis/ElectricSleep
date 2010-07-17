package com.androsz.electricsleep.ui;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

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

	private static boolean started = false;

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
		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_export);
		
		if (!started) {
			registerReceiver(chartUpdateReceiver,
					new IntentFilter(UPDATE_CHART));

			startService(new Intent(this, SleepAccelerometerService.class));

			String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
			XYSeries series = new XYSeries(seriesTitle);
			mDataset.addSeries(series);
			mCurrentSeries = series;
			XYSeriesRenderer renderer = new XYSeriesRenderer();
			renderer.setFillBelowLine(true);
			renderer.setFillBelowLineColor(R.color.background_transparent_lighten);
			renderer.setColor(R.color.title_separator);
			 //mRenderer.setBackgroundColor(R.color.background_transparent_darken);
			//	mRenderer.setApplyBackgroundColor(true);
			mRenderer.setShowLegend(false);
			mRenderer.setLabelsTextSize(20f);
			mRenderer.setAntialiasing(true);
			mRenderer.setYLabels(0);
			mRenderer.setYAxisMax(1f);
			mRenderer.setYAxisMin(0f);
			//mRenderer.setXAxisMax(System.currentTimeMillis()+(1000*60*60));
			mRenderer.setYTitle("Movement level during sleep");
			//mRenderer.setShowGrid(true);\
			mRenderer.setChartTitleTextSize(20f);
			mRenderer.addSeriesRenderer(renderer);
			mCurrentRenderer = renderer;

			started = true;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		started = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		if (mChartView != null) {
			mChartView.repaint();
		} else {
			LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
			mChartView = ChartFactory.getTimeChartView(this, mDataset,
					mRenderer, "hh:mm a");
			layout.addView(mChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	public void onTitleButton1Click(View v) {
		stopService(new Intent(this, SleepAccelerometerService.class));
	}

	@Override
	public int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	private BroadcastReceiver chartUpdateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent != null) {
				float movement = intent.getFloatExtra("movement", 0f);
				mCurrentSeries.add(System.currentTimeMillis(), movement);
				if (mChartView != null && hasWindowFocus()) {
					mChartView.repaint();
				}
			}
		}
	};
}
