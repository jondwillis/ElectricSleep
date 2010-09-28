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

package com.androsz.electricsleep.db;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Provides access to the dictionary database.
 */
public class SleepContentProvider extends ContentProvider {

	public static String AUTHORITY = "com.androsz.electricsleep.db.sleepcontentprovider";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/sleephistory");

	// MIME types used for searching words or looking up a single definition
	public static final String WORDS_MIME_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/vnd.com.androsz.electricsleep";
	public static final String DEFINITION_MIME_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/vnd.com.androsz.electricsleep";

	private SleepHistoryDatabase sleepHistoryDatabase;

	// UriMatcher stuff
	private static final int SEARCH_WORDS = 0;
	private static final int GET_WORD = 1;
	private static final int SEARCH_SUGGEST = 2;
	private static final int REFRESH_SHORTCUT = 3;
	private static final UriMatcher sURIMatcher = buildUriMatcher();

	/**
	 * Builds up a UriMatcher for search suggestion and shortcut refresh
	 * queries.
	 */
	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		// to get definitions...
		matcher.addURI(AUTHORITY, "sleephistory", SEARCH_WORDS);
		matcher.addURI(AUTHORITY, "sleephistory/#", GET_WORD);
		// to get suggestions...
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,
				SEARCH_SUGGEST);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*",
				SEARCH_SUGGEST);

		/*
		 * The following are unused in this implementation, but if we include
		 * {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column in our
		 * suggestions table, we could expect to receive refresh queries when a
		 * shortcutted suggestion is displayed in Quick Search Box, in which
		 * case, the following Uris would be provided and we would return a
		 * cursor with a single item representing the refreshed suggestion data.
		 */
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT,
				REFRESH_SHORTCUT);
		matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_SHORTCUT
				+ "/*", REFRESH_SHORTCUT);
		return matcher;
	}

	@Override
	public int delete(final Uri uri, final String selection,
			final String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private Cursor getSleep(final Uri uri) {
		final String rowId = uri.getLastPathSegment();
		final String[] columns = new String[] {
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME,
				SleepHistoryDatabase.KEY_SLEEP_DATA_X,
				SleepHistoryDatabase.KEY_SLEEP_DATA_Y,
				SleepHistoryDatabase.KEY_SLEEP_DATA_MIN,
				SleepHistoryDatabase.KEY_SLEEP_DATA_MAX,
				SleepHistoryDatabase.KEY_SLEEP_DATA_ALARM };

		return sleepHistoryDatabase.getSleep(rowId, columns);
	}

	private Cursor getSuggestions(String query) {
		query = query.toLowerCase();
		final String[] columns = new String[] { BaseColumns._ID,
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME,
				/*
				 * SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, (only if you want
				 * to refresh shortcuts)
				 */
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };

		return sleepHistoryDatabase.getSleepMatches(query, columns);
	}

	/**
	 * This method is required in order to query the supported types. It's also
	 * useful in our own query() method to determine the type of Uri received.
	 */
	@Override
	public String getType(final Uri uri) {
		switch (sURIMatcher.match(uri)) {
		case SEARCH_WORDS:
			return WORDS_MIME_TYPE;
		case GET_WORD:
			return DEFINITION_MIME_TYPE;
		case SEARCH_SUGGEST:
			return SearchManager.SUGGEST_MIME_TYPE;
		case REFRESH_SHORTCUT:
			return SearchManager.SHORTCUT_MIME_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(final Uri uri, final ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean onCreate() {
		sleepHistoryDatabase = new SleepHistoryDatabase(getContext());
		return true;
	}

	/**
	 * Handles all the dictionary searches and suggestion queries from the
	 * Search Manager. When requesting a specific word, the uri alone is
	 * required. When searching all of the dictionary for matches, the
	 * selectionArgs argument must carry the search query as the first element.
	 * All other arguments are ignored.
	 */
	@Override
	public Cursor query(final Uri uri, final String[] projection,
			final String selection, String[] selectionArgs,
			final String sortOrder) {

		// Use the UriMatcher to see what kind of query we have and format the
		// db query accordingly
		switch (sURIMatcher.match(uri)) {
		case SEARCH_SUGGEST:
			if (selectionArgs == null) {
				selectionArgs = new String[] { "" };
				// throw new IllegalArgumentException(
				// "selectionArgs must be provided for the Uri: " + uri);
			}
			return getSuggestions(selectionArgs[0]);
		case SEARCH_WORDS:
			if (selectionArgs == null) {
				selectionArgs = new String[] { "" };
				// throw new IllegalArgumentException(
				// "selectionArgs must be provided for the Uri: " + uri);
			}
			return search(selectionArgs[0]);
		case GET_WORD:
			return getSleep(uri);
		case REFRESH_SHORTCUT:
			return refreshShortcut(uri);
		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}
	}

	// Other required implementations...

	private Cursor refreshShortcut(final Uri uri) {
		/*
		 * This won't be called with the current implementation, but if we
		 * include {@link SearchManager#SUGGEST_COLUMN_SHORTCUT_ID} as a column
		 * in our suggestions table, we could expect to receive refresh queries
		 * when a shortcutted suggestion is displayed in Quick Search Box. In
		 * which case, this method will query the table for the specific word,
		 * using the given item Uri and provide all the columns originally
		 * provided with the suggestion query.
		 */
		final String rowId = uri.getLastPathSegment();
		final String[] columns = new String[] { BaseColumns._ID,
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME,
				SearchManager.SUGGEST_COLUMN_SHORTCUT_ID,
				SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID };

		return sleepHistoryDatabase.getSleep(rowId, columns);
	}

	private Cursor search(String query) {
		query = query.toLowerCase();
		final String[] columns = new String[] { BaseColumns._ID,
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME, };

		return sleepHistoryDatabase.getSleepMatches(query, columns);
	}

	@Override
	public int update(final Uri uri, final ContentValues values,
			final String selection, final String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}
}
