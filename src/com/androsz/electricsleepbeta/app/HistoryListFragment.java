package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.SleepHistoryCursorAdapter;

public class HistoryListFragment extends HostFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private final class ListOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(final AdapterView<?> parent, final View view,
                final int position, final long id) {

            final Intent reviewSleepIntent = new Intent(getActivity(),
                    ReviewSleepActivity.class);

            final Uri data = ContentUris.withAppendedId(
                    SleepSession.CONTENT_URI, id);
            reviewSleepIntent.setData(data);
            startActivity(reviewSleepIntent);
        }
    }

    /** Key used to mark what julian day to load a list for. Used by both the intent as well as the
     * cursor loader bundle.
     */
    public static final String EXTRA_JULIAN_DAY = "julian_day";

    /** Load all sleep sessions. */
    private static final int LOADER_ALL = 0;

    /** Load sleep sessions for a given julian day. */
    private static final int LOADER_JULIAN = 1;

    private ListView mListView;

    private TextView mTextView;
    
    ProgressDialog progressDialog;
    private SleepHistoryCursorAdapter sleepHistoryAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        //TODO doesn't seem possible without recreating the activity first.
        final View root = inflater.inflate(R.layout.fragment_history_list, container, false);
        progressDialog = new ProgressDialog(getActivity());
        
        mTextView = (TextView) root.findViewById(R.id.text_no_history);
        mListView = (ListView) root.findViewById(R.id.list);

        sleepHistoryAdapter = new SleepHistoryCursorAdapter(getActivity(), null);
        mListView.setAdapter(sleepHistoryAdapter);

       // mListView.setBackgroundColor(getActivity().getResources().getColor(R.color.background_light));

        final Intent intent = getActivity().getIntent();
        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // A single sleep session requested. Load review sleep activity and exit list view.
            final Intent reviewIntent = new Intent(getActivity(), ReviewSleepActivity.class);
            reviewIntent.setData(intent.getData());
            startActivity(reviewIntent);
            getActivity().finish();
        } else if (intent.hasExtra(EXTRA_JULIAN_DAY)) {
            // Loading all records for a julian day.
            final int julianDay = intent.getIntExtra(EXTRA_JULIAN_DAY, 0);
            final Bundle args = new Bundle(1);
            args.putInt(EXTRA_JULIAN_DAY, julianDay);
            getLoaderManager().initLoader(LOADER_JULIAN, args, this);
        } else {
            // Loading all known records.
            getLoaderManager().initLoader(LOADER_ALL, null, this);
        }

        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        progressDialog.setMessage(getString(R.string.querying_sleep_database));
        progressDialog.show();

        switch (id) {
        case LOADER_ALL:
            return new CursorLoader(getActivity(), SleepSession.CONTENT_URI,
                                    null, null, null, SleepSession.SORT_ORDER);

        case LOADER_JULIAN:
            final int julianDay = args.getInt(EXTRA_JULIAN_DAY);
            if (julianDay == 0) {
                return null;
            }
            return new CursorLoader(getActivity(), SleepSession.CONTENT_URI,
                                    null,
                                    SleepSession.START_JULIAN_DAY + " =? ",
                                    new String[] {Integer.toString(julianDay)},
                                    SleepSession.SORT_ORDER);
        }

        return null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history_list, menu);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        sleepHistoryAdapter.swapCursor(null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null) {
            dismissProgressDialogIfShowing();
            if (data.getCount() == 0) {
                mTextView.setVisibility(View.VISIBLE);
                mListView.setVisibility(View.GONE);
                return;
            } else if (data.getCount() == 1) {
                data.moveToFirst();
                final Intent reviewSleepIntent = new Intent(getActivity(),
                                                            ReviewSleepActivity.class);

                // WARNING: there is an assumption here that cursor index 0 is the primary key.
                final Uri uri = Uri.withAppendedPath(SleepSession.CONTENT_URI,
                                                     String.valueOf(data.getLong(0)));
                reviewSleepIntent.setData(uri);
                reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                           Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            sleepHistoryAdapter.swapCursor(data);
            mTextView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(final AdapterView<?> parent,
                        final View view, final int position, final long rowId) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity())
                            .setMessage(getString(R.string.delete_sleep_record))
                            .setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                final DialogInterface dialog,
                                                final int id) {

                                            new DeleteSleepTask(getActivity(), progressDialog)
                                                    .execute(
                                                            new Long[] { rowId },
                                                            null, null);
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
                    return true;
                }

            });

            // Define the on-click listener for the list items
            mListView.setOnItemClickListener(new ListOnItemClickListener());
        }
        
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_calendar:
            ((FragmentActivity) getActivity()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, new HistoryMonthFragment())
                    .commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();

        dismissProgressDialogIfShowing();
    }

	private void dismissProgressDialogIfShowing() {
		if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
	}

}
