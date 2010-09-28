package com.androsz.electricsleep.ui;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.achartengine.ChartView;
import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.ui.view.SleepChartReView;
import com.androsz.electricsleep.ui.view.SleepChartView;

public class ReviewSleepActivity extends CustomTitlebarActivity {

	private SleepChartReView sleepChartView;

	private void addChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (layout.getChildCount() == 0) {
			if (sleepChartView == null) {
				sleepChartView = new SleepChartReView(this);
			}
			layout.addView(sleepChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	private void removeChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (sleepChartView.getParent() == layout) {
			layout.removeView(sleepChartView);
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_sleep;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void onCreate(final Bundle savedInstanceState) {

		final Uri uri = getIntent().getData();
		final Cursor cursor = managedQuery(uri, null, null, null, null);

		if (cursor == null) {
			finish();
		} else {
			cursor.moveToFirst();

			final int dateTimeIndex = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATE_TIME);
			final int xIndex = cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_X);
			final int yIndex= cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_Y);
			final int min = cursor.getInt(cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MIN));
			final int max = cursor.getInt(cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MAX));
			final int alarm = cursor.getInt(cursor
					.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_ALARM));

			this.setTitle("Sleep: " + cursor.getString(dateTimeIndex));
			super.onCreate(savedInstanceState);

			addChartView();
			
			try {
				sleepChartView.syncByCopying(
						(List<Double>) SleepHistoryDatabase
								.byteArrayToObject(cursor.getBlob(xIndex)),
						(List<Double>) SleepHistoryDatabase
								.byteArrayToObject(cursor.getBlob(yIndex)), min,
						max, alarm);
			} catch (final StreamCorruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (final ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		sleepChartView = (SleepChartReView) savedState.getSerializable("sleepChartView");
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		removeChartView();
		outState.putSerializable("sleepChartView", sleepChartView);
	}
}
