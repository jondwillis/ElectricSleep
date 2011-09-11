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
package com.androsz.electricsleepbeta.achartengine.renderer;

import java.util.HashMap;
import java.util.Map;

import com.androsz.electricsleepbeta.achartengine.util.MathHelper;

/**
 * Multiple XY series renderer.
 */
public class XYMultipleSeriesRenderer extends DefaultRenderer {
	/**
	 * An enum for the XY chart orientation of the X axis.
	 */
	public enum Orientation {
		HORIZONTAL(0), VERTICAL(90);
		/** The rotate angle. */
		private int mAngle = 0;

		private Orientation(int angle) {
			mAngle = angle;
		}

		/**
		 * Return the orientation rotate angle.
		 * 
		 * @return the orientaion rotate angle
		 */
		public int getAngle() {
			return mAngle;
		}
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -1265461400961373427L;
	/** The end value in the X axis range. */
	private double mMaxX = -MathHelper.NULL_VALUE;
	/** The end value in the Y axis range. */
	private double mMaxY = -MathHelper.NULL_VALUE;
	/** The start value in the X axis range. */
	private double mMinX = MathHelper.NULL_VALUE;
	/** The start value in the Y axis range. */
	private double mMinY = MathHelper.NULL_VALUE;
	/** The initial axis range. */
	private double[] initialRange = new double[] { mMinX, mMaxX, mMinY, mMaxY };
	/** The axis title text size. */
	private float mAxisTitleTextSize = 12;
	/** The spacing between bars, in bar charts. */
	private double mBarSpacing = 0;
	/** The chart title. */
	private String mChartTitle = "";
	/** The chart title text size. */
	private float mChartTitleTextSize = 15;
	/** The chart values text size. */
	private float mChartValuesTextSize = 9;
	/** If the values should be displayed above the chart points. */
	private boolean mDisplayChartValues;
	/** The margins colors. */
	private int mMarginsColor = NO_COLOR;

	/** The current orientation of the chart. */
	private Orientation mOrientation = Orientation.HORIZONTAL;
	/** The pan limits. */
	private double[] mPanLimits;
	/** A flag for enabling or not the pan on the X axis. */
	private boolean mPanXEnabled = true;
	/** A flag for enabling or not the pan on the Y axis. */
	private boolean mPanYEnabled = true;
	/** The approximative number of labels on the x axis. */
	private int mXLabels = 5;
	/** The X axis labels rotation angle. */
	private float mXLabelsAngle;
	/** The X axis text labels. */
	private final Map<Double, String> mXTextLabels = new HashMap<Double, String>();
	/** The X axis title. */
	private String mXTitle = "";
	/** The approximative number of labels on the y axis. */
	private int mYLabels = 5;
	/** The Y axis labels rotation angle. */
	private float mYLabelsAngle;
	/** The Y axis title. */
	private String mYTitle = "";
	/** The zoom rate. */
	private float mZoomRate = 1.5f;
	/** A flag for enabling or not the zoom on the X axis. */
	private boolean mZoomXEnabled = true;

	/** A flag for enabling or not the zoom on the Y axis . */
	private boolean mZoomYEnabled = true;

	/**
	 * Adds a new text label for the specified X axis value.
	 * 
	 * @param x
	 *            the X axis value
	 * @param text
	 *            the text label
	 */
	public void addTextLabel(double x, String text) {
		mXTextLabels.put(x, text);
	}

	/**
	 * Returns the axis title text size.
	 * 
	 * @return the axis title text size
	 */
	public float getAxisTitleTextSize() {
		return mAxisTitleTextSize;
	}

	/**
	 * Returns the spacing between bars, in bar charts.
	 * 
	 * @return the spacing between bars
	 */
	public double getBarsSpacing() {
		return mBarSpacing;
	}

	/**
	 * Returns the chart title.
	 * 
	 * @return the chart title
	 */
	public String getChartTitle() {
		return mChartTitle;
	}

	/**
	 * Returns the chart title text size.
	 * 
	 * @return the chart title text size
	 */
	public float getChartTitleTextSize() {
		return mChartTitleTextSize;
	}

	/**
	 * Returns the chart values text size.
	 * 
	 * @return the chart values text size
	 */
	public float getChartValuesTextSize() {
		return mChartValuesTextSize;
	}

	/**
	 * Returns the initial range.
	 * 
	 * @return the initial range
	 */
	public double[] getInitialRange() {
		return initialRange;
	}

	/**
	 * Returns the margins color.
	 * 
	 * @return the margins color
	 */
	public int getMarginsColor() {
		return mMarginsColor;
	}

	/**
	 * Returns the current orientation of the chart X axis.
	 * 
	 * @return the chart orientation
	 */
	public Orientation getOrientation() {
		return mOrientation;
	}

	/**
	 * Returns the pan limits.
	 * 
	 * @return the pan limits
	 */
	public double[] getPanLimits() {
		return mPanLimits;
	}

	/**
	 * Returns the end value of the X axis range.
	 * 
	 * @return the X axis range end value
	 */
	public double getXAxisMax() {
		return mMaxX;
	}

	/**
	 * Returns the start value of the X axis range.
	 * 
	 * @return the X axis range start value
	 */
	public double getXAxisMin() {
		return mMinX;
	}

	/**
	 * Returns the approximate number of labels for the X axis.
	 * 
	 * @return the approximate number of labels for the X axis
	 */
	public int getXLabels() {
		return mXLabels;
	}

	/**
	 * Returns the rotation angle of labels for the X axis.
	 * 
	 * @return the rotation angle of labels for the X axis
	 */
	public float getXLabelsAngle() {
		return mXLabelsAngle;
	}

	/**
	 * Returns the X axis text label at the specified X axis value.
	 * 
	 * @param x
	 *            the X axis value
	 * @return the X axis text label
	 */
	public String getXTextLabel(Double x) {
		return mXTextLabels.get(x);
	}

	/**
	 * Returns the X text label locations.
	 * 
	 * @return the X text label locations
	 */
	public Double[] getXTextLabelLocations() {
		return mXTextLabels.keySet().toArray(new Double[0]);
	}

	/**
	 * Returns the title for the X axis.
	 * 
	 * @return the X axis title
	 */
	public String getXTitle() {
		return mXTitle;
	}

	/**
	 * Returns the end value of the Y axis range.
	 * 
	 * @return the Y axis range end value
	 */
	public double getYAxisMax() {
		return mMaxY;
	}

	/**
	 * Returns the start value of the Y axis range.
	 * 
	 * @return the Y axis range end value
	 */
	public double getYAxisMin() {
		return mMinY;
	}

	/**
	 * Returns the approximate number of labels for the Y axis.
	 * 
	 * @return the approximate number of labels for the Y axis
	 */
	public int getYLabels() {
		return mYLabels;
	}

	/**
	 * Returns the rotation angle of labels for the Y axis.
	 * 
	 * @return the approximate number of labels for the Y axis
	 */
	public float getYLabelsAngle() {
		return mYLabelsAngle;
	}

	/**
	 * Returns the title for the Y axis.
	 * 
	 * @return the Y axis title
	 */
	public String getYTitle() {
		return mYTitle;
	}

	/**
	 * Returns the zoom rate.
	 * 
	 * @return the zoom rate
	 */
	public float getZoomRate() {
		return mZoomRate;
	}

	/**
	 * Returns if the chart point values should be displayed as text.
	 * 
	 * @return if the chart point values should be displayed as text
	 */
	public boolean isDisplayChartValues() {
		return mDisplayChartValues;
	}

	/**
	 * Returns if the initial range is set.
	 * 
	 * @return the initial range was set or not
	 */
	public boolean isInitialRangeSet() {
		return isMinXSet() && isMaxXSet() && isMinYSet() && isMaxYSet();
	}

	/**
	 * Returns if the maximum X value was set.
	 * 
	 * @return the maxX was set or not
	 */
	public boolean isMaxXSet() {
		return mMaxX != -MathHelper.NULL_VALUE;
	}

	/**
	 * Returns if the maximum Y value was set.
	 * 
	 * @return the maxY was set or not
	 */
	public boolean isMaxYSet() {
		return mMaxY != -MathHelper.NULL_VALUE;
	}

	/**
	 * Returns if the minimum X value was set.
	 * 
	 * @return the minX was set or not
	 */
	public boolean isMinXSet() {
		return mMinX != MathHelper.NULL_VALUE;
	}

	/**
	 * Returns if the minimum Y value was set.
	 * 
	 * @return the minY was set or not
	 */
	public boolean isMinYSet() {
		return mMinY != MathHelper.NULL_VALUE;
	}

	/**
	 * Returns the enabled state of the pan on X axis.
	 * 
	 * @return if pan is enabled on X axis
	 */
	public boolean isPanXEnabled() {
		return mPanXEnabled;
	}

	/**
	 * Returns the enabled state of the pan on Y axis.
	 * 
	 * @return if pan is enabled on Y axis
	 */
	public boolean isPanYEnabled() {
		return mPanYEnabled;
	}

	/**
	 * Returns the enabled state of the zoom on X axis.
	 * 
	 * @return if zoom is enabled on X axis
	 */
	public boolean isZoomXEnabled() {
		return mZoomXEnabled;
	}

	/**
	 * Returns the enabled state of the zoom on Y axis.
	 * 
	 * @return if zoom is enabled on Y axis
	 */
	public boolean isZoomYEnabled() {
		return mZoomYEnabled;
	}

	/**
	 * Sets the axis title text size.
	 * 
	 * @param textSize
	 *            the chart axis text size
	 */
	public void setAxisTitleTextSize(float textSize) {
		mAxisTitleTextSize = textSize;
	}

	/**
	 * Sets the spacing between bars, in bar charts. Only available for bar
	 * charts. This is a coefficient of the bar width. For instance, if you want
	 * the spacing to be a half of the bar width, set this value to 0.5.
	 * 
	 * @param spacing
	 *            the spacing between bars coefficient
	 */
	public void setBarSpacing(double spacing) {
		mBarSpacing = spacing;
	}

	/**
	 * Sets the chart title.
	 * 
	 * @param title
	 *            the chart title
	 */
	public void setChartTitle(String title) {
		mChartTitle = title;
	}

	/**
	 * Sets the chart title text size.
	 * 
	 * @param textSize
	 *            the chart title text size
	 */
	public void setChartTitleTextSize(float textSize) {
		mChartTitleTextSize = textSize;
	}

	/**
	 * Sets the chart values text size.
	 * 
	 * @param textSize
	 *            the chart values text size
	 */
	public void setChartValuesTextSize(float textSize) {
		mChartValuesTextSize = textSize;
	}

	/**
	 * Sets if the chart point values should be displayed as text.
	 * 
	 * @param display
	 *            if the chart point values should be displayed as text
	 */
	public void setDisplayChartValues(boolean display) {
		mDisplayChartValues = display;
	}

	/**
	 * Sets the axes initial range values. This will be used in the zoom fit
	 * tool.
	 * 
	 * @param range
	 *            an array having the values in this order: minX, maxX, minY,
	 *            maxY
	 */
	public void setInitialRange(double[] range) {
		initialRange = range;
	}

	/**
	 * Sets the color of the margins.
	 * 
	 * @param color
	 *            the margins color
	 */
	public void setMarginsColor(int color) {
		mMarginsColor = color;
	}

	/**
	 * Sets the current orientation of the chart X axis.
	 * 
	 * @param orientation
	 *            the chart orientation
	 */
	public void setOrientation(Orientation orientation) {
		mOrientation = orientation;
	}

	/**
	 * Sets the enabled state of the pan.
	 * 
	 * @param enabledX
	 *            pan enabled on X axis
	 * @param enabledY
	 *            pan enabled on Y axis
	 */
	public void setPanEnabled(boolean enabledX, boolean enabledY) {
		mPanXEnabled = enabledX;
		mPanYEnabled = enabledY;
	}

	/**
	 * Sets the pan limits as an array of 4 values. Setting it to null or a
	 * different size array will disable the panning limitation. Values:
	 * [panMinimumX, panMaximumX, panMinimumY, panMaximumY]
	 * 
	 * @param panLimits
	 *            the pan limits
	 */
	public void setPanLimits(double[] panLimits) {
		mPanLimits = panLimits;
	}

	/**
	 * Sets the axes range values.
	 * 
	 * @param range
	 *            an array having the values in this order: minX, maxX, minY,
	 *            maxY
	 */
	public void setRange(double[] range) {
		setXAxisMin(range[0]);
		setXAxisMax(range[1]);
		setYAxisMin(range[2]);
		setYAxisMax(range[3]);
	}

	/**
	 * Sets the end value of the X axis range.
	 * 
	 * @param max
	 *            the X axis range end value
	 */
	public void setXAxisMax(double max) {
		if (!isMaxXSet()) {
			initialRange[1] = max;
		}
		mMaxX = max;
	}

	/**
	 * Sets the start value of the X axis range.
	 * 
	 * @param min
	 *            the X axis range start value
	 */
	public void setXAxisMin(double min) {
		if (!isMinXSet()) {
			initialRange[0] = min;
		}
		mMinX = min;
	}

	/**
	 * Sets the approximate number of labels for the X axis.
	 * 
	 * @param xLabels
	 *            the approximate number of labels for the X axis
	 */
	public void setXLabels(int xLabels) {
		mXLabels = xLabels;
	}

	/**
	 * Sets the rotation angle (in degrees) of labels for the X axis.
	 * 
	 * @param angle
	 *            the rotation angle of labels for the X axis
	 */
	public void setXLabelsAngle(float angle) {
		mXLabelsAngle = angle;
	}

	/**
	 * Sets the title for the X axis.
	 * 
	 * @param title
	 *            the X axis title
	 */
	public void setXTitle(String title) {
		mXTitle = title;
	}

	/**
	 * Sets the end value of the Y axis range.
	 * 
	 * @param max
	 *            the Y axis range end value
	 */
	public void setYAxisMax(double max) {
		if (!isMaxYSet()) {
			initialRange[3] = max;
		}
		mMaxY = max;
	}

	/**
	 * Sets the start value of the Y axis range.
	 * 
	 * @param min
	 *            the Y axis range start value
	 */
	public void setYAxisMin(double min) {
		if (!isMinYSet()) {
			initialRange[2] = min;
		}
		mMinY = min;
	}

	/**
	 * Sets the approximate number of labels for the Y axis.
	 * 
	 * @param yLabels
	 *            the approximate number of labels for the Y axis
	 */
	public void setYLabels(int yLabels) {
		mYLabels = yLabels;
	}

	/**
	 * Sets the rotation angle (in degrees) of labels for the Y axis.
	 * 
	 * @param angle
	 *            the rotation angle of labels for the Y axis
	 */
	public void setYLabelsAngle(float angle) {
		mYLabelsAngle = angle;
	}

	/**
	 * Sets the title for the Y axis.
	 * 
	 * @param title
	 *            the Y axis title
	 */
	public void setYTitle(String title) {
		mYTitle = title;
	}

	/**
	 * Sets the enabled state of the zoom.
	 * 
	 * @param enabledX
	 *            zoom enabled on X axis
	 * @param enabledY
	 *            zoom enabled on Y axis
	 */
	public void setZoomEnabled(boolean enabledX, boolean enabledY) {
		mZoomXEnabled = enabledX;
		mZoomYEnabled = enabledY;
	}

	/**
	 * Sets the zoom rate.
	 * 
	 * @param rate
	 *            the zoom rate
	 */
	public void setZoomRate(float rate) {
		mZoomRate = rate;
	}

}
