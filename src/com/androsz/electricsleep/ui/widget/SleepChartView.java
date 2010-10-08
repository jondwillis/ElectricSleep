package com.androsz.electricsleep.ui.widget;

import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
<<<<<<< .working
import android.util.AttributeSet;
=======
>>>>>>> .merge-right.r52

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.achartengine.ChartView;
import com.androsz.electricsleep.achartengine.chart.AbstractChart;
import com.androsz.electricsleep.achartengine.chart.TimeChart;
import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;
import com.androsz.electricsleep.db.SleepHistoryDatabase;

public class SleepChartView extends ChartView implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5692853786456847694L;

	public XYMultipleSeriesDataset xyMultipleSeriesDataset;

	public XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

	public XYSeries xySeriesMovement;

	public XYSeriesRenderer xySeriesMovementRenderer;

	public XYSeries xySeriesAlarmTrigger;

	public XYSeriesRenderer xySeriesAlarmTriggerRenderer;

	public SleepChartView(final Context context) {
		super(context);
	}

<<<<<<< .working
	public SleepChartView(final Context context, AttributeSet as) {
		super(context, as);
	}

	@Override
	protected AbstractChart buildChart() {
=======
	@Override
	public AbstractChart buildChart() {
>>>>>>> .merge-right.r52
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
			// TODO move this?
			/*
			 * final Display defaultDisplay =
			 * super.getContext().getWindowManager() .getDefaultDisplay(); if
			 * (defaultDisplay.getWidth() > defaultDisplay.getHeight()) { //
			 * landscape xyMultipleSeriesRenderer.setXLabels(10); } else { //
			 * portrait xyMultipleSeriesRenderer.setXLabels(5); }
			 */
			xyMultipleSeriesRenderer.setYLabels(0);
			xyMultipleSeriesRenderer.setYTitle(super.getContext().getString(
					R.string.movement_level_during_sleep));
			xyMultipleSeriesRenderer.setShowGrid(true);
			xyMultipleSeriesRenderer.setAxesColor(getResources().getColor(
					R.color.text));
			xyMultipleSeriesRenderer.setLabelsColor(xyMultipleSeriesRenderer
					.getAxesColor());
			final TimeChart timeChart = new TimeChart(xyMultipleSeriesDataset,
					xyMultipleSeriesRenderer);
			timeChart.setDateFormat("h:mm a");
			return timeChart;
		}
		return null;
	}
<<<<<<< .working

	public boolean makesSenseToDisplay() {
		return xySeriesMovement.getItemCount() > 1;
	}

	protected void redraw(final int min, final int max, final int alarm) {
		if (makesSenseToDisplay()) {
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

			repaint();
		}
	}

	public void syncByAdding(final Double x, final Double y, final int min,
			final int max, final int alarm) {
		xySeriesMovement.mX.add(x);
		xySeriesMovement.mY.add(y);
		redraw(min, max, alarm);
	}

	public void syncByCopying(final List<Double> x, final List<Double> y,
			final int min, final int max, final int alarm) {
		xySeriesMovement.mX = x;
		xySeriesMovement.mY = y;
		redraw(min, max, alarm);
	}
	
	public void syncWithCursor(final Cursor cursor)
	{
		final String name = cursor.getString(cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATE_TIME));
		
		try {

			xySeriesMovement.mX = (List<Double>) SleepHistoryDatabase.byteArrayToObject(cursor.getBlob(cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_X)));

			xySeriesMovement.mY = (List<Double>) SleepHistoryDatabase.byteArrayToObject(cursor.getBlob(cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_Y)));

		} catch (final StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		final int min = cursor.getInt(cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MIN));
		final int max = cursor.getInt(cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MAX));
		final int alarm = cursor.getInt(cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_ALARM));
		
		redraw(min, max, alarm);
	}
}=======

	public boolean makesSense() {
		return xySeriesMovement.getItemCount() > 1;
	}

	protected void redraw(final int min, final int max, final int alarm) {

		if (makesSense()) {
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

			repaint();
		}
	}

	public void syncByAdding(final Double x, final Double y, final int min,
			final int max, final int alarm) {
		xySeriesMovement.mX.add(x);
		xySeriesMovement.mY.add(y);
		redraw(min, max, alarm);
	}

	public void syncByCopying(final List<Double> x, final List<Double> y,
			final int min, final int max, final int alarm) {
		xySeriesMovement.mX = x;
		xySeriesMovement.mY = y;
		redraw(min, max, alarm);
	}
}
>>>>>>> .merge-right.r52
