/*
 * Copyright (C) 2010 The Android Open Source Project
 * Copyright (C) 2010 Androsz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androsz.electricsleep.ui;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.ui.widget.SleepHistoryCursorAdapter;
import com.androsz.electricsleep.util.DeviceUtil;

public class HistoryActivity extends CustomTitlebarActivity {

	private TextView mTextView;

	private ListView mListView;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);

		final Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			final Intent reviewIntent = new Intent(this,
					ReviewSleepActivity.class);
			reviewIntent.setData(intent.getData());
			startActivity(reviewIntent);
			finish();
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			final String query = intent.getStringExtra(SearchManager.QUERY);
			showResults(query);
		} else {
			showResults(getString(R.string.to));
		}
	}

	@Override
	public boolean onSearchRequested() {
		showResults(getString(R.string.to));
		return super.onSearchRequested();
	}

	/**
	 * Searches the dictionary and displays results for the given query.
	 * 
	 * @param query
	 *            The search query
	 */
	private void showResults(final String query) {

		final Cursor cursor = managedQuery(SleepContentProvider.CONTENT_URI,
				null, null, new String[] { query },
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME + " DESC");

		if (cursor == null) {
			// There are no results
			mTextView.setText(getString(R.string.no_results));
		} else {
			mTextView.setVisibility(View.GONE);

			// Specify the columns we want to display in the result
			// final String[] from = new String[] {
			// SleepHistoryDatabase.KEY_SLEEP_DATE_TIME,
			// SleepHistoryDatabase.KEY_SLEEP_DATA_X };

			// Specify the corresponding layout elements where we want the
			// columns to go
			// final int[] to = new int[] { R.id.name };

			// Create a simple cursor adapter for the definitions and apply them
			// to the ListView
			final SleepHistoryCursorAdapter sleepHistory = new SleepHistoryCursorAdapter(
					this, cursor);
			mListView.setAdapter(sleepHistory);

			mListView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
			// mListView.setWillNotCacheDrawing(true);
			if (DeviceUtil.getCpuClockSpeed() > 550) { // anything faster than a
														// droid *should* be
														// able to handle smooth
														// scrolling
				mListView.setScrollingCacheEnabled(false);
			}
			mListView.setScrollbarFadingEnabled(false);

			mListView.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(final AdapterView<?> parent,
						final View view, final int position, final long rowid) {
					final SleepHistoryDatabase shdb = new SleepHistoryDatabase(
							HistoryActivity.this);
					try {
						final AlertDialog.Builder dialog = new AlertDialog.Builder(
								HistoryActivity.this)
								.setMessage("Delete this sleep record?")
								.setPositiveButton("Yes",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													final DialogInterface dialog,
													final int id) {

												shdb.deleteRow(rowid);
												Toast.makeText(
														HistoryActivity.this,
														"Deleted sleep record.",
														Toast.LENGTH_SHORT)
														.show();
												showResults(query);
											}
										})
								.setNegativeButton("No",
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													final DialogInterface dialog,
													final int id) {
												dialog.cancel();
											}
										});
						dialog.show();
					} finally {
						shdb.close();
					}
					return true;
				}

			});

			// Define the on-click listener for the list items
			mListView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(final AdapterView<?> parent,
						final View view, final int position, final long id) {
					// Build the Intent used to open WordActivity with a
					// specific word Uri
					final Intent reviewSleepIntent = new Intent(
							getApplicationContext(), ReviewSleepActivity.class);
					final Uri data = Uri.withAppendedPath(
							SleepContentProvider.CONTENT_URI,
							String.valueOf(id));
					reviewSleepIntent.setData(data);
					startActivity(reviewSleepIntent);
				}
			});
		}
	}
}
