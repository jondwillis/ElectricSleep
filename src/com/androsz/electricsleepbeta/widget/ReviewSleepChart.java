package com.androsz.electricsleepbeta.widget;

import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.TimeChart;

import android.content.Context;
import android.util.AttributeSet;

public class ReviewSleepChart extends SleepChart {

	public ReviewSleepChart(final Context context) {
		super(context);
	}

	public ReviewSleepChart(final Context context, final AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected AbstractChart buildChart() {
		final TimeChart chart = (TimeChart) super.buildChart();
		chart.setDateFormat("h");
		return chart;
	}
}