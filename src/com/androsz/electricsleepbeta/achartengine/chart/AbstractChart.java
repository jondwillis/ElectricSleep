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

import java.io.Serializable;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;

import com.androsz.electricsleepbeta.achartengine.renderer.DefaultRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.SimpleSeriesRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.XYMultipleSeriesRenderer.Orientation;

/**
 * An abstract class to be implemented by the chart rendering classes.
 */
public abstract class AbstractChart implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2151286477891371071L;

	/**
	 * The graphical representation of the chart.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param x
	 *            the top left x value of the view to draw to
	 * @param y
	 *            the top left y value of the view to draw to
	 * @param width
	 *            the width of the view to draw to
	 * @param height
	 *            the height of the view to draw to
	 * @param paint
	 *            the paint
	 */
	public abstract void draw(Canvas canvas, int x, int y, int width,
			int height, Paint paint);

	/**
	 * Draws the chart background.
	 * 
	 * @param renderer
	 *            the chart renderer
	 * @param canvas
	 *            the canvas to paint to
	 * @param x
	 *            the top left x value of the view to draw to
	 * @param y
	 *            the top left y value of the view to draw to
	 * @param width
	 *            the width of the view to draw to
	 * @param height
	 *            the height of the view to draw to
	 * @param paint
	 *            the paint used for drawing
	 * @param newColor
	 *            if a new color is to be used
	 * @param color
	 *            the color to be used
	 */
	protected void drawBackground(DefaultRenderer renderer, Canvas canvas,
			int x, int y, int width, int height, Paint paint, boolean newColor,
			int color) {
		if (renderer.isApplyBackgroundColor() || newColor) {
			if (newColor) {
				paint.setColor(Color.TRANSPARENT);
			} else {
				paint.setColor(Color.TRANSPARENT);
			}
			paint.setStyle(Style.FILL);
			canvas.drawRect(x, y, x + width, y + height, paint);
		}
	}

	/**
	 * Draws the chart legend.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param renderer
	 *            the series renderer
	 * @param titles
	 *            the titles to go to the legend
	 * @param left
	 *            the left X value of the area to draw to
	 * @param right
	 *            the right X value of the area to draw to
	 * @param y
	 *            the y value of the area to draw to
	 * @param width
	 *            the width of the area to draw to
	 * @param height
	 *            the height of the area to draw to
	 * @param legendSize
	 *            the legend size
	 * @param paint
	 *            the paint to be used for drawing
	 */
	protected void drawLegend(Canvas canvas, DefaultRenderer renderer,
			String[] titles, int left, int right, int y, int width, int height,
			int legendSize, Paint paint) {
		if (renderer.isShowLegend()) {
			float currentX = left;
			float currentY = y + height - legendSize + 32;
			final float lineSize = getLegendShapeWidth();
			paint.setTextAlign(Align.LEFT);
			paint.setTextSize(renderer.getLegendTextSize());
			final int sLength = Math.min(titles.length,
					renderer.getSeriesRendererCount());
			for (int i = 0; i < sLength; i++) {
				String text = titles[i];
				if (titles.length == renderer.getSeriesRendererCount()) {
					paint.setColor(renderer.getSeriesRendererAt(i).getColor());
				} else {
					paint.setColor(Color.LTGRAY);
				}
				final float[] widths = new float[text.length()];
				paint.getTextWidths(text, widths);
				float sum = 0;
				for (final float value : widths) {
					sum += value;
				}
				final float extraSize = lineSize + 10 + sum;
				float currentWidth = currentX + extraSize;

				if (i > 0 && getExceed(currentWidth, renderer, right, width)) {
					currentX = left;
					currentY += 15;
					currentWidth = currentX + extraSize;
				}
				if (getExceed(currentWidth, renderer, right, width)) {
					float maxWidth = right - currentX - lineSize - 10;
					if (isVertical(renderer)) {
						maxWidth = width - currentX - lineSize - 10;
					}
					final int nr = paint
							.breakText(text, true, maxWidth, widths);
					text = text.substring(0, nr) + "...";
				}
				drawLegendShape(canvas, renderer.getSeriesRendererAt(i),
						currentX, currentY, paint);
				canvas.drawText(text, currentX + lineSize + 5, currentY + 5,
						paint);
				currentX += extraSize;
			}
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
	public abstract void drawLegendShape(Canvas canvas,
			SimpleSeriesRenderer renderer, float x, float y, Paint paint);

	/**
	 * The graphical representation of a path.
	 * 
	 * @param canvas
	 *            the canvas to paint to
	 * @param points
	 *            the points that are contained in the path to paint
	 * @param paint
	 *            the paint to be used for painting
	 * @param circular
	 *            if the path ends with the start point
	 */
	protected void drawPath(Canvas canvas, float[] points, Paint paint,
			boolean circular) {
		final Path path = new Path();
		path.moveTo(points[0], points[1]);
		for (int i = 2; i < points.length; i += 2) {
			path.lineTo(points[i], points[i + 1]);
		}
		if (circular) {
			path.lineTo(points[0], points[1]);
		}
		canvas.drawPath(path, paint);
	}

	private boolean getExceed(float currentWidth, DefaultRenderer renderer,
			int right, int width) {
		boolean exceed = currentWidth > right;
		if (isVertical(renderer)) {
			exceed = currentWidth > width;
		}
		return exceed;
	}

	/**
	 * Returns the legend shape width.
	 * 
	 * @return the legend shape width
	 */
	public abstract int getLegendShapeWidth();

	private boolean isVertical(DefaultRenderer renderer) {
		return renderer instanceof XYMultipleSeriesRenderer
				&& ((XYMultipleSeriesRenderer) renderer).getOrientation() == Orientation.VERTICAL;
	}

}
