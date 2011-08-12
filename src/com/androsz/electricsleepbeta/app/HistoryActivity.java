package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepContentProvider;
import com.androsz.electricsleepbeta.db.SleepHistoryDatabase;
import com.androsz.electricsleepbeta.db.SleepRecord;
import com.androsz.electricsleepbeta.util.DeviceUtil;
import com.androsz.electricsleepbeta.widget.SleepHistoryCursorAdapter;

public class HistoryActivity extends HostActivity {

	private class DeleteSleepTask extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(final Long... params) {
			final SleepHistoryDatabase shdb = new SleepHistoryDatabase(
					HistoryActivity.this);
			shdb.deleteRow(params[0]);
			shdb.close();
			return null;
		}

		@Override
		protected void onPostExecute(final Void results) {
			mListView.removeAllViewsInLayout();
			new QuerySleepTask().execute(searchFor);
			Toast.makeText(HistoryActivity.this,
					getString(R.string.deleted_sleep_record),
					Toast.LENGTH_SHORT).show();

			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(getString(R.string.deleting_sleep));
			progress.show();
		}
	}

	private class QuerySleepTask extends AsyncTask<String, Void, Cursor> {

		@Override
		protected Cursor doInBackground(final String... params) {
			if (params[0] == null) {
				searchFor = params[0];
			}

			return managedQuery(SleepContentProvider.CONTENT_URI, null, null,
					new String[] { searchFor }, SleepRecord.KEY_TITLE);
		}

		@Override
		protected void onPostExecute(final Cursor cursor) {
			if (cursor == null) {
				// There are no results
				mTextView.setVisibility(View.VISIBLE);
				mTextView.setText(getString(R.string.no_results));
				mListView.setVisibility(View.GONE);
			} else {
				mTextView.setVisibility(View.GONE);
				mListView.setVisibility(View.VISIBLE);

				final SleepHistoryCursorAdapter sleepHistory = new SleepHistoryCursorAdapter(
						HistoryActivity.this, cursor);
				mListView.setAdapter(sleepHistory);
				// mListView.setWillNotCacheDrawing(true);
				if (DeviceUtil.getCpuClockSpeed() >= 600) {
					// anything faster than a
					// droid *should* be
					// able to handle smooth
					// scrolling
					mListView.setScrollingCacheEnabled(false);
				}

				mListView
						.setOnItemLongClickListener(new OnItemLongClickListener() {

							@Override
							public boolean onItemLongClick(
									final AdapterView<?> parent,
									final View view, final int position,
									final long rowId) {
								final AlertDialog.Builder dialog = new AlertDialog.Builder(
										HistoryActivity.this)
										.setMessage(
												getString(R.string.delete_sleep_record))
										.setPositiveButton(
												getString(R.string.ok),
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															final DialogInterface dialog,
															final int id) {

														new DeleteSleepTask()
																.execute(rowId,
																		null,
																		null);
													}
												})
										.setNegativeButton(
												getString(R.string.cancel),
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															final DialogInterface dialog,
															final int id) {
														dialog.cancel();
													}
												});
								dialog.show();
								return true;
							}

						});

				// Define the on-click listener for the list items
				mListView.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(final AdapterView<?> parent,
							final View view, final int position, final long id) {
						// Build the Intent used to open WordActivity with a
						// specific word Uri
						final Intent reviewSleepIntent = new Intent(
								getApplicationContext(),
								ReviewSleepActivity.class);
						final Uri data = Uri.withAppendedPath(
								SleepContentProvider.CONTENT_URI,
								String.valueOf(id));
						reviewSleepIntent.setData(data);
						startActivity(reviewSleepIntent);
					}
				});
			}
			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			progress.setMessage(getString(R.string.querying_sleep_database));
			progress.show();
		}
	}

	public static final String SEARCH_FOR = "searchFor";

	ProgressDialog progress;

	private TextView mTextView;

	private ListView mListView;

	String searchFor = null;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		progress = new ProgressDialog(HistoryActivity.this);

		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);

		mListView.setVerticalFadingEdgeEnabled(false);
		mListView.setScrollbarFadingEnabled(false);

		final Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			final Intent reviewIntent = new Intent(this,
					ReviewSleepActivity.class);
			reviewIntent.setData(intent.getData());
			startActivity(reviewIntent);
			finish();
		} else {
			// set searchFor parameter if it exists
			searchFor = intent.getStringExtra(SEARCH_FOR);
			if (searchFor != null) {
				HistoryActivity.this.setTitle(HistoryActivity.this.getTitle()
						+ " " + searchFor);
				// do exact searches only.
				searchFor = "\"" + searchFor + "\"";
			} else {
				searchFor = getString(R.string.to);
			}
			new QuerySleepTask().execute(searchFor);
		}
	}

	@Override
	public void onPause() {
		super.onPause();

		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
	}
}
