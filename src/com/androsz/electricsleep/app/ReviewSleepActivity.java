package com.androsz.electricsleep.app;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.view.SleepChartView;

public class ReviewSleepActivity extends CustomTitlebarActivity {

	private SleepChartView sleepChartView;
	private long rowId;

	private void addChartView() {
		sleepChartView = (SleepChartView) findViewById(R.id.sleep_movement_chart);

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
				rowId = cursor.getPosition();
				sleepChartView.syncWithCursor(cursor);
			}
		} else {
			cursor = managedQuery(uri, null, null, null, null);

			if (cursor == null) {
				finish();
			} else {
				rowId = Long.parseLong(uri.getLastPathSegment());
				showTitleButton1(android.R.drawable.ic_menu_delete);
				cursor.moveToFirst();
				sleepChartView.syncWithCursor(cursor);
			}
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onPause() {
		super.onPause();
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
		addChartView();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("sleepChartView", sleepChartView);
	}

	public void onTitleButton1Click(final View v) {
		final SleepHistoryDatabase shdb = new SleepHistoryDatabase(this);
		try {
			final AlertDialog.Builder dialog = new AlertDialog.Builder(
					ReviewSleepActivity.this)
					.setMessage(getString(R.string.delete_sleep_record))
					.setPositiveButton(getString(R.string.yes),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {

									shdb.deleteRow(rowId);
									Toast.makeText(
											ReviewSleepActivity.this,
											getString(R.string.deleted_sleep_record),
											Toast.LENGTH_SHORT).show();
									finish();
								}
							})
					.setNegativeButton(getString(R.string.no),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			dialog.show();
		} finally {
			shdb.close();
		}
	}
}
