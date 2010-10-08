package com.androsz.electricsleep.ui.widget;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepHistoryDatabase;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class SleepHistoryCursorAdapter extends ResourceCursorAdapter {

	private static int LAYOUT = R.layout.list_item_sleep_history;

	public SleepHistoryCursorAdapter(Context context, Cursor cursor) {
		super(context, LAYOUT, cursor, true);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		return li.inflate(LAYOUT, parent, false);
	}

	@Override
	public void bindView(final View view, final Context context, final Cursor cursor) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				TextView nameTextView = (TextView) view
						.findViewById(R.id.sleep_history_list_item_name);
				final SleepChartReView sleepChartView = (SleepChartReView) view
						.findViewById(R.id.sleep_history_list_item_sleepchartview);

				final String name = cursor.getString(cursor
						.getColumnIndexOrThrow(SleepHistoryDatabase.KEY_SLEEP_DATE_TIME));
				
				nameTextView.setText(name);
				sleepChartView.syncWithCursor(cursor);
			}

		}).run();

	}
}
