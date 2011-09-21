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
package com.androsz.electricsleepbeta.achartengine.chart;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.androsz.electricsleepbeta.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleepbeta.achartengine.model.XYSeries;
import com.androsz.electricsleepbeta.achartengine.renderer.SimpleSeriesRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.XYMultipleSeriesRenderer;

/**
 * The range bar chart rendering class.
 */
public class RangeBarChart extends BarChart {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1727592765996342222L;

	/**
	 * Builds a new range bar chart instance.
	 * 
	 * @param dataset
	 *            the multiple series dataset
	 * @param renderer
	 *            the multiple series renderer
	 * @param type
	 *            the range bar chart type
	 */
	public RangeBarChart(XYMultipleSeriesDataset dataset, XYMultipleSeriesRenderer renderer,
			Type type) {
		super(dataset, renderer, type);
	}

	/**
	 * The graphical representation of the series values as text.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param series
	 *            the series to be painted
	 * @param paint
	 *            the paint to be used for drawing
	 * @param points
	 *            the array of points to be used for drawing the series
	 * @param seriesIndex
	 *            the index of the series currently being drawn
	 */
	@Override
	protected void drawChartValuesText(Canvas canvas, XYSeries series, Paint paint, float[] points,
			int seriesIndex) {
		final int seriesNr = mDataset.getSeriesCount();
		final float halfDiffX = getHalfDiffX(points, points.length, seriesNr);
		for (int k = 0; k < points.length; k += 4) {
			float x = points[k];
			if (mType == Type.DEFAULT) {
				x += seriesIndex * 2 * halfDiffX - (seriesNr - 1.5f) * halfDiffX;
			}
			// draw the maximum value
			drawText(canvas, getLabel(series.getY(k / 2 + 1)), x, points[k + 3] - 3f, paint, 0);
			// draw the minimum value
			drawText(canvas, getLabel(series.getY(k / 2)), x, points[k + 1] + 7.5f, paint, 0);
		}
	}

	/**
	 * The graphical representation of a series.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param paint
	 *            the paint to be used for drawing
	 * @param points
	 *            the array of points to be used for drawing the series
	 * @param seriesRenderer
	 *            the series renderer
	 * @param yAxisValue
	 *            the minimum value of the y axis
	 * @param seriesIndex
	 *            the index of the series currently being drawn
	 */
	@Override
	public void drawSeries(Canvas canvas, Paint paint, float[] points,
			SimpleSeriesRenderer seriesRenderer, float yAxisValue, int seriesIndex) {
		final int seriesNr = mDataset.getSeriesCount();
		final int length = points.length;
		paint.setColor(seriesRenderer.getColor());
		paint.setStyle(Style.FILL);
		final float halfDiffX = getHalfDiffX(points, length, seriesNr);
		for (int i = 0; i < length; i += 4) {
			final float xMin = points[i];
			final float yMin = points[i + 1];
			// xMin = xMax
			final float xMax = points[i + 2];
			final float yMax = points[i + 3];
			if (mType == Type.STACKED) {
				canvas.drawRect(xMin - halfDiffX, yMax, xMax + halfDiffX, yMin, paint);
			} else {
				final float startX = xMin - seriesNr * halfDiffX + seriesIndex * 2 * halfDiffX;
				canvas.drawRect(startX, yMax, startX + 2 * halfDiffX, yMin, paint);
			}
		}
	}

	/**
	 * Returns the value of a constant used to calculate the half-distance.
	 * 
	 * @return the constant value
	 */
	@Override
	protected float getCoeficient() {
		return 0.5f;
	}

}
