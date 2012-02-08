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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.format.DateUtils;
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

            final Uri data = ContentUris.withAppendedId(SleepSession.CONTENT_URI, id);
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

    private Bundle getLoaderArgs(final Intent intent, boolean init) {
        final Bundle args = new Bundle();

        if (intent.hasExtra(SEARCH_FOR)) {
            final long timestamp = intent.getLongExtra(SEARCH_FOR, TIMESTAMP_INVALID);
            getActivity().setTitle(getActivity().getTitle() + " " +
                                   DateUtils.formatDateTime(getActivity(),
                                                            timestamp,
                                                            DateUtils.LENGTH_SHORTEST));
            args.putLong(SEARCH_FOR, timestamp);
        }
        return args;
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Activity a = getActivity();
        progress = new ProgressDialog(a);

        mTextView = (TextView) a.findViewById(R.id.text);
        mListView = (ListView) a.findViewById(R.id.list);

        mListView.setVerticalFadingEdgeEnabled(false);
        mListView.setScrollbarFadingEnabled(false);

        sleepHistoryAdapter = new SleepHistoryCursorAdapter(a, null);

        mListView.setAdapter(sleepHistoryAdapter);

        final Intent intent = a.getIntent();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            final Intent reviewIntent = new Intent(a, ReviewSleepActivity.class);
            reviewIntent.setData(intent.getData());
            startActivity(reviewIntent);
            a.finish();
        } else {
            ((HostActivity) a).getSupportLoaderManager().initLoader(0,
                    getLoaderArgs(intent, true), this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_history_list, container, false);
        return root;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        progress.setMessage(getString(R.string.querying_sleep_database));
        progress.show();
        return new CursorLoader(
            getActivity(), SleepSession.CONTENT_URI, null,
            SleepSession.START_TIMESTAMP + " <=? " +
            SleepSession.END_TIMESTAMP + " >=? ",
            new String[] {Long.toString(args.getLong(SEARCH_FOR)),
                          Long.toString(args.getLong(SEARCH_FOR))},
            null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater mi) {
        mi.inflate(R.menu.menu_multiple_history, menu);
        super.onCreateOptionsMenu(menu, mi);
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

                final Uri uri = Uri.withAppendedPath(
                        SleepSession.CONTENT_URI,
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
        final Activity a = getActivity();
        switch (item.getItemId()) {
        case R.id.menu_item_delete_all:
            final AlertDialog.Builder dialog = new AlertDialog.Builder(a)
                    .setMessage(getString(R.string.delete_sleep_record))
                    .setPositiveButton(getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(
                                        final DialogInterface dialog,
                                        final int id) {

                                    Long[] rowIds = new Long[sleepHistoryAdapter
                                            .getCount()];
                                    Cursor c = sleepHistoryAdapter.getCursor();
                                    c.moveToFirst();
                                    int i = 0;
                                    do {
                                        rowIds[i++] = c.getLong(0);
                                    } while (c.moveToNext());

                                    new DeleteSleepTask(a, progress).execute(
                                            rowIds, null, null);
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
