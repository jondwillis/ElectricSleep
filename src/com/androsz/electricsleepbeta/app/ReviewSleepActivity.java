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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepHistoryDatabase;
import com.androsz.electricsleepbeta.db.SleepRecord;

public class ReviewSleepActivity extends HostActivity implements
		LoaderManager.LoaderCallbacks<Cursor>, ActionBar.TabListener {

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

		ActionBar bar = getSupportActionBar();
		bar.addTab(bar.newTab().setText(R.string.sleep_chart)
				.setTabListener(this));
		bar.addTab(bar.newTab().setText(R.string.analysis).setTabListener(this));
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// If selectedTab is not saved to the savedInstanceState,
		// 0 is returned by default.
		if (savedInstanceState != null) {
			int selectedTab = savedInstanceState.getInt("selectedTab");
			bar.setSelectedNavigationItem(selectedTab);
			uri = Uri.parse(savedInstanceState.getString("uri"));
		} else {
			uri = getIntent().getData();
		}

		getSupportLoaderManager().initLoader(0, null, this);

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
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("uri", uri.toString());
		ActionBar bar = getSupportActionBar();
		int selectedTab = bar.getSelectedTab().getPosition();
		outState.putInt("selectedTab", selectedTab);
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

	ReviewSleepChartFragment chartFragment;
	ReviewSleepAnalysisFragment analysisFragment;

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		data.moveToFirst();
		SleepRecord sleepRecord = new SleepRecord(data);

		chartFragment.setSleepRecord(sleepRecord);
		analysisFragment.setSleepRecord(sleepRecord);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
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
