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
import com.androsz.electricsleep.achartengine.renderer.SimpleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;

/**
 * The line chart rendering class.
 */
public class LineChart extends XYChart {
	/**
	 * 
	 */
	private static final long serialVersionUID = -533658285402330691L;
	/** The legend shape width. */
	private static final int SHAPE_WIDTH = 30;

	/**
	 * Builds a new line chart instance.
	 * 
	 * @param dataset
	 *            the multiple series dataset
	 * @param renderer
	 *            the multiple series renderer
	 */
	public LineChart(final XYMultipleSeriesDataset dataset,
			final XYMultipleSeriesRenderer renderer) {
		super(dataset, renderer);
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
		canvas.drawLine(x, y, x + SHAPE_WIDTH, y, paint);
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
		final int length = points.length;
		final XYSeriesRenderer renderer = (XYSeriesRenderer) seriesRenderer;
		final float lineWidth = paint.getStrokeWidth();
		paint.setStrokeWidth(renderer.getLineWidth());
		if (renderer.isFillBelowLine()) {
			paint.setColor(renderer.getFillBelowLineColor());
			final int pLength = points.length;
			final float[] fillPoints = new float[pLength + 4];
			System.arraycopy(points, 0, fillPoints, 0, length);
			fillPoints[0] = points[0] + 1;
			fillPoints[length] = fillPoints[length - 2];
			fillPoints[length + 1] = yAxisValue;
			fillPoints[length + 2] = fillPoints[0];
			fillPoints[length + 3] = fillPoints[length + 1];
			paint.setStyle(Style.FILL);
			drawPath(canvas, fillPoints, paint, true);
		}
		paint.setColor(seriesRenderer.getColor());
		paint.setStyle(Style.STROKE);
		drawPath(canvas, points, paint, false);
		paint.setStrokeWidth(lineWidth);
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

	/**
	 * Returns if the chart should display the points as a certain shape.
	 * 
	 * @param renderer
	 *            the series renderer
	 */
	@Override
	public boolean isRenderPoints(final SimpleSeriesRenderer renderer) {
		return ((XYSeriesRenderer) renderer).getPointStyle() != PointStyle.POINT;
	}

}
