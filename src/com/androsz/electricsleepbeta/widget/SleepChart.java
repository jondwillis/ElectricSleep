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
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.util.MathUtils;
import com.androsz.electricsleepbeta.util.PointD;

public class SleepChart extends GraphicalView implements Parcelable {

	protected double calibrationLevel;// =
										// SettingsActivity.DEFAULT_ALARM_SENSITIVITY;

	public int rating;

	public XYMultipleSeriesDataset xyMultipleSeriesDataset;

	public XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

	public XYSeries xySeriesCalibration;
	public XYSeriesRenderer xySeriesCalibrationRenderer;

	public XYSeries xySeriesMovement;

	public XYSeriesRenderer xySeriesMovementRenderer;

    private boolean mSetScroll;
    private int mBackgroundColor;

    public SleepChart(final Context context) {
		this(context, null);
	}

	public SleepChart(final Context context, final AttributeSet attrs) {
		this(context, attrs, 0);
    }

    public SleepChart(final Context context, final AttributeSet attrs, int defStyle) {
        super(context, attrs);
        // After this point buildChart() should have been invoked and the various renders and series
        // should have been populated.

        // Now begin processing attributes
        final Resources resources = context.getResources();
        final TypedArray array =
            context.obtainStyledAttributes(attrs, R.styleable.SleepChart, defStyle, 0);

        // background color processing
        if (array.hasValue(R.styleable.SleepChart_backgroundColor)) {
            int backgroundColor =
                array.getColor(R.styleable.SleepChart_backgroundColor, R.color.background_dark);
            xyMultipleSeriesRenderer.setBackgroundColor(backgroundColor);
            xyMultipleSeriesRenderer.setMarginsColor(backgroundColor);
            xyMultipleSeriesRenderer.setApplyBackgroundColor(true);
        } else {
            xyMultipleSeriesRenderer.setBackgroundColor(Color.TRANSPARENT);
            xyMultipleSeriesRenderer.setMarginsColor(Color.TRANSPARENT);
            xyMultipleSeriesRenderer.setApplyBackgroundColor(true);
        }

        // inside scrollview flag
        if (array.getBoolean(R.styleable.SleepChart_setScroll, false)) {
            xyMultipleSeriesRenderer.setInScroll(true);
        }
    }

	@Override
	protected AbstractChart buildChart() {
		if (xySeriesMovement == null) {
			Context context = getContext();
			// set up sleep movement series/renderer
			xySeriesMovement = new XYSeries(context.getString(R.string.legend_movement));
			xySeriesMovementRenderer = new XYSeriesRenderer();

			xySeriesMovementRenderer.setFillBelowLine(true);
			xySeriesMovementRenderer.setFillBelowLineColor(context.getResources().getColor(
					R.color.primary_dark_transparent));
			xySeriesMovementRenderer.setColor(context.getResources().getColor(R.color.primary_dark));

			// set up calibration line series/renderer
			xySeriesCalibration = new XYSeries(
					context.getString(R.string.legend_light_sleep_trigger));
			xySeriesCalibrationRenderer = new XYSeriesRenderer();
			xySeriesCalibrationRenderer.setFillBelowLine(true);
			xySeriesCalibrationRenderer.setFillBelowLineColor(context.getResources().getColor(
					R.color.background_transparent_lighten));
			xySeriesCalibrationRenderer.setColor(Color.WHITE);

			// add series to the dataset
			xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
			xyMultipleSeriesDataset.addSeries(xySeriesMovement);
			xyMultipleSeriesDataset.addSeries(xySeriesCalibration);

			// set up the dataset renderer
			xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
			xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesMovementRenderer);
			xyMultipleSeriesRenderer.addSeriesRenderer(xySeriesCalibrationRenderer);

			xyMultipleSeriesRenderer.setPanEnabled(false, false);
			xyMultipleSeriesRenderer.setZoomEnabled(false, false);
			final float textSize = MathUtils.calculatePxFromSp(context, 14);
			xyMultipleSeriesRenderer.setChartTitleTextSize(textSize);
			xyMultipleSeriesRenderer.setAxisTitleTextSize(textSize);
			xyMultipleSeriesRenderer.setLabelsTextSize(textSize);
			//xyMultipleSeriesRenderer.setLegendHeight((int) (MathUtils
			//		.calculatePxFromDp(context, 30) + textSize*3));
			xyMultipleSeriesRenderer.setAntialiasing(true);
			xyMultipleSeriesRenderer.setFitLegend(true);
			int[] margins = xyMultipleSeriesRenderer.getMargins();
			margins[2] += 20; // increase bottom margin
			xyMultipleSeriesRenderer.setMargins(margins);
			xyMultipleSeriesRenderer.setLegendTextSize(textSize);
			xyMultipleSeriesRenderer.setShowLegend(true);
			xyMultipleSeriesRenderer.setShowLabels(true);
			xyMultipleSeriesRenderer.setXLabels(6);
			xyMultipleSeriesRenderer.setYLabels(0);
			xyMultipleSeriesRenderer.setShowGrid(true);
			//TODO
			//xyMultipleSeriesRenderer.setAxesColor(context.getResources().getColor(R.color.text_));
			xyMultipleSeriesRenderer.setLabelsColor(xyMultipleSeriesRenderer.getAxesColor());
			final TimeChart timeChart = new TimeChart(xyMultipleSeriesDataset,
					xyMultipleSeriesRenderer);
			timeChart.setDateFormat("h:mm:ss");
			return timeChart;
		}
		return null;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public double getCalibrationLevel() {
		return calibrationLevel;
	}

	public boolean makesSenseToDisplay() {
		return xySeriesMovement.getItemCount() > 1;
	}

	public void reconfigure() {
		if (makesSenseToDisplay()) {
			final double firstX = xySeriesMovement.getX(0);
			final double lastX = xySeriesMovement.getX(xySeriesMovement.getItemCount() - 1);

			//if (makesSenseToDisplay()) {
				// reconfigure the calibration line..
				xySeriesCalibration.clear();

				xySeriesCalibration.add(firstX, calibrationLevel);
				xySeriesCalibration.add(lastX, calibrationLevel);
			//}

			final int MINUTE_IN_MS = 1000 * 60;
			final int HOUR_IN_MS = MINUTE_IN_MS * 60;
			/*if (lastX - firstX > HOUR_IN_MS*2) {
				((TimeChart) mChart).setDateFormat("h");
				xyMultipleSeriesRenderer.setXLabels(8);
			}else if (lastX - firstX > MINUTE_IN_MS*3) {
				((TimeChart) mChart).setDateFormat("h:mm");
				xyMultipleSeriesRenderer.setXLabels(5);
			}*/

			xyMultipleSeriesRenderer.setXAxisMin(firstX);
			xyMultipleSeriesRenderer.setXAxisMax(lastX);

			xyMultipleSeriesRenderer.setYAxisMin(0);
			xyMultipleSeriesRenderer.setYAxisMax(SettingsActivity.MAX_ALARM_SENSITIVITY);
		}
	}

	public void setCalibrationLevel(final double calibrationLevel) {
		this.calibrationLevel = calibrationLevel;
	}

    public void setScroll(boolean scroll) {
        mSetScroll = scroll;
    }

	public void sync(final Cursor cursor) throws StreamCorruptedException,
			IllegalArgumentException, IOException, ClassNotFoundException {
		sync(new SleepSession(cursor));
	}

	public void sync(final Double x, final Double y, final double alarm) {
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
		xySeriesMovement.setXY(PointD.convertToNew(sleepRecord.chartData));
		calibrationLevel = sleepRecord.alarm;

		rating = sleepRecord.rating;

		xyMultipleSeriesRenderer.setChartTitle(sleepRecord.title);
		reconfigure();
		repaint();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
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
		xySeriesMovement.clear();
		xySeriesCalibration.clear();
	}
}