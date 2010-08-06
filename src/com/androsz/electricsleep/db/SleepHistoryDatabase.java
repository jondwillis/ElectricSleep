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

import android.R;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Contains logic to return specific words from the dictionary, and load the
 * dictionary table when it needs to be created.
 */
public class SleepHistoryDatabase {
	private static final String TAG = "SleepHistoryDatabase";

	// The columns we'll include in the dictionary table
	public static final String KEY_SLEEP_DATE_TIME = SearchManager.SUGGEST_COLUMN_TEXT_1;
	public static final String KEY_SLEEP_DATA_X = "sleep_data_x";
	public static final String KEY_SLEEP_DATA_Y = "sleep_data_y";
	public static final String KEY_SLEEP_DATA_MIN = "sleep_data_min";
	public static final String KEY_SLEEP_DATA_MAX = "sleep_data_min";
	public static final String KEY_SLEEP_DATA_ALARM = "sleep_data_alarm";

	private static final String DATABASE_NAME = "sleephistory";
	private static final String FTS_VIRTUAL_TABLE = "FTSsleephistory";
	private static final int DATABASE_VERSION = 2;

	private final SleepHistoryDBOpenHelper databaseOpenHelper;
	private static final HashMap<String, String> columnMap = buildColumnMap();

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The Context within which to work, used to create the DB
	 */
	public SleepHistoryDatabase(Context context) {
		databaseOpenHelper = new SleepHistoryDBOpenHelper(context);
	}

	/**
	 * Builds a map for all columns that may be requested, which will be given
	 * to the SQLiteQueryBuilder. This is a good way to define aliases for
	 * column names, but must include all columns, even if the value is the key.
	 * This allows the ContentProvider to request columns w/o the need to know
	 * real column names and create the alias itself.
	 */
	private static HashMap<String, String> buildColumnMap() {
		HashMap<String, String> map = new HashMap<String, String>();
		map.put(KEY_SLEEP_DATE_TIME, KEY_SLEEP_DATE_TIME);
		map.put(KEY_SLEEP_DATA_X, KEY_SLEEP_DATA_X);
		map.put(KEY_SLEEP_DATA_Y, KEY_SLEEP_DATA_Y);
		map.put(KEY_SLEEP_DATA_MIN, KEY_SLEEP_DATA_MIN);
		map.put(KEY_SLEEP_DATA_MAX, KEY_SLEEP_DATA_MAX);
		map.put(KEY_SLEEP_DATA_ALARM, KEY_SLEEP_DATA_ALARM);

		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
		return map;
	}

	/**
	 * Returns a Cursor positioned at the word specified by rowId
	 * 
	 * @param rowId
	 *            id of word to retrieve
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor positioned to matching word, or null if not found.
	 */
	public Cursor getSleep(String rowId, String[] columns) {
		String selection = "rowid = ?";
		String[] selectionArgs = new String[] { rowId };

		return query(selection, selectionArgs, columns);

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE rowid = <rowId>
		 */
	}

	/**
	 * Returns a Cursor over all words that match the given query
	 * 
	 * @param query
	 *            The string to search for
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor over all words that match, or null if none found.
	 */
	public Cursor getSleepMatches(String query, String[] columns) {
		String selection = KEY_SLEEP_DATE_TIME + " MATCH ?";
		String[] selectionArgs = new String[] { query + "*" };

		return query(selection, selectionArgs, columns);

		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE <KEY_WORD> MATCH 'query*' which is an FTS3 search for the query
		 * text (plus a wildcard) inside the word column.
		 * 
		 * - "rowid" is the unique id for all rows but we need this value for
		 * the "_id" column in order for the Adapters to work, so the columns
		 * need to make "_id" an alias for "rowid" - "rowid" also needs to be
		 * used by the SUGGEST_COLUMN_INTENT_DATA alias in order for suggestions
		 * to carry the proper intent data. These aliases are defined in the
		 * DictionaryProvider when queries are made. - This can be revised to
		 * also search the definition text with FTS3 by changing the selection
		 * clause to use FTS_VIRTUAL_TABLE instead of KEY_WORD (to search across
		 * the entire table, but sorting the relevance could be difficult.
		 */
	}

	/**
	 * Proxy call to underlying openhelper: Add a sleep to db.
	 * 
	 * @return rowId or -1 if failed
	 * @throws IOException
	 */
	public void addSleep(final String sleepDateTime,
			final ArrayList<Double> sleepChartDataX,
			final ArrayList<Double> sleepChartDataY, final int min,
			final int max, final int alarm) throws IOException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					databaseOpenHelper.addSleep(sleepDateTime, sleepChartDataX,
							sleepChartDataY, min, max, alarm);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Performs a database query.
	 * 
	 * @param selection
	 *            The selection clause
	 * @param selectionArgs
	 *            Selection arguments for "?" components in the selection
	 * @param columns
	 *            The columns to return
	 * @return A Cursor over all rows matching the query
	 */
	private Cursor query(String selection, String[] selectionArgs,
			String[] columns) {
		/*
		 * The SQLiteBuilder provides a map for all possible columns requested
		 * to actual columns in the database, creating a simple column alias
		 * mechanism by which the ContentProvider does not need to know the real
		 * column names
		 */
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(FTS_VIRTUAL_TABLE);
		builder.setProjectionMap(columnMap);

		Cursor cursor = builder.query(databaseOpenHelper.getReadableDatabase(),
				columns, selection, selectionArgs, null, null, null);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	private static byte[] objectToByteArray(Object obj) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);

		return baos.toByteArray();
	}

	private static Object byteArrayToObject(byte[] bytes)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = new ObjectInputStream(bais);

		return ois.readObject();
	}

	/**
	 * This creates/opens the database.
	 */
	private static class SleepHistoryDBOpenHelper extends SQLiteOpenHelper {

		private final Context mHelperContext;
		private SQLiteDatabase mDatabase;

		/*
		 * Note that FTS3 does not support column constraints and thus, you
		 * cannot declare a primary key. However, "rowid" is automatically used
		 * as a unique identifier, so when making requests, we will use "_id" as
		 * an alias for "rowid"
		 */
		private static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
				+ FTS_VIRTUAL_TABLE + " USING fts3 (" + KEY_SLEEP_DATE_TIME
				+ ", " + KEY_SLEEP_DATA_X + ", " + KEY_SLEEP_DATA_Y + ", "
				+ KEY_SLEEP_DATA_MIN + ", " + KEY_SLEEP_DATA_MAX + ", "
				+ KEY_SLEEP_DATA_ALARM + ");";

		SleepHistoryDBOpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
			mHelperContext = context;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			mDatabase = db;
			mDatabase.execSQL(FTS_TABLE_CREATE);
		}
		
		@Override
		public void onOpen(SQLiteDatabase db)
		{
			mDatabase = db;
		}

		/**
		 * Add a sleep to db.
		 * 
		 * @return rowId or -1 if failed
		 * @throws IOException
		 */
		public long addSleep(String sleepDateTime,
				ArrayList<Double> sleepChartDataX,
				ArrayList<Double> sleepChartDataY, int min, int max, int alarm)
				throws IOException {

			while (mDatabase == null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			ContentValues initialValues = new ContentValues();
			initialValues.put(KEY_SLEEP_DATE_TIME, sleepDateTime);

			initialValues.put(KEY_SLEEP_DATA_X,
					objectToByteArray(sleepChartDataX));
			initialValues.put(KEY_SLEEP_DATA_Y,
					objectToByteArray(sleepChartDataY));

			initialValues.put(KEY_SLEEP_DATA_MIN, min);
			initialValues.put(KEY_SLEEP_DATA_MAX, max);
			initialValues.put(KEY_SLEEP_DATA_ALARM, alarm);

			return mDatabase.insert(FTS_VIRTUAL_TABLE, null, initialValues);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			onCreate(db);
		}
	}

}
