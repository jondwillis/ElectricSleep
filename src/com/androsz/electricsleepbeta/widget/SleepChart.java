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

	final Context mContext;

	public int rating;

	private XYMultipleSeriesDataset mDataset;

	private XYMultipleSeriesRenderer mRenderer;

	private int DEFAULT_BOTTOM_MARGIN = 20;
	private int DEFAULT_LEFT_MARGIN = 25;
	private int DEFAULT_TOP_MARGIN = 20;

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

	private String mAxisFormat;

	public final SleepChartData mData;

	final int mDefStyle;

	AttributeSet mAttrs;

	public SleepChart(final Context context) {
		this(context, null);
	}

	public SleepChart(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SleepChart(final Context context, final AttributeSet attrs,
			int defStyle) {
		super(context, attrs);
		mContext = context;
		mAttrs = attrs;
		mDefStyle = defStyle;
		mData = new SleepChartData(mContext);

		// Now begin processing attributes
		final TypedArray array = mContext.obtainStyledAttributes(mAttrs,
				R.styleable.SleepChart, mDefStyle, 0);

		if (array.hasValue(R.styleable.SleepChart_android_background)) {
			mSetBackgroundColor = true;
			mBackgroundColor = array.getColor(
					R.styleable.SleepChart_android_background,
					R.color.background_dark);
		}

		if (array.hasValue(R.styleable.SleepChart_android_textColor)) {
			mSetTextColor = true;
			mTextColor = array
					.getColor(R.styleable.SleepChart_android_textColor,
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

		mMovementBorderColor = array.getColor(
				R.styleable.SleepChart_movementBorderColor,
				R.color.sleepchart_movement_border_light);

		mCalibrationColor = array.getColor(
				R.styleable.SleepChart_calibrationColor,
				R.color.sleepchart_calibration_light);

		mCalibrationBorderColor = array.getColor(
				R.styleable.SleepChart_calibrationBorderColor,
				R.color.sleepchart_calibration_border_light);

		mShowGrid = array.getBoolean(R.styleable.SleepChart_showGrid, true);

		mShowLabels = array.getBoolean(R.styleable.SleepChart_showLabels, true);
		mShowLegend = array.getBoolean(R.styleable.SleepChart_showLegend, true);
		mShowTitle = array.getBoolean(R.styleable.SleepChart_showTitle, true);

		setupData();
	}

	@Override
	protected AbstractChart buildChart() {
		Log.d(TAG, "Attempting to build chart.");
		if (mChart != null) {
			Log.w(TAG, "Attempt to build chart when chart already exists.");
			return mChart;
		}

		mDataset = new XYMultipleSeriesDataset();
		mRenderer = new XYMultipleSeriesRenderer();
		// Set initial framing for renderer.
		mRenderer.setYAxisMin(0);
		// Referencing SettingsActivity.MAX_ALARM_SENSITIVITY causes errors in ADT.
		if (isInEditMode()) {
			mRenderer.setYAxisMax(1.0f);
		} else {
			mRenderer.setYAxisMax(SettingsActivity.MAX_ALARM_SENSITIVITY);
		}
		mRenderer.setXAxisMin(System.currentTimeMillis());
		mRenderer.setXAxisMax(System.currentTimeMillis());
		mRenderer.setPanEnabled(false, false);
		mRenderer.setZoomEnabled(false, false);

		TimeChart timeChart = new TimeChart(mDataset, mRenderer);
		timeChart.setDateFormat("h:mm:ss");
		mChart = timeChart;

		return mChart;
	}

	/**
	 * Return the calibration level if one is available; otherwise return
	 * INVALID_CALIBRATION.
	 * 
	 * Callers of this method could call hasCalibrationLevel first to determine
	 * if information surrounding calibration is available prior to invoking
	 * this method.
	 */
	public float getCalibrationLevel() {
		return mData.getCalibrationLevel();
	}

	public boolean hasTwoOrMorePoints() {
		return mData.hasTwoOrMorePoints();
	}

	public void reconfigure() {
		if (hasTwoOrMorePoints()) {
			synchronized (mData) {
				final double firstX = mData.getLeftMostTime();
				final double lastX = mData.getRightMostTime();

				mData.setupCalibrationSpan(firstX, lastX);

				mRenderer.setXAxisMin(firstX);
				mRenderer.setXAxisMax(lastX);
			}

			setupChartAxisFormat();
		} else {
			Log.w(TAG,
					"Asked to reconfigure but it did not make sense to display.");
		}
	}

	public void setCalibrationLevelAndRedraw(final float calibrationLevel) {
		mData.setCalibrationLevel(calibrationLevel);
		reconfigure();
		repaint();
	}

	public void setScroll(boolean scroll) {
		mSetInScroll = scroll;
	}

	public void sync(final Cursor cursor) throws StreamCorruptedException,
			IllegalArgumentException, IOException, ClassNotFoundException {
		Log.d(TAG, "Attempting to sync with cursor: " + cursor);
		this.sync(new SleepSession(cursor));
	}

	public void sync(final Double x, final Double y) {
		Log.d(TAG, "Syncing by adding a point. " + x + ", " + y);
		mData.add(x, y);
		reconfigure();
		repaint();
	}

	public void sync(List<PointD> points) {
		Log.d(TAG, "Syncing by replacing the list of points, new length is "
				+ points.size());
		synchronized (mData) {
			clear();
			for (PointD point : points) {
				mData.add(point.x, point.y);
			}
		}
		reconfigure();
		repaint();
	}

	public void sync(final SleepSession sleepRecord) {
		Log.d(TAG, "Attempting to sync with sleep record: " + sleepRecord);

		mData.set(com.androsz.electricsleepbeta.util.PointD
				.convertToNew(sleepRecord.getData()));

		// TODO this need to take into account timezone information.
		if (mShowTitle) {
			mRenderer.setChartTitle(sleepRecord.getTitle(mContext));
		}

		// setCalibrationLevel currently reconfigures and repaints.
		// if that changes, we will need a reconfigure and repaint here too.
		setCalibrationLevelAndRedraw(sleepRecord.getCalibrationLevel());
	}

	public void clear() {
		mData.clear();
		repaint();
	}

	/**
	 * Set the chart's axis format based upon current duration of the data.
	 */
	private void setupChartAxisFormat() {

		mRenderer.setShowLabels(mShowLabels);
		final double duration;
		String axisFormat;
		duration = mData.getDuration();

		final int MSEC_PER_MINUTE = 1000 * 60;
		final int MSEC_PER_HOUR = MSEC_PER_MINUTE * 60;

		if (duration > (15 * MSEC_PER_MINUTE)) {
			axisFormat = "h:mm";
		} else {
			axisFormat = "h:mm:ss";
		}
		if (!axisFormat.equals(mAxisFormat)) {
			mAxisFormat = axisFormat;
			try {
				((TimeChart) mChart).setDateFormat(mAxisFormat);
			} catch (ClassCastException cce) {
				Log.w(TAG,
						"Could not set the SleepChart's axis format because the underlying chart is not a TimeChart.");
			}
		}
	}

	/**
	 * Helper method that initializes charting after insertion of data.
	 */
	private void setupData() {
		synchronized (mData) {

			// remove all existing series
			for (int i = 0; i < mDataset.getSeriesCount(); i++) {
				mDataset.removeSeries(i);
			}
			for (int i = 0; i < mRenderer.getSeriesRendererCount(); i++) {
				mRenderer
						.removeSeriesRenderer(mRenderer.getSeriesRendererAt(i));
			}

			// add series to the dataset and renderer
			mData.attachToDataset(mDataset);
			mData.attachToRenderer(mRenderer);
		}

		final float textSize = MathUtils.calculatePxFromSp(mContext, 14);
		mRenderer.setChartTitleTextSize(textSize);
		mRenderer.setAxisTitleTextSize(textSize);
		mRenderer.setLabelsTextSize(textSize);

		mRenderer.setAntialiasing(true);
		mRenderer.setFitLegend(true);
		mRenderer.setLegendTextSize(textSize);
		mRenderer.setXLabels(mXLabelTicks);
		mRenderer.setYLabels(8);
		mRenderer.setYLabelsAlign(Align.RIGHT);

		setupStyle();
		// this ensures that labels are not drawn until we have data.
		mRenderer.setShowLabels(false);
	}

	/**
	 * Iterate over the known attributes for this view setting our chart to the
	 * desired settings.
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

		mData.setSeriesColors(mMovementColor, mMovementBorderColor,
				mCalibrationColor, mCalibrationBorderColor);

		// SleepChart_showGrid
		mRenderer.setShowGrid(mShowGrid);

		Resources res = mContext.getResources();
		int[] margins = mRenderer.getMargins();
		mRenderer.setShowLabels(mShowLabels);
		if (mShowLabels) {
			try {
				margins[1] += res.getDimension(R.dimen.sleep_chart_left_margin);
			} catch (android.content.res.Resources.NotFoundException e) {
				margins[1] += DEFAULT_LEFT_MARGIN; // increase left margin
			}
		}
		mRenderer.setShowLegend(mShowLegend);
		if (mShowLegend) {
			try {
				margins[2] += res
						.getDimension(R.dimen.sleep_chart_bottom_margin);
			} catch (android.content.res.Resources.NotFoundException e) {
				margins[2] += DEFAULT_BOTTOM_MARGIN; // increase bottom margin
			}
		}
		if (mShowTitle) {
			try {
				margins[0] += res.getDimension(R.dimen.sleep_chart_top_margin);
			} catch (android.content.res.Resources.NotFoundException e) {
				margins[0] += DEFAULT_TOP_MARGIN; // increase top margin
			}
		}
		mRenderer.setMargins(margins);
	}
}