package com.androsz.electricsleepbeta.widget;

import java.io.Serializable;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.achartengine.ChartView;
import com.androsz.electricsleepbeta.achartengine.chart.AbstractChart;
import com.androsz.electricsleepbeta.achartengine.chart.TimeChart;
import com.androsz.electricsleepbeta.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleepbeta.achartengine.model.XYSeries;
import com.androsz.electricsleepbeta.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.XYSeriesRenderer;
import com.androsz.electricsleepbeta.util.PointD;

public class DailySleepComparisonChart extends ChartView implements
		Serializable {

	private static final long serialVersionUID = -5692853786456847694L;

	public XYMultipleSeriesDataset xyMultipleSeriesDataset;

	public XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

	public XYSeries xySeriesMovement;

	public XYSeriesRenderer xySeriesMovementRenderer;

	public DailySleepComparisonChart(final Context context) {
		super(context);
	}

	public DailySleepComparisonChart(final Context context,
			final AttributeSet as) {
		super(context, as);
	}

	@Override
	public AbstractChart buildChart() {
		if (xySeriesMovement == null) {
			// set up sleep movement series/renderer
			xySeriesMovement = new XYSeries("sleep score");
			xySeriesMovementRenderer = new XYSeriesRenderer();
			xySeriesMovementRenderer.setFillBelowLine(true);
			xySeriesMovementRenderer.setFillBelowLineColor(getResources()
					.getColor(R.color.primary1_transparent));
			xySeriesMovementRenderer.setColor(Color.TRANSPARENT);

			// add series to the dataset
			xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
			xyMultipleSeriesDataset.addSeries(xySeriesMovement);

			// set up the dataset renderer
			xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
			xyMultipleSeriesRenderer
					.addSeriesRenderer(xySeriesMovementRenderer);

			// xyMultipleSeriesRenderer.setShowLegend(false);
			xyMultipleSeriesRenderer.setAxisTitleTextSize(17);
			xyMultipleSeriesRenderer.setLabelsTextSize(17);

			// xyMultipleSeriesRenderer.setXLabels(7);
			// xyMultipleSeriesRenderer.setYLabels(5);
			// xyMultipleSeriesRenderer.setYTitle(super.getContext().getString(
			// R.string.movement_level_during_sleep));
			xyMultipleSeriesRenderer.setShowGrid(true);
			xyMultipleSeriesRenderer.setShowLegend(false);
			xyMultipleSeriesRenderer.setAxesColor(getResources().getColor(
					R.color.text));
			xyMultipleSeriesRenderer.setLabelsColor(xyMultipleSeriesRenderer
					.getAxesColor());
			final TimeChart timeChart = new TimeChart(xyMultipleSeriesDataset,
					xyMultipleSeriesRenderer);
			timeChart.setDateFormat("M/d");
			return timeChart;
		}
		return null;
	}

	public boolean makesSenseToDisplay() {
		return xySeriesMovement.getItemCount() > 1;
	}

	public void redraw(final double min, final double alarm) {
		if (makesSenseToDisplay()) {
			final double firstX = xySeriesMovement.xyList.get(0).x;
			final double lastX = xySeriesMovement.xyList
					.get(xySeriesMovement.xyList.size() - 1).x;
			xyMultipleSeriesRenderer.setXAxisMin(firstX);
			xyMultipleSeriesRenderer.setXAxisMax(lastX);

			xyMultipleSeriesRenderer.setYAxisMin(min);
			xyMultipleSeriesRenderer.setYAxisMax(alarm);
			repaint();
		}
	}

	public void sync(final Double x, final Double y, final double min,
			final double alarm) {
		xySeriesMovement.xyList.add(new PointD(x, y));
		xyMultipleSeriesRenderer.setXLabels(xySeriesMovement.xyList.size() + 1);
		redraw(min, alarm);
	}
}
