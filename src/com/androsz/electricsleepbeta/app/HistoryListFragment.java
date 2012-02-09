package com.androsz.electricsleepbeta.app;

import android.app.Activity;
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
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
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

    public static final String SEARCH_FOR = "searchFor";

    private static final long TIMESTAMP_INVALID = -1;

    private ListView mListView;

    private TextView mTextView;

    ProgressDialog progress;
    private SleepHistoryCursorAdapter sleepHistoryAdapter;

    /*
     * TODO temporarily disabled. private Bundle getLoaderArgs(final Intent
     * intent, boolean init) { final Bundle args = new Bundle();
     * 
     * if (intent.hasExtra(SEARCH_FOR)) { final long timestamp =
     * intent.getLongExtra(SEARCH_FOR, TIMESTAMP_INVALID);
     * getActivity().setTitle(getActivity().getTitle() + " " +
     * DateUtils.formatDateTime(getActivity(), timestamp,
     * DateUtils.LENGTH_SHORTEST)); args.putLong(SEARCH_FOR, timestamp); }
     * return args; }
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        final Activity activity = getActivity();
        
        //TODO doesn't seem possible without recreating the activity first.
        final View root = LayoutInflater.from( new ContextThemeWrapper(activity, R.style.Theme_SleepMate_Light)).inflate(R.layout.fragment_history_list,
                container, false);
        
        progress = new ProgressDialog(activity);

        mTextView = (TextView) root.findViewById(R.id.text);
        mListView = (ListView) root.findViewById(R.id.list);
        mListView.setVerticalFadingEdgeEnabled(false);
        mListView.setScrollbarFadingEnabled(false);

        sleepHistoryAdapter = new SleepHistoryCursorAdapter(activity, null);
        mListView.setAdapter(sleepHistoryAdapter);

        final Intent intent = activity.getIntent();
        /*
         * TODO temporarily disabled. if
         * (Intent.ACTION_VIEW.equals(intent.getAction())) { final Intent
         * reviewIntent = new Intent(activity, ReviewSleepActivity.class);
         * reviewIntent.setData(intent.getData()); startActivity(reviewIntent);
         * activity.finish(); } else { ((HostActivity)
         * activity).getSupportLoaderManager().initLoader( 0,
         * getLoaderArgs(intent, true), this); }
         */

        ((HostActivity) activity).getSupportLoaderManager().initLoader(0, null,
                this);
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        progress.setMessage(getString(R.string.querying_sleep_database));
        progress.show();
        if (args != null) {
            return new CursorLoader(getActivity(), SleepSession.CONTENT_URI,
                    null, SleepSession.START_TIMESTAMP + " <=? AND "
                            + SleepSession.END_TIMESTAMP + " >=? ",
                    new String[] { Long.toString(args.getLong(SEARCH_FOR)),
                            Long.toString(args.getLong(SEARCH_FOR)) }, null);
        } else {
            return new CursorLoader(getActivity(), SleepSession.CONTENT_URI,
                    null, null, null, SleepSession.SORT_ORDER);
        }
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
        final Activity a = getActivity();
        if (data == null) {
            a.finish();
        } else {
            if (data.getCount() == 1) {
                data.moveToFirst();
                final Intent reviewSleepIntent = new Intent(a,
                        ReviewSleepActivity.class);

                final Uri uri = Uri.withAppendedPath(SleepSession.CONTENT_URI,
                        String.valueOf(data.getLong(0)));
                reviewSleepIntent.setData(uri);
                reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(reviewSleepIntent);
                a.finish();

            } else if (data.getCount() == 0) {
                a.finish();
                return;
            }
            sleepHistoryAdapter.swapCursor(data);
            mTextView.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);

            mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

                @Override
                public boolean onItemLongClick(final AdapterView<?> parent,
                        final View view, final int position, final long rowId) {
                    final AlertDialog.Builder dialog = new AlertDialog.Builder(
                            a)
                            .setMessage(getString(R.string.delete_sleep_record))
                            .setPositiveButton(getString(R.string.ok),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                final DialogInterface dialog,
                                                final int id) {

                                            new DeleteSleepTask(a, progress)
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
        if (progress != null && progress.isShowing()) {
            progress.dismiss();
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

        if (progress != null && progress.isShowing()) {
            progress.dismiss();
        }
    }

}
