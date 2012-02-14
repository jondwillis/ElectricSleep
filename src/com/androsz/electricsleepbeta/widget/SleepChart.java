package com.androsz.electricsleepbeta.widget;

import java.io.IOException;
import java.io.StreamCorruptedException;

import org.achartengine.GraphicalView;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.util.MathUtils;
import com.androsz.electricsleepbeta.util.PointD;

public class SleepChart extends GraphicalView implements Parcelable {

    private static final String TAG = SleepChart.class.getSimpleName();

    Context mContext;

    protected double calibrationLevel;// =
                                      // SettingsActivity.DEFAULT_ALARM_SENSITIVITY;

    public int rating;

    public TimeChart mChart;

    public XYMultipleSeriesDataset xyMultipleSeriesDataset;

    public XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

    public XYSeries xySeriesCalibration;
    public XYSeriesRenderer xySeriesCalibrationRenderer;

    public XYSeries xySeriesMovement;

    public XYSeriesRenderer xySeriesMovementRenderer;

    /** Flag that indicates sleep chart needs a clear prior to insertion of new data. */
    private boolean mNeedsClear;

    private boolean mSetScroll;
    private int mBackgroundColor;

    private String mAxisFormat;

    public SleepChart(final Context context) {
        this(context, null);
    }

    public SleepChart(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepChart(final Context context, final AttributeSet attrs,
            int defStyle) {
        super(context, attrs);

        Log.d(TAG, "Building sleep chart.");

        mContext = context;

        // After this point buildChart() should have been invoked and the
        // various renders and series
        // should have been populated.

        // Now begin processing attributes
        final Resources resources = context.getResources();
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.SleepChart, defStyle, 0);

        // background color processing
        if (array.hasValue(R.styleable.SleepChart_android_background)) {
            int backgroundColor = array.getColor(
                    R.styleable.SleepChart_android_background,
                    R.color.background_dark);
            xyMultipleSeriesRenderer.setBackgroundColor(backgroundColor);
            xyMultipleSeriesRenderer.setMarginsColor(backgroundColor);
            xyMultipleSeriesRenderer.setApplyBackgroundColor(true);
        } else {
            xyMultipleSeriesRenderer.setBackgroundColor(Color.TRANSPARENT);
            xyMultipleSeriesRenderer.setMarginsColor(Color.TRANSPARENT);
            xyMultipleSeriesRenderer.setApplyBackgroundColor(true);
        }

        if (array.hasValue(R.styleable.SleepChart_android_textColor)) {
            int textColor = array
                    .getColor(R.styleable.SleepChart_android_textColor,
                            R.color.text_dark);
            xyMultipleSeriesRenderer.setLabelsColor(textColor);
            xyMultipleSeriesRenderer.setAxesColor(textColor);
        }

        // SleepChart_setScroll
        if (array.getBoolean(R.styleable.SleepChart_setScroll, false)) {
            xyMultipleSeriesRenderer.setInScroll(true);
        }

        // SleepChart_gridAxisColor
        if (array.hasValue(R.styleable.SleepChart_gridAxisColor)) {
            xyMultipleSeriesRenderer.setGridColor(array.getColor(
                    R.styleable.SleepChart_gridAxisColor,
                    R.color.sleepchart_axis));
        } else {
            xyMultipleSeriesRenderer.setGridColor(R.color.sleepchart_axis);
        }

        // SleepChart_movementColor
        if (array.hasValue(R.styleable.SleepChart_movementColor)) {
            xySeriesMovementRenderer.setFillBelowLineColor(array.getColor(
                    R.styleable.SleepChart_movementColor,
                    R.color.sleepchart_movement_light));
        } else {
            xySeriesMovementRenderer.setFillBelowLineColor(array.getColor(
                    R.styleable.SleepChart_movementColor,
                    R.color.sleepchart_movement_light));
        }

        // SleepChart_movementBorderColor
        if (array.hasValue(R.styleable.SleepChart_movementBorderColor)) {
            xySeriesMovementRenderer.setColor(array.getColor(
                    R.styleable.SleepChart_movementBorderColor,
                    R.color.sleepchart_movement_border_light));
        } else {
            xySeriesMovementRenderer.setColor(array.getColor(
                    R.styleable.SleepChart_movementBorderColor,
                    R.color.sleepchart_movement_border_light));
        }

        // SleepChart_calibrationColor
        if (array.hasValue(R.styleable.SleepChart_calibrationColor)) {
            xySeriesCalibrationRenderer.setFillBelowLineColor(array.getColor(
                    R.styleable.SleepChart_calibrationColor,
                    R.color.sleepchart_movement_light));
        } else {
            xySeriesCalibrationRenderer.setFillBelowLineColor(array.getColor(
                    R.styleable.SleepChart_calibrationColor,
                    R.color.sleepchart_calibration_light));
        }

        // SleepChart_calibrationBorderColor
        if (array.hasValue(R.styleable.SleepChart_calibrationBorderColor)) {
            xySeriesCalibrationRenderer.setColor(array.getColor(
                    R.styleable.SleepChart_calibrationBorderColor,
                    R.color.sleepchart_movement_light));
        } else {
            xySeriesCalibrationRenderer.setColor(array.getColor(
                    R.styleable.SleepChart_calibrationBorderColor,
                    R.color.sleepchart_calibration_border_light));
        }
    }

    @Override
    protected AbstractChart buildChart() {
        Log.d(TAG, "Attempting to build chart.");
        if (xySeriesMovement == null) {
            Log.d(TAG, "xySeriesMovement was null.");
            Context context = getContext();

            // set up sleep movement series/renderer
            xySeriesMovement = new XYSeries(
                    context.getString(R.string.legend_movement));
            // WARNING - the movement must be populated with some initial data in order for this
            // view to properly render.
            mNeedsClear = true;
            xySeriesMovement.add(0, 0);
            xySeriesMovementRenderer = new XYSeriesRenderer();
            xySeriesMovementRenderer.setFillBelowLine(true);
            xySeriesMovementRenderer.setLineWidth(3);

            // set up calibration line series/renderer
            xySeriesCalibration = new XYSeries(
                    context.getString(R.string.legend_light_sleep_trigger));
            xySeriesCalibrationRenderer = new XYSeriesRenderer();
            xySeriesCalibrationRenderer.setFillBelowLine(true);
            xySeriesCalibrationRenderer.setLineWidth(3);

            // add series to the dataset
            xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
            xyMultipleSeriesDataset.addSeries(xySeriesMovement);
            xyMultipleSeriesDataset.addSeries(xySeriesCalibration);

            // set up the dataset renderer
            xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
            xyMultipleSeriesRenderer
                    .addSeriesRenderer(xySeriesMovementRenderer);
            xyMultipleSeriesRenderer
                    .addSeriesRenderer(xySeriesCalibrationRenderer);

            xyMultipleSeriesRenderer.setPanEnabled(false, false);
            xyMultipleSeriesRenderer.setZoomEnabled(false, false);
            final float textSize = MathUtils.calculatePxFromSp(context, 14);
            xyMultipleSeriesRenderer.setChartTitleTextSize(textSize);
            xyMultipleSeriesRenderer.setAxisTitleTextSize(textSize);
            xyMultipleSeriesRenderer.setLabelsTextSize(textSize);
            // xyMultipleSeriesRenderer.setLegendHeight((int) (MathUtils
            // .calculatePxFromDp(context, 30) + textSize*3));
            xyMultipleSeriesRenderer.setAntialiasing(true);
            xyMultipleSeriesRenderer.setFitLegend(true);
            int[] margins = xyMultipleSeriesRenderer.getMargins();
            margins[0] += 20; // increase top margin
            margins[1] += 25; // increase left margin
            margins[2] += 20; // increase bottom margin
            xyMultipleSeriesRenderer.setMargins(margins);
            xyMultipleSeriesRenderer.setLegendTextSize(textSize);
            xyMultipleSeriesRenderer.setShowLegend(true);
            xyMultipleSeriesRenderer.setShowLabels(true);
            xyMultipleSeriesRenderer.setXLabels(5);
            xyMultipleSeriesRenderer.setYLabels(8);
            xyMultipleSeriesRenderer.setYLabelsAlign(Align.RIGHT);
            xyMultipleSeriesRenderer.setShowGrid(true);

            mChart = new TimeChart(xyMultipleSeriesDataset, xyMultipleSeriesRenderer);
            // TODO determine what to do here. Ideally the date format would
            // change as the total
            // duration increased.
            mAxisFormat = "h:mm";
            mChart.setDateFormat(mAxisFormat);
            Log.d(TAG, "Returning built chart.");
            return mChart;
        } else {
            Log.w(TAG, "xySeriesMovement was NOT null.");
        }

        Log.d(TAG, "Returning null.");
        return null;
    }

    @Override
    public int describeContents() {
        Log.d(TAG, "Describing contents as 0.");
        return 0;
    }

    public double getCalibrationLevel() {
        return calibrationLevel;
    }

    public boolean makesSenseToDisplay() {
        Log.d(TAG, "Make sense to display is: " + (xySeriesMovement.getItemCount() > 1));
        return xySeriesMovement.getItemCount() > 1;
    }

    public void reconfigure() {
        if (makesSenseToDisplay()) {
            Log.d(TAG, "Executing reconfigure after it made sense to display.");
            final double firstX = xySeriesMovement.getX(0);
            final double lastX = xySeriesMovement.getX(xySeriesMovement
                    .getItemCount() - 1);

            // if (makesSenseToDisplay()) {
            // reconfigure the calibration line..
            xySeriesCalibration.clear();

            xySeriesCalibration.add(firstX, calibrationLevel);
            xySeriesCalibration.add(lastX, calibrationLevel);
            // }

            final int MINUTE_IN_MS = 1000 * 60;
            final int HOUR_IN_MS = MINUTE_IN_MS * 60;
            /*
             * if (lastX - firstX > HOUR_IN_MS*2) { ((TimeChart)
             * mChart).setDateFormat("h");
             * xyMultipleSeriesRenderer.setXLabels(8); }else if (lastX - firstX
             * > MINUTE_IN_MS*3) { ((TimeChart) mChart).setDateFormat("h:mm");
             * xyMultipleSeriesRenderer.setXLabels(5); }
             */

            xyMultipleSeriesRenderer.setXAxisMin(firstX);
            xyMultipleSeriesRenderer.setXAxisMax(lastX);
            final long duration = (long) (lastX - firstX);
            String axisFormat;
            if (duration < MINUTE_IN_MS) {
                axisFormat = "s's'";
            } else if (duration < (15 * MINUTE_IN_MS)) {
                axisFormat = "m'm's's'";
            } else if (duration < (HOUR_IN_MS * 2)) {
                axisFormat = "h:m";
            } else {
                axisFormat = "h'h'";
            }
            if (!axisFormat.equals(mAxisFormat)) {
                mAxisFormat = axisFormat;
                mChart.setDateFormat(mAxisFormat);
            }

            xyMultipleSeriesRenderer.setYAxisMin(0);
            xyMultipleSeriesRenderer
                    .setYAxisMax(SettingsActivity.MAX_ALARM_SENSITIVITY);
        } else {
            Log.w(TAG, "Asked to reconfigure but it did not make sense to display.");
        }

    }

    public void setCalibrationLevel(final double calibrationLevel) {
        this.calibrationLevel = calibrationLevel;
    }

    public void setScroll(boolean scroll) {
        mSetScroll = scroll;
    }

    public void sync(final Cursor cursor)
        throws StreamCorruptedException, IllegalArgumentException,
               IOException, ClassNotFoundException {
        Log.d(TAG, "Attempting to sync with cursor: " + cursor);
        sync(new SleepSession(cursor));
    }

    public void sync(final Double x, final Double y, final double calibrationLevel) {
        Log.d(TAG, "Attempting to sync with values: " +
              " x=" + x +
              " y=" + y +
              " calibrationLevel=" + calibrationLevel);
        if (mNeedsClear) {
            // Erase the current data if chart needs clearing.
            xySeriesMovement.clear();
            mNeedsClear = false;
        }
        if (xySeriesMovement.getItemCount() >= SleepMonitoringService.MAX_POINTS_IN_A_GRAPH) {
            xySeriesMovement.add(x, y);
            xySeriesMovement.remove(0);
        } else {
            xySeriesMovement.add(x, y);
        }
        reconfigure();
        repaint();
    }

    public void sync(final SleepSession sleepRecord) {
        Log.d(TAG, "Attempting to sync with sleep record: " + sleepRecord);
        xySeriesMovement.setXY(PointD.convertToNew(sleepRecord.getData()));
        calibrationLevel = sleepRecord.getCalibrationLevel();

        rating = sleepRecord.getRating();

        // TODO this need to take into account timezone information.
        xyMultipleSeriesRenderer.setChartTitle(sleepRecord.getTitle(mContext));
        reconfigure();
        repaint();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(TAG, "Writing sleep chart to parcel.");
        dest.writeSerializable(xyMultipleSeriesDataset);
        dest.writeSerializable(xyMultipleSeriesRenderer);
        dest.writeSerializable(xySeriesMovement);
        dest.writeSerializable(xySeriesMovementRenderer);
        dest.writeSerializable(xySeriesCalibration);
        dest.writeSerializable(xySeriesCalibrationRenderer);
        dest.writeDouble(calibrationLevel);
        dest.writeInt(rating);
    }

    public void clear() {
        Log.d(TAG, "Clearing sleep chart.");
        xySeriesMovement.clear();
        xySeriesCalibration.clear();
    }
}