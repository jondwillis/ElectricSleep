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

import android.app.SearchManager;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;

public class HistoryActivity extends CustomTitlebarActivity {

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}

	private TextView mTextView;
	private ListView mListView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mTextView = (TextView) findViewById(R.id.text);
		mListView = (ListView) findViewById(R.id.list);

		Intent intent = getIntent();

		if (Intent.ACTION_VIEW.equals(intent.getAction())) {
			// handles a click on a search suggestion; launches activity to show
			// word
			Intent reviewIntent = new Intent(this, ReviewSleepActivity.class);
			reviewIntent.setData(intent.getData());
			startActivity(reviewIntent);
			finish();
		} else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			// handles a search query
			String query = intent.getStringExtra(SearchManager.QUERY);
			showResults(query);
		}
	}
	
	public boolean onSearchRequested()
	{
		showResults("");
		return super.onSearchRequested();
	}

	/**
	 * Searches the dictionary and displays results for the given query.
	 * 
	 * @param query
	 *            The search query
	 */
	private void showResults(String query) {
		
		Cursor cursor = managedQuery(SleepContentProvider.CONTENT_URI, null,
				null, new String[] { query }, null);

		if (cursor == null) {
			// There are no results
			mTextView.setText(getString(R.string.no_results,
					new Object[] { query }));
		} else {
			// Display the number of results
			int count = cursor.getCount();
			String countString = getResources().getQuantityString(
					R.plurals.search_results, count,
					new Object[] { count, query });
			mTextView.setText(countString);

			// Specify the columns we want to display in the result
			String[] from = new String[] { SleepHistoryDatabase.KEY_SLEEP_DATE_TIME };

			// Specify the corresponding layout elements where we want the
			// columns to go
			int[] to = new int[] { R.id.word };

			// Create a simple cursor adapter for the definitions and apply them
			// to the ListView
			SimpleCursorAdapter sleepHistory = new SimpleCursorAdapter(this,
					R.layout.list_item_sleep_history, cursor, from, to);
			mListView.setAdapter(sleepHistory);

			// Define the on-click listener for the list items
			mListView.setOnItemClickListener(new OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					// Build the Intent used to open WordActivity with a
					// specific word Uri
					Intent wordIntent = new Intent(getApplicationContext(),
							ReviewSleepActivity.class);
					Uri data = Uri.withAppendedPath(
							SleepContentProvider.CONTENT_URI, String.valueOf(id));
					wordIntent.setData(data);
					startActivity(wordIntent);
				}
			});
		}
	}
}
