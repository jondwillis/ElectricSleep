package com.androsz.electricsleep.ui.widget;

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

	private ViewGroup parent;

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.parent = parent;
		return li.inflate(LAYOUT, parent, false);
	}

	@Override
	public void bindView(final View view, final Context context,
			final Cursor cursor) {
		System.gc();
		final SleepChartView sleepChartView = (SleepChartView) view
				.findViewById(R.id.sleep_history_list_item_sleepchartview);

		sleepChartView.syncWithCursor(cursor);
		sleepChartView.setMinimumHeight(parent.getHeight());
	}
}
