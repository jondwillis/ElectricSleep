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
import com.androsz.electricsleep.achartengine.model.XYValueSeries;
import com.androsz.electricsleep.achartengine.renderer.SimpleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;

/**
 * The bubble chart rendering class.
 */
public class BubbleChart extends XYChart {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3382269721510744859L;

	/** The legend shape width. */
	private static final int SHAPE_WIDTH = 10;

	/** The minimum bubble size. */
	private static final int MIN_BUBBLE_SIZE = 2;

	/** The maximum bubble size. */
	private static final int MAX_BUBBLE_SIZE = 20;

	/**
	 * Builds a new bubble chart instance.
	 * 
	 * @param dataset
	 *            the multiple series dataset
	 * @param renderer
	 *            the multiple series renderer
	 */
	public BubbleChart(final XYMultipleSeriesDataset dataset,
			final XYMultipleSeriesRenderer renderer) {
		super(dataset, renderer);
	}

	/**
	 * The graphical representation of a circle point shape.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param paint
	 *            the paint to be used for drawing
	 * @param x
	 *            the x value of the point the shape should be drawn at
	 * @param y
	 *            the y value of the point the shape should be drawn at
	 * @param radius
	 *            the bubble radius
	 */
	private void drawCircle(final Canvas canvas, final Paint paint,
			final float x, final float y, final float radius) {
		canvas.drawCircle(x, y, radius, paint);
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
	public void drawLegendShape(final Canvas canvas,
			final SimpleSeriesRenderer renderer, final float x, final float y,
			final Paint paint) {
		paint.setStyle(Style.FILL);
		drawCircle(canvas, paint, x + SHAPE_WIDTH, y, 3);
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
	public void drawSeries(final Canvas canvas, final Paint paint,
			final float[] points, final SimpleSeriesRenderer seriesRenderer,
			final float yAxisValue, final int seriesIndex) {
		final XYSeriesRenderer renderer = (XYSeriesRenderer) seriesRenderer;
		paint.setColor(renderer.getColor());
		paint.setStyle(Style.FILL);
		final int length = points.length;
		final XYValueSeries series = (XYValueSeries) mDataset
				.getSeriesAt(seriesIndex);
		final double max = series.getMaxValue();

		final double coef = MAX_BUBBLE_SIZE / max;
		for (int i = 0; i < length; i += 2) {
			final double size = series.getValue(i / 2) * coef + MIN_BUBBLE_SIZE;
			drawCircle(canvas, paint, points[i], points[i + 1], (float) size);
		}
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
