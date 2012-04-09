package com.androsz.electricsleepbeta.app;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;

public class DeleteSleepTask extends AsyncTask<Long, Void, Void> {

    /**
	 *
	 */
    private final Context context;
    private ProgressDialog progress;

    /**
     * @param context
     * @param progress
     *            pass a non-null if you want a ProgressDialog to be managed by
     *            this Task's lifecycle
     */
    DeleteSleepTask(Context context) {
        this.context = context;
        this.progress = new ProgressDialog(context);
    }

    @Override
    protected Void doInBackground(final Long... params) {
        for (Long rowId : params) {
            if (rowId != null) {
                context.getContentResolver().delete(SleepSession.CONTENT_URI,
                        SleepSession._ID + " =? ",
                        new String[] { Long.toString(rowId) });
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(final Void results) {
        // mListView.removeAllViewsInLayout();
        // getSupportLoaderManager().restartLoader(0,
        // getLoaderArgs(getIntent(), false), HistoryActivity.this);
        Toast.makeText(this.context,
                this.context.getString(R.string.deleted_sleep_record),
                Toast.LENGTH_SHORT).show();

        if (this.progress != null && this.progress.isShowing()) {
            this.progress.dismiss();
        }
    }

    @Override
    protected void onPreExecute() {
        if (this.progress != null) {
            this.progress.setMessage(this.context
                    .getString(R.string.deleting_sleep));
            this.progress.show();
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        if (this.progress != null && this.progress.isShowing()) {
            this.progress.dismiss();
        }
    }

}