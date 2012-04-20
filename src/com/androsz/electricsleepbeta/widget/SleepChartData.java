/* @(#)SleepChartData.java
 *
 *========================================================================
 * Copyright 2011 by Zeo Inc. All Rights Reserved
 *========================================================================
 *
 * Date: $Date$
 * Author: Jon Willis
 * Author: Brandon Edens <brandon.edens@myzeo.com>
 * Version: $Revision$
 */

package com.androsz.electricsleepbeta.widget;

import java.util.List;

import org.achartengine.model.PointD;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;

/**
 * Data container for sleep chart points and information. This class is
 * parcelable and is the method by which sleep chart data is saved across screen
 * rotates.
 * 
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class SleepChartData implements Parcelable {

	private static final String TAG = SleepChartData.class.getSimpleName();

	/**
	 * Flag that indicates sleep chart needs a clear prior to insertion of new
	 * data.
	 */
	private boolean mNeedsClear;

	private XYSeries mXYSeriesMovement;
	private XYSeries mXYSeriesCalibration;

	private XYSeriesRenderer mXYSeriesCalibrationRenderer;
	private XYSeriesRenderer mXYSeriesMovementRenderer;

	private float mCalibrationLevel;

	public float getCalibrationLevel() {
		return mCalibrationLevel;
	}

	public void setCalibrationLevel(float calibrationLevel) {
		mCalibrationLevel = calibrationLevel;
	}

	public static final Parcelable.Creator<SleepChartData> CREATOR = new Parcelable.Creator<SleepChartData>() {

		public SleepChartData createFromParcel(Parcel in) {
			return new SleepChartData(in);
		}

		public SleepChartData[] newArray(int size) {
			return new SleepChartData[size];
		}
	};

	/**
	 * Build sleep chart data from the given context. The context is used to
	 * extract default strings used for movement and calibration legends.
	 */
	public SleepChartData(final Context context) {
		mXYSeriesMovement = new XYSeries(
				context.getString(R.string.legend_movement));
		// WARNING - the movement must be populated with some initial data in
		// order for this view to
		// properly render.
		mNeedsClear = true;
		mXYSeriesMovement.add(0, 0);

		mXYSeriesMovementRenderer = new XYSeriesRenderer();
		mXYSeriesMovementRenderer.setFillBelowLine(true);
		mXYSeriesMovementRenderer.setLineWidth(3);

		mXYSeriesCalibration = new XYSeries(
				context.getString(R.string.legend_light_sleep_trigger));
		mXYSeriesCalibrationRenderer = new XYSeriesRenderer();
		mXYSeriesCalibrationRenderer.setFillBelowLine(true);
		mXYSeriesCalibrationRenderer.setLineWidth(3);
	}

	private SleepChartData(Parcel in) {
		mXYSeriesMovement = (XYSeries) in.readSerializable();
		mXYSeriesMovementRenderer = (XYSeriesRenderer) in.readSerializable();
		mXYSeriesCalibration = (XYSeries) in.readSerializable();
		mXYSeriesCalibrationRenderer = (XYSeriesRenderer) in.readSerializable();
		mCalibrationLevel = in.readFloat();
	}

	public void add(double x, double y) {
		synchronized (mXYSeriesMovement) {
			if (mNeedsClear) {
				mXYSeriesMovement.clear();
				mNeedsClear = false;
			}

			if (mXYSeriesMovement.getItemCount() >= SleepMonitoringService.MAX_POINTS_IN_A_GRAPH) {
				mXYSeriesMovement.remove(0);
			}
			
			mXYSeriesMovement.add(x, y);

		}
	}

	public double getLeftMostTime() {
		return mXYSeriesMovement.getX(0);
	}

	public double getRightMostTime() {
		synchronized (mXYSeriesMovement) {
			return mXYSeriesMovement.getX(mXYSeriesMovement.getItemCount() - 1);
		}
	}

	/**
	 * Return the duration or the last timestamp minus the first timestamp.
	 */
	public double getDuration() {
		final double firstX;
		final double lastX;

		synchronized (mXYSeriesMovement) {
			firstX = mXYSeriesMovement.getX(0);
			lastX = mXYSeriesMovement
					.getX(mXYSeriesMovement.getItemCount() - 1);
		}

		final long duration = (long) (lastX - firstX);
		return duration;
	}

	public void set(List<PointD> points) {
		mXYSeriesMovement.setXY(points);
	}

	public void setupCalibrationSpan(double left, double right) {
		final float calibrationLevel = getCalibrationLevel();

		synchronized (mXYSeriesCalibration) {
			// reconfigure the calibration line..
			mXYSeriesCalibration.clear();

			mXYSeriesCalibration.add(left, calibrationLevel);
			mXYSeriesCalibration.add(right, calibrationLevel);
		}
	}

	public void clear() {
		Log.d(TAG, "Clearing sleep chart.");
		mXYSeriesMovement.clear();
		mXYSeriesCalibration.clear();
	}

	@Override
	public int describeContents() {
		Log.d(TAG, "Describing contents as 0.");
		return 0;
	}

	public boolean hasTwoOrMorePoints() {
		return mXYSeriesMovement.getItemCount() > 1;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(mXYSeriesMovement);
		dest.writeSerializable(mXYSeriesMovementRenderer);
		dest.writeSerializable(mXYSeriesCalibration);
		dest.writeSerializable(mXYSeriesCalibrationRenderer);
		dest.writeFloat(mCalibrationLevel);
	}

	public void attachToDataset(XYMultipleSeriesDataset dataset) {
		dataset.addSeries(mXYSeriesMovement);
		dataset.addSeries(mXYSeriesCalibration);
	}

	public void attachToRenderer(XYMultipleSeriesRenderer mRenderer) {
		// set up the dataset renderer
		mRenderer.addSeriesRenderer(mXYSeriesMovementRenderer);
		mRenderer.addSeriesRenderer(mXYSeriesCalibrationRenderer);
	}

	public void setSeriesColors(int mMovementColor, int mMovementBorderColor,
			int mCalibrationColor, int mCalibrationBorderColor) {
		// SleepChart_movementColor
		mXYSeriesMovementRenderer.setFillBelowLineColor(mMovementColor);

		// SleepChart_movementBorderColor
		mXYSeriesMovementRenderer.setColor(mMovementBorderColor);

		// SleepChart_calibrationColor
		mXYSeriesCalibrationRenderer.setFillBelowLineColor(mCalibrationColor);

		// SleepChart_calibrationBorderColor
		mXYSeriesCalibrationRenderer.setColor(mCalibrationBorderColor);

	}
}
