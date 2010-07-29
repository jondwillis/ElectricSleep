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

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.SimpleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;

/**
 * The bar chart rendering class.
 */
public class BarChart extends XYChart {
	/**
	 * The bar chart type enum.
	 */
	public enum Type {
		DEFAULT, STACKED;
	}

	/** The legend shape width. */
	private static final int SHAPE_WIDTH = 12;

	/** The chart type. */
	protected Type mType = Type.DEFAULT;

	/**
	 * Builds a new bar chart instance.
	 * 
	 * @param dataset
	 *            the multiple series dataset
	 * @param renderer
	 *            the multiple series renderer
	 * @param type
	 *            the bar chart type
	 */
	public BarChart(XYMultipleSeriesDataset dataset,
			XYMultipleSeriesRenderer renderer, Type type) {
		super(dataset, renderer);
		mType = type;
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
	protected void drawChartValuesText(Canvas canvas, XYSeries series,
			Paint paint, float[] points, int seriesIndex) {
		final int seriesNr = mDataset.getSeriesCount();
		final float halfDiffX = getHalfDiffX(points, points.length, seriesNr);
		for (int k = 0; k < points.length; k += 2) {
			float x = points[k];
			if (mType == Type.DEFAULT) {
				x += seriesIndex * 2 * halfDiffX - (seriesNr - 1.5f)
						* halfDiffX;
			}
			drawText(canvas, getLabel(series.getY(k / 2)), x,
					points[k + 1] - 3.5f, paint, 0);
		}
	}

	/**
	 * The graphical representation of the legend shape.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param renderer
	 *            the series renderer
	 * @param x
	 *            the x value of the point the shape should be drawn at
	 * @param y
	 *            the y value of the point the shape should be drawn at
	 * @param paint
	 *            the paint to be used for drawing
	 */
	@Override
	public void drawLegendShape(Canvas canvas, SimpleSeriesRenderer renderer,
			float x, float y, Paint paint) {
		final float halfShapeWidth = SHAPE_WIDTH / 2;
		canvas.drawRect(x, y - halfShapeWidth, x + SHAPE_WIDTH, y
				+ halfShapeWidth, paint);
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
			SimpleSeriesRenderer seriesRenderer, float yAxisValue,
			int seriesIndex) {
		final int seriesNr = mDataset.getSeriesCount();
		final int length = points.length;
		paint.setColor(seriesRenderer.getColor());
		paint.setStyle(Style.FILL);
		final float halfDiffX = getHalfDiffX(points, length, seriesNr);
		for (int i = 0; i < length; i += 2) {
			final float x = points[i];
			final float y = points[i + 1];
			if (mType == Type.STACKED) {
				canvas.drawRect(x - halfDiffX, y, x + halfDiffX, yAxisValue,
						paint);
			} else {
				final float startX = x - seriesNr * halfDiffX + seriesIndex * 2
						* halfDiffX;
				canvas.drawRect(startX, y, startX + 2 * halfDiffX, yAxisValue,
						paint);
			}
		}
	}

	/**
	 * Returns the value of a constant used to calculate the half-distance.
	 * 
	 * @return the constant value
	 */
	protected float getCoeficient() {
		return 1;
	}

	/**
	 * Calculates and returns the half-distance in the graphical representation
	 * of 2 consecutive points.
	 * 
	 * @param points
	 *            the points
	 * @param length
	 *            the points length
	 * @param seriesNr
	 *            the series number
	 * @return the calculated half-distance value
	 */
	protected float getHalfDiffX(float[] points, int length, int seriesNr) {
		float halfDiffX = (points[length - 2] - points[0]) / length;
		if (halfDiffX == 0) {
			halfDiffX = 10;
		}

		if (mType != Type.STACKED) {
			halfDiffX /= seriesNr;
		}
		return halfDiffX / getCoeficient();
	}

	/**
	 * Returns the legend shape width.
	 * 
	 * @return the legend shape width
	 */
	@Override
	public int getLegendShapeWidth() {
		return SHAPE_WIDTH;
	}

}
