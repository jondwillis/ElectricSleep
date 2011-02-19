package com.androsz.electricsleep.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleepdonate.R;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.db.SleepRecord;
import com.androsz.electricsleep.widget.SleepChart;

public class ReviewSleepActivity extends CustomTitlebarTabActivity {

	private class DeleteSleepTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
			final SleepHistoryDatabase shdb = new SleepHistoryDatabase(
					ReviewSleepActivity.this);
			shdb.deleteRow(Long.parseLong(uri.getLastPathSegment()));
			shdb.close();
			return null;
		}

		@Override
		protected void onPostExecute(final Void results) {
			Toast.makeText(ReviewSleepActivity.this,
					getString(R.string.deleted_sleep_record),
					Toast.LENGTH_SHORT).show();

			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			finish();
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(getString(R.string.deleting_sleep));
			progress.show();
		}
	}

	private class LoadSleepChartTask extends AsyncTask<Uri, Void, Cursor> {

		@Override
		protected Cursor doInBackground(Uri... params) {
			return managedQuery(params[0], null, null, null, null);
		}

		@Override
		protected void onPostExecute(final Cursor cursor) {
			if (cursor == null) {
				finish();
				return;
			}
			cursor.moveToFirst();

			final SleepRecord sleepRecord = new SleepRecord(cursor);

			((TextView) findViewById(R.id.value_score_text))
					.setText(sleepRecord.getSleepScore() + "%");
			((TextView) findViewById(R.id.value_duration_text))
					.setText(sleepRecord.getDurationText(getResources()));
			((TextView) findViewById(R.id.value_spikes_text))
					.setText(sleepRecord.spikes + "");
			((TextView) findViewById(R.id.value_fell_asleep_text))
					.setText(sleepRecord.getFellAsleepText(getResources()));
			((TextView) findViewById(R.id.value_note_text))
					.setText(sleepRecord.note);

			((RatingBar) findViewById(R.id.value_rating_bar))
					.setRating(sleepRecord.rating);

			sleepChart.sync(sleepRecord);
		}

		@Override
		protected void onPreExecute() {
			sleepChart = (SleepChart) findViewById(R.id.sleep_movement_chart);
		}
	}

	ProgressDialog progress;
	private SleepChart sleepChart;

	LoadSleepChartTask loadSleepChartTask;

	private Uri uri;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_review_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progress = new ProgressDialog(this);
		showTitleButton1(android.R.drawable.ic_menu_delete);
		addTab(R.id.sleep_movement_chart, R.string.sleep_chart);
		addTab(R.id.sleep_analysis_table, R.string.analysis);
		uri = getIntent().getData();
	}

	@Override
	public void onPause() {
		super.onPause();

		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}

		if (loadSleepChartTask != null) {
			loadSleepChartTask.cancel(true);
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		uri = Uri.parse(savedState.getString("uri"));
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (loadSleepChartTask != null) {
			loadSleepChartTask.cancel(true);
		}
		loadSleepChartTask = new LoadSleepChartTask();
		loadSleepChartTask.execute(uri);
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("uri", uri.toString());
	}

	public void onTitleButton1Click(final View v) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(
				ReviewSleepActivity.this)
				.setMessage(getString(R.string.delete_sleep_record))
				.setPositiveButton(getString(R.string.ok),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								new DeleteSleepTask().execute(null, null, null);
							}
						})
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		dialog.show();
	}
}
