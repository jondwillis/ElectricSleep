package com.androsz.electricsleep.app;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.view.SleepChartView;

public class ReviewSleepActivity extends CustomTitlebarActivity {

	private SleepChartView sleepChartView;

	private void addChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (layout.getChildCount() == 0) {
			if (sleepChartView == null) {
				sleepChartView = new SleepChartView(this);
			}
			layout.addView(sleepChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}

		final Uri uri = getIntent().getData();
		Cursor cursor;
		if (uri == null) {
			final long uriEnding = getIntent().getLongExtra("position", -1);
			cursor = managedQuery(SleepContentProvider.CONTENT_URI, null, null,
					new String[] { getString(R.string.to) },
					SleepHistoryDatabase.KEY_SLEEP_DATE_TIME + " DESC");
			if (cursor == null) {
				finish();
			} else {
				cursor.moveToPosition((int) uriEnding);
			}
		} else {
			cursor = managedQuery(uri, null, null, null, null);

			if (cursor == null) {
				finish();
			} else {
				cursor.moveToFirst();
			}
		}

		final int dateTimeIndex = cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATE_TIME);
		final int xIndex = cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_X);
		final int yIndex = cursor
				.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_Y);
		final int min = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MIN));
		final int max = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_MAX));
		final int alarm = cursor
				.getInt(cursor
						.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATA_ALARM));

		this.setTitle("Sleep: " + cursor.getString(dateTimeIndex));

		try {
			sleepChartView.syncByCopying((List<Double>) SleepHistoryDatabase
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
		} finally {
			cursor.close();
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_sleep;
	}

	@Override
	protected void onPause() {
		super.onPause();
		removeChartView();
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		try {
			super.onRestoreInstanceState(savedState);
			sleepChartView = (SleepChartView) savedState
					.getSerializable("sleepChartView");
		} catch (final RuntimeException re) {

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		new Thread(new Runnable() {

			@Override
			public void run() {
				addChartView();
			}
		}).run();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("sleepChartView", sleepChartView);
	}

	private void removeChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (sleepChartView.getParent() == layout) {
			layout.removeView(sleepChartView);
		}
	}
}
