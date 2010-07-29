/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.androsz.electricsleep.achartengine.chart;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;

/**
 * The time chart rendering class.
 */
public class TimeChart extends LineChart {
	/** The number of milliseconds in a day. */
	public static final long DAY = 24 * 60 * 60 * 1000;
	/** The date format pattern to be used in formatting the X axis labels. */
	private String mDateFormat;

	/**
	 * Builds a new time chart instance.
	 * 
	 * @param dataset
	 *            the multiple series dataset
	 * @param renderer
	 *            the multiple series renderer
	 */
	public TimeChart(XYMultipleSeriesDataset dataset,
			XYMultipleSeriesRenderer renderer) {
		super(dataset, renderer);
	}

	/**
	 * The graphical representation of the labels on the X axis.
	 * 
	 * @param xLabels
	 *            the X labels values
	 * @param xTextLabelLocations
	 *            the X text label locations
	 * @param canvas
	 *            the canvas to paint to
	 * @param paint
	 *            the paint to be used for drawing
	 * @param left
	 *            the left value of the labels area
	 * @param top
	 *            the top value of the labels area
	 * @param bottom
	 *            the bottom value of the labels area
	 * @param xPixelsPerUnit
	 *            the amount of pixels per one unit in the chart labels
	 * @param minX
	 *            the minimum value on the X axis in the chart
	 */
	@Override
	protected void drawXLabels(List<Double> xLabels,
			Double[] xTextLabelLocations, Canvas canvas, Paint paint, int left,
			int top, int bottom, double xPixelsPerUnit, double minX) {
		final int length = xLabels.size();
		final boolean showLabels = mRenderer.isShowLabels();
		final boolean showGrid = mRenderer.isShowGrid();
		final DateFormat format = getDateFormat(xLabels.get(0), xLabels
				.get(length - 1));
		for (int i = 0; i < length; i++) {
			final long label = Math.round(xLabels.get(i));
			final float xLabel = (float) (left + xPixelsPerUnit
					* (label - minX));
			if (showLabels) {
				paint.setColor(mRenderer.getLabelsColor());
				canvas.drawLine(xLabel, bottom, xLabel, bottom + 4, paint);
				drawText(canvas, format.format(new Date(label)), xLabel,
						bottom + 12, paint, 0);
			}
			if (showGrid) {
				paint.setColor(GRID_COLOR);
				canvas.drawLine(xLabel, bottom, xLabel, top, paint);
			}
		}
	}

	/**
	 * Returns the date format pattern to be used for formatting the X axis
	 * labels.
	 * 
	 * @return the date format pattern for the X axis labels
	 */
	public String getDateFormat() {
		return mDateFormat;
	}

	/**
	 * Returns the date format pattern to be used, based on the date range.
	 * 
	 * @param start
	 *            the start date in milliseconds
	 * @param end
	 *            the end date in milliseconds
	 * @return the date format
	 */
	private DateFormat getDateFormat(double start, double end) {
		if (mDateFormat != null) {
			SimpleDateFormat format = null;
			try {
				format = new SimpleDateFormat(mDateFormat);
				return format;
			} catch (final Exception e) {
				// do nothing here
			}
		}
		DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);
		final double diff = end - start;
		if (diff > DAY && diff < 5 * DAY) {
			format = DateFormat.getDateTimeInstance(DateFormat.SHORT,
					DateFormat.SHORT);
		} else if (diff < DAY) {
			format = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		}
		return format;
	}

	/**
	 * Sets the date format pattern to be used for formatting the X axis labels.
	 * 
	 * @param format
	 *            the date format pattern for the X axis labels. If null, an
	 *            appropriate default format will be used.
	 */
	public void setDateFormat(String format) {
		mDateFormat = format;
	}
}
