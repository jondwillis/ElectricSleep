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

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;

import org.achartengine.model.PointD;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.List;

/**
 * Data container for sleep chart points and information. This class is parcelable and is the method
 * by which sleep chart data is saved across screen rotates.
 *
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class SleepChartData implements Parcelable {

    private static final String TAG = SleepChartData.class.getSimpleName();

    /** Flag that indicates sleep chart needs a clear prior to insertion of new data. */
    boolean mNeedsClear;

    public XYSeries xySeriesMovement;

    XYSeries xySeriesCalibration;
    XYSeriesRenderer xySeriesCalibrationRenderer;
    XYSeriesRenderer xySeriesMovementRenderer;
    //int rating;
    double calibrationLevel;

    public static final Parcelable.Creator<SleepChartData> CREATOR =
        new Parcelable.Creator<SleepChartData>() {

        public SleepChartData createFromParcel(Parcel in) {
            return new SleepChartData(in);
        }

        public SleepChartData[] newArray(int size) {
            return new SleepChartData[size];
        }
    };

    /**
     * Build sleep chart data from the given context. The context is used to extract default strings
     * used for movement and calibration legends.
     */
    public SleepChartData(final Context context) {
        xySeriesMovement = new XYSeries(context.getString(R.string.legend_movement));
        // WARNING - the movement must be populated with some initial data in order for this view to
        // properly render.
        mNeedsClear = true;
        xySeriesMovement.add(0, 0);

        xySeriesMovementRenderer = new XYSeriesRenderer();
        xySeriesMovementRenderer.setFillBelowLine(true);
        xySeriesMovementRenderer.setLineWidth(3);

        xySeriesCalibration = new XYSeries(context.getString(R.string.legend_light_sleep_trigger));
        xySeriesCalibrationRenderer = new XYSeriesRenderer();
        xySeriesCalibrationRenderer.setFillBelowLine(true);
        xySeriesCalibrationRenderer.setLineWidth(3);
    }

    private SleepChartData(Parcel in) {
        xySeriesMovement = (XYSeries) in.readSerializable();
        xySeriesMovementRenderer = (XYSeriesRenderer) in.readSerializable();
        xySeriesCalibration = (XYSeries) in.readSerializable();
        xySeriesCalibrationRenderer = (XYSeriesRenderer) in.readSerializable();
        calibrationLevel = in.readDouble();
        //rating = in.readInt();
    }

    public void add(double x, double y) {
        if (mNeedsClear) {
            xySeriesMovement.clear();
            mNeedsClear = false;
        }

        if (xySeriesMovement.getItemCount() >= SleepMonitoringService.MAX_POINTS_IN_A_GRAPH) {
            xySeriesMovement.add(x, y);
            xySeriesMovement.remove(0);
        } else {
            xySeriesMovement.add(x, y);
        }
    }

    public void add(double x, double y, double calibrationLevel) {
        add(x, y);
    }

    /**
     * Return the duration or the last timestamp minus the first timestamp.
     */
    public double getDuration() {
        final double firstX = xySeriesMovement.getX(0);
        final double lastX = xySeriesMovement.getX(xySeriesMovement.getItemCount() - 1);

        final long duration = (long) (lastX - firstX);
        return duration;
    }

    public void set(List<PointD> points, double calibrationLevel) {
        xySeriesMovement.setXY(points);
        this.calibrationLevel = calibrationLevel;
    }

    public void clear() {
        Log.d(TAG, "Clearing sleep chart.");
        xySeriesMovement.clear();
        xySeriesCalibration.clear();
    }

    @Override
    public int describeContents() {
        Log.d(TAG, "Describing contents as 0.");
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(xySeriesMovement);
        dest.writeSerializable(xySeriesMovementRenderer);
        dest.writeSerializable(xySeriesCalibration);
        dest.writeSerializable(xySeriesCalibrationRenderer);
        dest.writeDouble(calibrationLevel);
        //dest.writeInt(rating);
    }
}

