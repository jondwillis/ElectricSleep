package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.db.SleepSessions;

public class ReviewSleepActivity extends HostActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, ActionBar.TabListener {

	private class DeleteSleepTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
			SleepSessions.deleteSession(ReviewSleepActivity.this, Long.parseLong(uri.getLastPathSegment()));
			return null;
		}

		@Override
		protected void onPostExecute(final Void results) {
			Toast.makeText(ReviewSleepActivity.this, getString(R.string.deleted_sleep_record),
					Toast.LENGTH_SHORT).show();

			if (progress != null && progress.isShowing()) {
				progress.dismiss();
			}
			finish();
		}

		@Override
		protected void onPreExecute() {
			getSupportLoaderManager().destroyLoader(0);
			progress.setMessage(getString(R.string.deleting_sleep));
			progress.show();
		}
	}

	ReviewSleepAnalysisFragment analysisFragment;

	ReviewSleepChartFragment chartFragment;

	ProgressDialog progress;

	private Uri uri;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_review_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_review_sleep);
		progress = new ProgressDialog(this);

		chartFragment = new ReviewSleepChartFragment();
		analysisFragment = new ReviewSleepAnalysisFragment();

		final ActionBar bar = getSupportActionBar();
		bar.addTab(bar.newTab().setText(R.string.sleep_chart).setTabListener(this));
		bar.addTab(bar.newTab().setText(R.string.analysis).setTabListener(this));
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// If selectedTab is not saved to the savedInstanceState,
		// 0 is returned by default.
		if (savedInstanceState != null) {
			final int selectedTab = savedInstanceState.getInt("selectedTab");
			bar.setSelectedNavigationItem(selectedTab);
			uri = Uri.parse(savedInstanceState.getString("uri"));
		} else {
			uri = getIntent().getData();
		}

		getSupportLoaderManager().initLoader(0, null, this);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, uri, SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, null,
				null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_review_sleep, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		loader.stopLoading();
		finish();
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data != null && !data.isFirst()) {
			data.moveToFirst();
		}
		final SleepSession sleepRecord = new SleepSession(data);

		chartFragment.setSleepRecord(sleepRecord);
		analysisFragment.setSleepRecord(sleepRecord);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_delete_sleep_record:
			final AlertDialog.Builder dialog = new AlertDialog.Builder(ReviewSleepActivity.this)
					.setMessage(getString(R.string.delete_sleep_record))
					.setPositiveButton(getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int id) {
									new DeleteSleepTask().execute(null, null, null);
								}
							})
					.setNegativeButton(getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int id) {
									dialog.cancel();
								}
							});
			dialog.show();
			break;
		case R.id.menu_item_export_sleep_record:
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

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("uri", uri.toString());
		final ActionBar bar = getSupportActionBar();
		final int selectedTab = bar.getSelectedTab().getPosition();
		outState.putInt("selectedTab", selectedTab);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {

	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		// ft passed here is always null... (not sure why, but its in the docs)

		ft = getSupportFragmentManager().beginTransaction();
		if (tab.getPosition() == 0) {
			ft.replace(R.id.frags, chartFragment);
		} else {
			ft.replace(R.id.frags, analysisFragment);
		}
		ft.commit();
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
}
