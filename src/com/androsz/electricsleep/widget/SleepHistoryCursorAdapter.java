package com.androsz.electricsleep.widget;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ResourceCursorAdapter;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.view.SleepChartView;

public class SleepHistoryCursorAdapter extends ResourceCursorAdapter {

	private static int LAYOUT = R.layout.list_item_sleep_history;

	private ViewGroup parent;

	public SleepHistoryCursorAdapter(final Context context, final Cursor cursor) {
		super(context, LAYOUT, cursor, true);
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

	@Override
	public View newView(final Context context, final Cursor cursor,
			final ViewGroup parent) {
		final LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.parent = parent;
		return li.inflate(LAYOUT, parent, false);
	}
}
