package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActionBar;
import android.support.v4.app.ActionBar.Tab;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.SleepChart;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ReviewSleepActivity extends HostActivity
    implements LoaderManager.LoaderCallbacks<Cursor>, ActionBar.TabListener {

    private static final int LOADER_SLEEP = 0;

    private ReviewSleepFragment mSleepFragment;
    private SleepSession mSleepRecord;

    private class DeleteSleepTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(final Void... params) {
			if (getIntent().getData() != null) {

                ReviewSleepActivity.this.getContentResolver().delete(
                    SleepSession.CONTENT_URI,
                    SleepSession._ID + " =? ",
                    new String[] {Long.toString(ContentUris.parseId(getIntent().getData()))});
			} else {
				Toast.makeText(ReviewSleepActivity.this, "Wait for the sleep session to load.",
						Toast.LENGTH_LONG).show();
			}
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

	//ReviewSleepAnalysisFragment analysisFragment;
    //ReviewSleepChartFragment chartFragment;

	ProgressDialog progress;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_review_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        mSleepFragment = new ReviewSleepFragment();
        getSupportFragmentManager().beginTransaction()
            .replace(android.R.id.content, mSleepFragment)
            .commit();

        /*
        FragmentTransaction
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
		}
        */
		getSupportLoaderManager().initLoader(LOADER_SLEEP, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, getIntent().getData(),
				null, null, null, null);
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
		if (data.moveToLast()) {
			this.getIntent().setData(ContentUris.withAppendedId(SleepSession.CONTENT_URI,
                                                                data.getLong(0)));
            mSleepRecord = new SleepSession(data);
            mSleepFragment.setSleepRecord(mSleepRecord);
            //chartFragment.setSleepRecord(mSleepRecord);
			//analysisFragment.setSleepRecord(mSleepRecord);

		} else {
			Toast.makeText(this,
					"Could not display the correct Sleep record. This error has been reported.",
					Toast.LENGTH_LONG).show();
			trackEvent("ReviewSleepActivity couldn't data.moveToFirst()", 0);
		}
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
		case R.id.menu_item_share_sleep_record:
            if (mSleepRecord == null) {
                // Emit error if user attempts to share when no record is loaded.
                Toast.makeText(this,
                               R.string.unfortunately_no_sleep_record_was_available_for_sharing,
                               Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            final String dateString =
                DateUtils.formatDateTime(this,
                                         mSleepRecord.getStartTimestamp(),
                                         DateUtils.FORMAT_NO_YEAR |
                                         DateUtils.FORMAT_SHOW_DATE |
                                         DateUtils.FORMAT_ABBREV_ALL);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.see_how_i_slept_on) +
                            " " + dateString);
            StringBuilder builder = new StringBuilder();
            builder.append(getString(R.string.see_how_i_slept_on) + " " + dateString + ".\n");
            builder.append(getString(R.string.try_out_zeo));
            intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
            intent.setType("text/plain");
            try {
                final String filename = "zeo_actigraphy_detail.png";
                File screenshotFile;
                String storageState = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(storageState) &&
                    !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
                    // We only attempt to use external storage if its mounted and NOT read only as
                    // we must write our screenshots there.
                    File screenshotDirectory =
                        new File(Environment.getExternalStorageDirectory(),
                                 "/Android/data/com.androsz.electricsleepbeta/tmp/");
                    screenshotDirectory.mkdirs();
                    // Attempt to store the night detail screenshot on the SD card if possible.
                    screenshotFile = new File(screenshotDirectory, filename);
                    FileOutputStream os = new FileOutputStream(screenshotFile);
                    // Inflate the layout used for sharing night details with others.
                    LayoutInflater inflater = getLayoutInflater();
                    View shareView = inflater.inflate(R.layout.share_sleep, null);
                    SleepChart chart =
                        (SleepChart) shareView.findViewById(R.id.sleep_movement_chart);
                    chart.sync(mSleepRecord);
                    shareView.setLayoutParams(
                        new LinearLayout.LayoutParams(800, 800));
                    //shareView.setMinimumHeight(800);

                    // Begin process of drawing night sharing to bitmap.
                    shareView.measure(
                        View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY));
                    shareView.layout(0, 0, 800, 800);
                    //shareView.setBackgroundColor(getResources().getColor(R.color.share_background));
                    Bitmap bitmap =
                        Bitmap.createBitmap(shareView.getWidth(), shareView.getHeight(),
                                            Bitmap.Config.ARGB_8888);
                    shareView.draw(new Canvas(bitmap));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.close();
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(screenshotFile));
                    intent.setType("image/png");
                } else {
                    // Warn user that sharing with other apps is not possible without external
                    // storage.
                    Toast.makeText(this,
                                   R.string.i_am_sorry_but_cannot_share_zeo_sleep_without_sdcard,
                                   Toast.LENGTH_LONG).show();
                    return true;
                }
            } catch (IOException e) {
                Toast.makeText(
                    this,
                    R.string.oops_there_was_error_while_generating_image_for_sharing,
                    Toast.LENGTH_LONG).show();
            }
            startActivity(Intent.createChooser(intent, "Share Night of Sleep"));
            return true;
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

        /*
		ft = getSupportFragmentManager().beginTransaction();
		if (tab.getPosition() == 0) {
			ft.replace(R.id.frags, chartFragment);
		} else {
			ft.replace(R.id.frags, analysisFragment);
		}
		ft.commit();
        */
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
	}
}
