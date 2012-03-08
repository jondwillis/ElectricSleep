package com.androsz.electricsleepbeta.widget;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import org.achartengine.GraphicalView;
import org.achartengine.chart.AbstractChart;
import org.achartengine.chart.TimeChart;
import org.achartengine.model.PointD;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.util.Log;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.util.MathUtils;

public class SleepChart extends GraphicalView {

    private static final String TAG = SleepChart.class.getSimpleName();

    /** The default number of X label ticks. */
    private static final int DEFAULT_X_LABEL_TICKS = 5;

    Context mContext;

    public int rating;

    public TimeChart mChart;

    public XYMultipleSeriesDataset mDataset;

    public XYMultipleSeriesRenderer mRenderer;

    private static final float INVALID_CALIBRATION = -1;

    int mBackgroundColor;
    int mTextColor;
    int mCalibrationBorderColor;
    int mCalibrationColor;
    int mGridColor;
    int mMovementBorderColor;
    int mMovementColor;
    int mXLabelTicks;
    boolean mSetBackgroundColor;
    boolean mSetGridColor;
    boolean mSetInScroll;
    boolean mSetTextColor;
    boolean mShowGrid;
    boolean mShowLabels;
    boolean mShowLegend;
    boolean mShowTitle = true;

    /** Temporary storage for calibration level information prior to mData being initalized. */
    private float mTempCalibrationLevel = INVALID_CALIBRATION;

    private String mAxisFormat;

    public SleepChartData mData;

    int mDefStyle;

    AttributeSet mAttrs;

    public SleepChart(final Context context) {
        this(context, null);
    }

    public SleepChart(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SleepChart(final Context context, final AttributeSet attrs, int defStyle) {
        super(context, attrs);
        mContext = context;
        mAttrs = attrs;
        mDefStyle = defStyle;

        // Now begin processing attributes
        final Resources resources = mContext.getResources();
        final TypedArray array =
            mContext.obtainStyledAttributes(mAttrs, R.styleable.SleepChart, mDefStyle, 0);

        if (array.hasValue(R.styleable.SleepChart_android_background)) {
            mSetBackgroundColor = true;
            mBackgroundColor = array.getColor(R.styleable.SleepChart_android_background,
                                                 R.color.background_dark);
        }

        if (array.hasValue(R.styleable.SleepChart_android_textColor)) {
            mSetTextColor = true;
            mTextColor = array.getColor(R.styleable.SleepChart_android_textColor,
                                        R.color.text_dark);
        }

        if (array.getBoolean(R.styleable.SleepChart_setScroll, false)) {
            mSetInScroll = true;
        }

        if (array.hasValue(R.styleable.SleepChart_gridAxisColor)) {
            mSetGridColor = true;
            mGridColor = array.getColor(R.styleable.SleepChart_gridAxisColor,
                                        R.color.sleepchart_axis);
        }

        mXLabelTicks = array.getInteger(R.styleable.SleepChart_xLabelTicks,
                                        DEFAULT_X_LABEL_TICKS);

        mMovementColor = array.getColor(R.styleable.SleepChart_movementColor,
                                        R.color.sleepchart_movement_light);

        mMovementBorderColor = array.getColor(R.styleable.SleepChart_movementBorderColor,
                                              R.color.sleepchart_movement_border_light);

        mCalibrationColor = array.getColor(R.styleable.SleepChart_calibrationColor,
                                           R.color.sleepchart_calibration_light);

        mCalibrationBorderColor = array.getColor(R.styleable.SleepChart_calibrationBorderColor,
                                                 R.color.sleepchart_calibration_border_light);

        mShowGrid = array.getBoolean(R.styleable.SleepChart_showGrid, true);

        mShowLabels = array.getBoolean(R.styleable.SleepChart_showLabels, true);
        mShowLegend = array.getBoolean(R.styleable.SleepChart_showLegend, true);
        mShowTitle = array.getBoolean(R.styleable.SleepChart_showTitle, true);
    }

    @Override
    protected AbstractChart buildChart() {
        Log.d(TAG, "Attempting to build chart.");
        if (mChart != null) {
            Log.d(TAG, "Attempt to build chart when chart already exists.");
            return null;
        }

        mDataset = new XYMultipleSeriesDataset();
        mRenderer = new XYMultipleSeriesRenderer();
        // Set initial framing for renderer.
        mRenderer.setYAxisMin(0);
        mRenderer.setYAxisMax(SettingsActivity.MAX_ALARM_SENSITIVITY);
        mRenderer.setXAxisMin(System.currentTimeMillis());
        mRenderer.setXAxisMax(System.currentTimeMillis());
        mRenderer.setPanEnabled(false, false);
        mRenderer.setZoomEnabled(false, false);
        mChart = new TimeChart(mDataset, mRenderer);
        mChart.setDateFormat("h:mm:ss");
        return mChart;
    }

    public void addPoint(double x, double y) {
        synchronized (this) {
            mData.add(x, y);
        }
    }

    /**
     * Return the calibration level if one is available; otherwise return INVALID_CALIBRATION.
     *
     * Callers of this method could call hasCalibrationLevel first to determine if information
     * surrounding calibration is available prior to invoking this method.
     */
    public float getCalibrationLevel() {
        if (mData != null) {
            return mData.calibrationLevel;
        } else if (mTempCalibrationLevel != INVALID_CALIBRATION) {
            Log.d(TAG, "Returning the temporary calibration level.");
            return mTempCalibrationLevel;
        }
        return INVALID_CALIBRATION;
    }

    /**
     * Return true or false depending upon whether or not calibration level information is available
     * either as contained in sleep chart data or in the temporary calibration level information.
     */
    public boolean hasCalibrationLevel() {
        return (mData != null || mTempCalibrationLevel != INVALID_CALIBRATION);
    }

    public boolean makesSenseToDisplay() {
        if (mData == null) {
            return false;
        }
        return mData.xySeriesMovement.getItemCount() > 1;
    }

    public void reconfigure() {
        if (makesSenseToDisplay()) {
            final double firstX = mData.xySeriesMovement.getX(0);
            final double lastX =
                mData.xySeriesMovement.getX(mData.xySeriesMovement.getItemCount() - 1);

            // if (makesSenseToDisplay()) {
            // reconfigure the calibration line..
            mData.xySeriesCalibration.clear();

            mData.xySeriesCalibration.add(firstX, mData.calibrationLevel);
            mData.xySeriesCalibration.add(lastX, mData.calibrationLevel);
            // }
            /*
             * if (lastX - firstX > HOUR_IN_MS*2) { ((TimeChart)
             * mChart).setDateFormat("h");
             * xyMultipleSeriesRenderer.setXLabels(8); }else if (lastX - firstX
             * > MINUTE_IN_MS*3) { ((TimeChart) mChart).setDateFormat("h:mm");
             * xyMultipleSeriesRenderer.setXLabels(5); }
             */

            mRenderer.setXAxisMin(firstX);
            mRenderer.setXAxisMax(lastX);
            mRenderer.setYAxisMin(0);
            mRenderer.setYAxisMax(SettingsActivity.MAX_ALARM_SENSITIVITY);

            setupChartAxisFormat();
        } else {
            Log.w(TAG, "Asked to reconfigure but it did not make sense to display.");
        }
    }

    /**
     * Set this sleep chart to the given data.
     */
    public void setData(SleepChartData data) {
        synchronized (this) {
            mData = data;
            setupData();
        }
    }

    public void setCalibrationLevel(final float calibrationLevel) {
        if (mData == null) {
            mTempCalibrationLevel = calibrationLevel;
        } else {
            mData.calibrationLevel = calibrationLevel;
            mTempCalibrationLevel = INVALID_CALIBRATION;
        }
        reconfigure();
        repaint();
    }

    public void setScroll(boolean scroll) {
        mSetInScroll = scroll;
    }

    public void sync(final Cursor cursor)
        throws StreamCorruptedException, IllegalArgumentException,
               IOException, ClassNotFoundException {
        Log.d(TAG, "Attempting to sync with cursor: " + cursor);
        this.sync(new SleepSession(cursor));
    }

    public void sync(final Double x, final Double y, final float calibrationLevel) {
        synchronized (this) {
            initCheckData();
            mData.add(x, y, calibrationLevel);
        }
        reconfigure();
        repaint();
    }

    public void sync(List<PointD> points)
    {
        synchronized (this) {
            initCheckData();
            clear();
        	for (PointD point : points) {
                addPoint(point.x, point.y);
            }
        }
        reconfigure();
    }

    public void sync(final SleepSession sleepRecord) {
        Log.d(TAG, "Attempting to sync with sleep record: " + sleepRecord);

        synchronized (this) {
            initCheckData();
            mData.set(com.androsz.electricsleepbeta.util.PointD.convertToNew(sleepRecord.getData()),
                      sleepRecord.getCalibrationLevel());
        }

        // TODO this need to take into account timezone information.
        if (mShowTitle) {
            mRenderer.setChartTitle(sleepRecord.getTitle(mContext));
        }
        reconfigure();
        repaint();
    }

    public void clear() {
        synchronized (this) {
            if (mData != null) {
                mData.clear();
                repaint();
            }
        }
    }

    /**
     * Perform a check to see if data is initialized and if not then initialize it.
     */
    private void initCheckData() {
        if (mData == null) {
            mData = new SleepChartData(mContext);
            if (mTempCalibrationLevel != INVALID_CALIBRATION) {
                mData.calibrationLevel = mTempCalibrationLevel;
                mTempCalibrationLevel = INVALID_CALIBRATION;
            }
            setupData();
        }
    }

    /**
     * Set the chart's axis format based upon current duration of the data.
     */
    private void setupChartAxisFormat() {
        if (mData == null) {
            return;
        }
        final double duration = mData.getDuration();
        String axisFormat;

        final int MSEC_PER_MINUTE = 1000 * 60;
        final int MSEC_PER_HOUR = MSEC_PER_MINUTE * 60;

        if (duration > (15 * MSEC_PER_MINUTE)) {
            axisFormat = "h:mm";
        } else {
            axisFormat = "h:mm:ss";
        }
        if (!axisFormat.equals(mAxisFormat)) {
            mAxisFormat = axisFormat;
            mChart.setDateFormat(mAxisFormat);
        }
    }

    /**
     * Helper method that initializes charting after insertion of data.
     */
    private void setupData() {
        if (mData == null) {
            Log.w(TAG, "Asked to setup data when it was not instantiated.");
            return;
        }

        // remove all existing series
        for (int i = 0; i < mDataset.getSeriesCount(); i++) {
            mDataset.removeSeries(i);
        }
        for (int i = 0; i < mRenderer.getSeriesRendererCount(); i++) {
            mRenderer.removeSeriesRenderer(mRenderer.getSeriesRendererAt(i));
        }

        // add series to the dataset
        mDataset.addSeries(mData.xySeriesMovement);
        mDataset.addSeries(mData.xySeriesCalibration);

        // set up the dataset renderer
        mRenderer.addSeriesRenderer(mData.xySeriesMovementRenderer);
        mRenderer.addSeriesRenderer(mData.xySeriesCalibrationRenderer);

        final float textSize = MathUtils.calculatePxFromSp(mContext, 14);
        mRenderer.setChartTitleTextSize(textSize);
        mRenderer.setAxisTitleTextSize(textSize);
        mRenderer.setLabelsTextSize(textSize);
        // xyMultipleSeriesRenderer.setLegendHeight((int) (MathUtils
        // .calculatePxFromDp(context, 30) + textSize*3));
        mRenderer.setAntialiasing(true);
        mRenderer.setFitLegend(true);
        mRenderer.setLegendTextSize(textSize);
        mRenderer.setXLabels(mXLabelTicks);
        mRenderer.setYLabels(8);
        mRenderer.setYLabelsAlign(Align.RIGHT);

        setupStyle();
        setupChartAxisFormat();
    }

    /**
     * Iterate over the known attributes for this view setting our chart to the desired settings.
     */
    private void setupStyle() {
        // TODO remove comment
        // After this point buildChart() should have been invoked and the
        // various renders and series
        // should have been populated.

        if (mAttrs == null) {
            Log.d(TAG, "No attributes nothing to process.");
            return;
        }

        Log.d(TAG, "Processing attributes.");

        // background color processing
        if (mSetBackgroundColor) {
            mRenderer.setBackgroundColor(mBackgroundColor);
            mRenderer.setMarginsColor(mBackgroundColor);
            mRenderer.setApplyBackgroundColor(true);
        } else {
            mRenderer.setBackgroundColor(Color.TRANSPARENT);
            mRenderer.setMarginsColor(Color.TRANSPARENT);
            mRenderer.setApplyBackgroundColor(true);
        }

        if (mSetTextColor) {
            mRenderer.setLabelsColor(mTextColor);
            mRenderer.setAxesColor(mTextColor);
        }

        // SleepChart_setScroll
        mRenderer.setInScroll(mSetInScroll);

        // SleepChart_gridAxisColor
        if (mSetGridColor) {
            mRenderer.setGridColor(mGridColor);
        } else {
            mRenderer.setGridColor(R.color.sleepchart_axis);
        }

        // SleepChart_movementColor
        mData.xySeriesMovementRenderer.setFillBelowLineColor(mMovementColor);

        // SleepChart_movementBorderColor
        mData.xySeriesMovementRenderer.setColor(mMovementBorderColor);

        // SleepChart_calibrationColor
        mData.xySeriesCalibrationRenderer.setFillBelowLineColor(mCalibrationColor);

        // SleepChart_calibrationBorderColor
        mData.xySeriesCalibrationRenderer.setColor(mCalibrationBorderColor);

        // SleepChart_showGrid
        mRenderer.setShowGrid(mShowGrid);

        int[] margins = mRenderer.getMargins();
        mRenderer.setShowLabels(mShowLabels);
        if (mShowLabels) {
            margins[1] += 25; // increase left margin
        }
        mRenderer.setShowLegend(mShowLegend);
        if (mShowLegend) {
            margins[2] += 20; // increase bottom margin
        }
        if (mShowTitle) {
            margins[0] += 20; // increase top margin
        }
        mRenderer.setMargins(margins);

        setupChartAxisFormat();
    }
}