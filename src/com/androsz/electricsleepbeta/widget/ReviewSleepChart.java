package com.androsz.electricsleepbeta.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.androsz.electricsleepbeta.achartengine.chart.AbstractChart;
import com.androsz.electricsleepbeta.achartengine.chart.TimeChart;

public class ReviewSleepChart extends SleepChart {

	private static final long serialVersionUID = 9053706484244792095L;

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