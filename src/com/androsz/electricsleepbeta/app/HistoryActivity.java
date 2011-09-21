package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSessions;
import com.androsz.electricsleepbeta.widget.SleepHistoryCursorAdapter;

public class HistoryActivity extends HostActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private class DeleteSleepTask extends AsyncTask<Long, Void, Void> {

		@Override
		protected Void doInBackground(final Long... params) {
			SleepSessions.deleteSession(HistoryActivity.this, params[0]);
			return null;
		}

		@Override
		protected void onPostExecute(final Void results) {
			// mListView.removeAllViewsInLayout();
			// getSupportLoaderManager().restartLoader(0,
			// getLoaderArgs(getIntent(), false), HistoryActivity.this);
			Toast.makeText(HistoryActivity.this, getString(R.string.deleted_sleep_record),
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

	private final class ListOnItemClickListener implements OnItemClickListener {
		@Override
		public void onItemClick(final AdapterView<?> parent, final View view, final int position,
				final long id) {

			final Intent reviewSleepIntent = new Intent(HistoryActivity.this,
					ReviewSleepActivity.class);

			final Uri data = Uri.withAppendedPath(SleepSessions.MainTable.CONTENT_URI,
					String.valueOf(id));
			reviewSleepIntent.setData(data);
			startActivity(reviewSleepIntent);
		}
	}

	public static final String SEARCH_FOR = "searchFor";

	private ListView mListView;

	private TextView mTextView;

	ProgressDialog progress;
	private SleepHistoryCursorAdapter sleepHistoryAdapter;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}

	private Bundle getLoaderArgs(final Intent intent, boolean init) {
		// set searchFor parameter if it exists
		String searchFor = intent.getStringExtra(SEARCH_FOR);
		if (searchFor != null) {
			if (init) {
				HistoryActivity.this.setTitle(HistoryActivity.this.getTitle() + " " + searchFor);
			}
			// do exact searches only.
			searchFor = "\"" + searchFor + "\"";
		} else {
			searchFor = getString(R.string.to);
		}
		final Bundle args = new Bundle();
		args.putString(SEARCH_FOR, searchFor);
		return args;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		progress = new ProgressDialog(HistoryActivity.this);

		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);

		mListView.setVerticalFadingEdgeEnabled(false);
		mListView.setScrollbarFadingEnabled(false);

		sleepHistoryAdapter = new SleepHistoryCursorAdapter(HistoryActivity.this, null);

		mListView.setAdapter(sleepHistoryAdapter);

		final Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			final Intent reviewIntent = new Intent(this, ReviewSleepActivity.class);
			reviewIntent.setData(intent.getData());
			startActivity(reviewIntent);
			finish();
		} else {
			getSupportLoaderManager().initLoader(0, getLoaderArgs(intent, true), this);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		progress.setMessage(getString(R.string.querying_sleep_database));
		progress.show();
		return new CursorLoader(this, SleepSessions.MainTable.CONTENT_URI,
				SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, SleepSessions.MainTable.KEY_TITLE
						+ " MATCH ?", new String[] { args.getString(SEARCH_FOR) + "*" }, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_multiple_history, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		sleepHistoryAdapter.swapCursor(null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data == null) {
			// There are no results
			mTextView.setVisibility(View.VISIBLE);
			mTextView.setText(getString(R.string.no_results));
			mListView.setVisibility(View.GONE);
		} else {
			if (data.getCount() == 1) {
				mListView.getAdapter().getItem(0);
			}
			sleepHistoryAdapter.swapCursor(data);
			mTextView.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);

			mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(final AdapterView<?> parent, final View view,
						final int position, final long rowId) {
					final AlertDialog.Builder dialog = new AlertDialog.Builder(HistoryActivity.this)
							.setMessage(getString(R.string.delete_sleep_record))
							.setPositiveButton(getString(R.string.ok),
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(final DialogInterface dialog,
												final int id) {

											new DeleteSleepTask().execute(rowId, null, null);
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
					return true;
				}

			});

			// Define the on-click listener for the list items
			mListView.setOnItemClickListener(new ListOnItemClickListener());
		}
		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_delete_all:
			// TODO
			break;
		case R.id.menu_item_export_all:
			// TODO
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();

		if (progress != null && progress.isShowing()) {
			progress.dismiss();
		}
	}

}
