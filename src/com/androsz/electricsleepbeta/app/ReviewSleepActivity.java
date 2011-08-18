package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepHistoryDatabase;
import com.androsz.electricsleepbeta.db.SleepRecord;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class ReviewSleepActivity extends HostActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {
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

	ProgressDialog progress;
	private SleepChart sleepChart;

	private Uri uri;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_review_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		progress = new ProgressDialog(this);
		ActionBar bar = getSupportActionBar();
		bar.addTab(bar.newTab().setText(R.string.sleep_chart)); // layout
																// R.id.sleep_movement_chart
		bar.addTab(bar.newTab().setText(R.string.analysis));// addTab(R.id.sleep_analysis_table,
															// R.string.analysis);
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		uri = getIntent().getData();

		getSupportLoaderManager().initLoader(0, null, this);
/*
		new AsyncTask<Uri, Void, Cursor>() {

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
				boolean worked = cursor.moveToFirst();
				if (worked) {
					final SleepRecord sleepRecord = new SleepRecord(cursor);

					((TextView) findViewById(R.id.value_score_text))
							.setText(sleepRecord.getSleepScore() + "%");
					((TextView) findViewById(R.id.value_duration_text))
							.setText(sleepRecord
									.getDurationText(getResources()));
					((TextView) findViewById(R.id.value_spikes_text))
							.setText(sleepRecord.spikes + "");
					((TextView) findViewById(R.id.value_fell_asleep_text))
							.setText(sleepRecord
									.getFellAsleepText(getResources()));
					((TextView) findViewById(R.id.value_note_text))
							.setText(sleepRecord.note);

					((RatingBar) findViewById(R.id.value_rating_bar))
							.setRating(sleepRecord.rating);

					sleepChart = (SleepChart) findViewById(R.id.sleep_movement_chart);
					sleepChart.sync(sleepRecord);
				}
			}
		}.execute(uri);*/
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_review_sleep, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		uri = Uri.parse(savedState.getString("uri"));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("uri", uri.toString());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_delete_sleep_record:
			final AlertDialog.Builder dialog = new AlertDialog.Builder(
					ReviewSleepActivity.this)
					.setMessage(getString(R.string.delete_sleep_record))
					.setPositiveButton(getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									new DeleteSleepTask().execute(null, null,
											null);
								}
							})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			dialog.show();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, uri, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		data.moveToFirst();
		final SleepRecord sleepRecord = new SleepRecord(data);

		((TextView) findViewById(R.id.value_score_text)).setText(sleepRecord
				.getSleepScore() + "%");
		((TextView) findViewById(R.id.value_duration_text)).setText(sleepRecord
				.getDurationText(getResources()));
		((TextView) findViewById(R.id.value_spikes_text))
				.setText(sleepRecord.spikes + "");
		((TextView) findViewById(R.id.value_fell_asleep_text))
				.setText(sleepRecord.getFellAsleepText(getResources()));
		((TextView) findViewById(R.id.value_note_text))
				.setText(sleepRecord.note);

		((RatingBar) findViewById(R.id.value_rating_bar))
				.setRating(sleepRecord.rating);

		sleepChart = (SleepChart) findViewById(R.id.sleep_movement_chart);
		sleepChart.sync(sleepRecord);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
