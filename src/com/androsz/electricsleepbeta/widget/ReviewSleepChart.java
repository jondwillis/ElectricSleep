package com.androsz.electricsleepbeta.widget;

import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.TimeChart;

import android.content.Context;
import android.util.AttributeSet;

public class ReviewSleepChart extends SleepChart {

	public ReviewSleepChart(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected AbstractChart buildChart() {
		final TimeChart chart = (TimeChart) super.buildChart();
		
		//final double firstX = xyMultipleSeriesRenderer.getXAxisMin();
		//final double lastX = xyMultipleSeriesRenderer.getXAxisMax();
		//final int HOUR_IN_MS = 1000 * 60 * 60;
		//if (lastX - firstX > HOUR_IN_MS*2) {
		//	chart.getRenderer().setXLabels(8);
		//	chart.setDateFormat("h");
		//}
		
		xyMultipleSeriesRenderer.setInScroll(true);
		return chart;
	}
	
	@Override
	public void reconfigure()
	{
		if (makesSenseToDisplay()) {
			super.reconfigure();
			final int HOUR_IN_MS = 1000 * 60 * 60;
			final double firstX = xyMultipleSeriesRenderer.getXAxisMin();
			final double lastX = xyMultipleSeriesRenderer.getXAxisMax();
			final double length = lastX-firstX;
			
		}
	}
}