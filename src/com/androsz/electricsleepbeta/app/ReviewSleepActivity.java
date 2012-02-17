package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItem;
import android.util.Log;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;

public class ReviewSleepActivity extends HostActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = ReviewSleepActivity.class.getSimpleName();

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
                        new String[] { Long.toString(ContentUris
                                .parseId(getIntent().getData())) });
            } else {
                Toast.makeText(ReviewSleepActivity.this,
                        "Wait for the sleep session to load.",
                        Toast.LENGTH_LONG).show();
            }
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
            getSupportLoaderManager().destroyLoader(0);
            if (progress == null) {
                progress = new ProgressDialog(ReviewSleepActivity.this);
            }
            progress.setMessage(getString(R.string.deleting_sleep));
            progress.show();
        }
    }

    ProgressDialog progress;

    @Override
    protected int getContentAreaLayoutId() {
        return R.layout.activity_review_sleep;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "Creating sleep fragment.");
        mSleepFragment = new ReviewSleepFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(android.R.id.content, mSleepFragment).commit();
        getSupportLoaderManager().initLoader(LOADER_SLEEP, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final Uri uri = getIntent().getData();
        Log.d(TAG, "Creating loader: " + uri);
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "Loader reset.");
        loader.stopLoading();
        finish();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "Load finished.");
        if (data.moveToLast()) {
            // WARNING - there is assumption here that the cursor's first column
            // is its primary key.
            getIntent().setData(
                    ContentUris.withAppendedId(SleepSession.CONTENT_URI,
                            data.getLong(0)));
            mSleepRecord = new SleepSession(data);
            mSleepFragment.setSleepRecord(mSleepRecord);
        } else {
            Toast.makeText(
                    this,
                    "Could not display the correct Sleep record. This error has been reported.",
                    Toast.LENGTH_LONG).show();
            trackEvent("ReviewSleepActivity couldn't data.moveToFirst()", 0);
        }
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
    public void onPause() {
        super.onPause();

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }
}
