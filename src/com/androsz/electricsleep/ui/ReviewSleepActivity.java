package com.androsz.electricsleep.ui;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.achartengine.ChartFactory;
import com.androsz.electricsleep.achartengine.GraphicalView;
import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;
import com.androsz.electricsleep.db.SleepHistoryDatabase;

public class ReviewSleepActivity extends CustomTitlebarActivity {

	private XYMultipleSeriesDataset xyMultipleSeriesDataset;

	private XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

	private XYSeries xySeriesMovement;

	private XYSeriesRenderer xySeriesMovementRenderer;

	private XYSeries xySeriesAlarmTrigger;

	private XYSeriesRenderer xySeriesAlarmTriggerRenderer;

	private GraphicalView chartGraphicalView;

	private ProgressDialog waitForSeriesData;

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_sleep;
	}

	@SuppressWarnings("unchecked")
	protected void onCreate(Bundle savedInstanceState) {

		Uri uri = getIntent().getData();
		Cursor cursor = managedQuery(uri, null, null, null, null);

		if (cursor == null) {
			finish();
		} else {
			cursor.moveToFirst();

			int dateTime = cursor.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATE_TIME);
			int x = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_X);
			int y = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_Y);
			int min = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MIN);
			int max = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MAX);
			int alarm = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_ALARM);
			
			this.setTitle("Sleep: " + cursor.getString(dateTime));
			super.onCreate(savedInstanceState);
			buildChart();
			try {
				xySeriesMovement.mX = (List<Double>) SleepHistoryDatabase
						.byteArrayToObject(cursor.getBlob(x));
				xySeriesMovement.mY = (List<Double>) SleepHistoryDatabase
						.byteArrayToObject(cursor.getBlob(y));
			} catch (StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			addChartView();
			redrawChart(cursor.getInt(min), cursor.getInt(max), cursor.getInt(alarm));
		}
	}

	private void addChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (layout.getChildCount() == 0) {
			chartGraphicalView = ChartFactory
					.getTimeChartView(this, xyMultipleSeriesDataset,
							xyMultipleSeriesRenderer, "h:mm a");
			layout.addView(chartGraphicalView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	private void buildChart() {
		if (xySeriesMovement == null) {

			// set up sleep movement series/renderer
			xySeriesMovement = new XYSeries("sleep");
			xySeriesMovementRenderer = new XYSeriesRenderer();
			xySeriesMovementRenderer.setFillBelowLine(true);
			xySeriesMovementRenderer.setFillBelowLineColor(getResources()
					.getColor(R.color.primary1));
			xySeriesMovementRenderer.setColor(Color.TRANSPARENT);

			// set up alarm trigger series/renderer
			xySeriesAlarmTrigger = new XYSeries("alarmTrigger");
			xySeriesAlarmTriggerRenderer = new XYSeriesRenderer();
			xySeriesAlarmTriggerRenderer.setFillBelowLine(true);
			xySeriesAlarmTriggerRenderer.setFillBelowLineColor(getResources()
					.getColor(R.color.background_transparent_lighten));
			xySeriesAlarmTriggerRenderer.setColor(Color.TRANSPARENT);

			// add series to the dataset
			xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
			xyMultipleSeriesDataset.addSeries(xySeriesMovement);
			xyMultipleSeriesDataset.addSeries(xySeriesAlarmTrigger);

			// set up the dataset renderer
			xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
			xyMultipleSeriesRenderer
					.addSeriesRenderer(xySeriesMovementRenderer);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(xySeriesAlarmTriggerRenderer);
			xyMultipleSeriesRenderer.setShowLegend(false);
			xyMultipleSeriesRenderer.setAxisTitleTextSize(22);
			xyMultipleSeriesRenderer.setLabelsTextSize(17);
			xyMultipleSeriesRenderer.setAntialiasing(true);
			final Display defaultDisplay = getWindowManager()
					.getDefaultDisplay();
			if (defaultDisplay.getWidth() > defaultDisplay.getHeight()) {
				// landscape
				xyMultipleSeriesRenderer.setXLabels(10);
			} else {
				// portrait
				xyMultipleSeriesRenderer.setXLabels(5);
			}
			xyMultipleSeriesRenderer.setYLabels(0);
			xyMultipleSeriesRenderer.setYTitle("Movement level during sleep");
			xyMultipleSeriesRenderer.setShowGrid(true);
			xyMultipleSeriesRenderer.setAxesColor(getResources().getColor(
					R.color.text));
			xyMultipleSeriesRenderer.setLabelsColor(xyMultipleSeriesRenderer
					.getAxesColor());
		}
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);

		xyMultipleSeriesDataset = (XYMultipleSeriesDataset) savedState
				.getSerializable("dataset");
		xyMultipleSeriesRenderer = (XYMultipleSeriesRenderer) savedState
				.getSerializable("renderer");

		xySeriesMovement = (XYSeries) savedState
				.getSerializable("seriesMovement");
		xySeriesMovementRenderer = (XYSeriesRenderer) savedState
				.getSerializable("rendererMovement");

		xySeriesAlarmTrigger = (XYSeries) savedState
				.getSerializable("seriesAlarmTrigger");
		xySeriesAlarmTriggerRenderer = (XYSeriesRenderer) savedState
				.getSerializable("rendererAlarmTrigger");
	}

	@Override
	protected void onResume() {
		super.onResume();
		addChartView();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable("dataset", xyMultipleSeriesDataset);
		outState.putSerializable("renderer", xyMultipleSeriesRenderer);

		outState.putSerializable("seriesMovement", xySeriesMovement);
		outState.putSerializable("rendererMovement", xySeriesMovementRenderer);

		outState.putSerializable("seriesAlarmTrigger", xySeriesAlarmTrigger);
		outState.putSerializable("rendererAlarmTrigger",
				xySeriesAlarmTriggerRenderer);
	}

	private void redrawChart(int min, int max, int alarm) {
		if (xySeriesMovement.mX.size() > 1 && xySeriesMovement.mY.size() > 1) {
			if (waitForSeriesData != null) {
				waitForSeriesData.dismiss();
				waitForSeriesData = null;
			}

			final double firstX = xySeriesMovement.mX.get(0);
			final double lastX = xySeriesMovement.mX.get(xySeriesMovement.mX
					.size() - 1);
			xyMultipleSeriesRenderer.setXAxisMin(firstX);
			xyMultipleSeriesRenderer.setXAxisMax(lastX);

			xyMultipleSeriesRenderer.setYAxisMin(min);
			xyMultipleSeriesRenderer.setYAxisMax(max);

			// reconfigure the alarm trigger line..
			xySeriesAlarmTrigger.clear();

			xySeriesAlarmTrigger.add(firstX, alarm);
			xySeriesAlarmTrigger.add(lastX, alarm);

			chartGraphicalView.repaint();
		}
	}
}
