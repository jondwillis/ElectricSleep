package com.androsz.electricsleep.ui.view;

import java.io.Serializable;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.View;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.achartengine.ChartView;
import com.androsz.electricsleep.achartengine.chart.AbstractChart;
import com.androsz.electricsleep.achartengine.chart.TimeChart;
import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;

public class SleepChartView extends ChartView implements Serializable {

	public XYMultipleSeriesDataset xyMultipleSeriesDataset;

	public XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

	public XYSeries xySeriesMovement;

	public XYSeriesRenderer xySeriesMovementRenderer;

	public XYSeries xySeriesAlarmTrigger;

	public XYSeriesRenderer xySeriesAlarmTriggerRenderer;

	public SleepChartView(Context context) {
		super(context);
	}

	public boolean makesSense() {
		return xySeriesMovement.getItemCount() > 1;
	}

	protected void redraw(int min, int max, int alarm) {

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

	public void syncByCopying(List<Double> x, List<Double> y, int min, int max,
			int alarm) {
		xySeriesMovement.mX = x;
		xySeriesMovement.mY = y;
		redraw(min, max, alarm);
	}

	public void syncByAdding(Double x, Double y, int min, int max, int alarm) {
		xySeriesMovement.mX.add(x);
		xySeriesMovement.mY.add(y);
		redraw(min, max, alarm);
	}

	public AbstractChart buildChart() {
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
			TimeChart timeChart = new TimeChart(xyMultipleSeriesDataset,
					xyMultipleSeriesRenderer);
			timeChart.setDateFormat("h:mm a");
			return timeChart;
		}
		return null;
	}
}
