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

package com.androsz.electricsleepbeta.db;

import java.io.IOException;
import java.io.StreamCorruptedException;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

/**
 * Contains logic to return specific words from the dictionary, and load the
 * dictionary table when it needs to be created.
 */
public class SleepHistoryDatabase {
	/**
	 * This creates/opens the database.
	 */
	private static class SleepHistoryDBOpenHelper extends SQLiteOpenHelper {

		SleepHistoryDBOpenHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		/**
		 * Add a sleep to db.
		 * 
		 * @return rowId or -1 if failed
		 * @throws IOException
		 */
		public long addSleep(final SleepRecord sleepRecord) throws IOException {

			final SQLiteDatabase db = getWritableDatabase();

			return sleepRecord.insertIntoDb(db);

		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			/*
			 * if (Environment.MEDIA_MOUNTED.equals(Environment
			 * .getExternalStorageState())) { File externalDb = new
			 * File(Environment
			 * .getExternalStorageDirectory().getAbsolutePath(),
			 * SleepHistoryDatabase.DATABASE_NAME); if (externalDb.exists()) {
			 * File data = Environment.getDataDirectory();
			 * 
			 * String restoredDbPath =
			 * "/data/com.androsz.electricsleepdonate/databases/"; File
			 * restoredDb = new File(data + restoredDbPath, DATABASE_NAME); try
			 * { db.close(); DeviceUtil.copyFile(externalDb, restoredDb); db =
			 * SQLiteDatabase.openDatabase(restoredDbPath, null,
			 * SQLiteDatabase.OPEN_READONLY); } catch (IOException e) {
			 * db.execSQL(FTS_TABLE_CREATE); } } else {
			 * db.execSQL(FTS_TABLE_CREATE); }
			 * 
			 * } else
			 */{
				db.execSQL(FTS_TABLE_CREATE);
			}
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + FTS_VIRTUAL_TABLE);
			onCreate(db);
		}
	}

	public static final String DATABASE_NAME = "sleephistory";

	private static final int DATABASE_VERSION = 5;
	
	public static final String FTS_VIRTUAL_TABLE = "FTSsleephistory";
	/*
	 * Note that FTS3 does not support column constraints and thus, you cannot
	 * declare a primary key. However, "rowid" is automatically used as a unique
	 * identifier, so when making requests, we will use "_id" as an alias for
	 * "rowid"
	 */
	public static final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE "
			+ FTS_VIRTUAL_TABLE + " USING fts3 (" + SleepRecord.KEY_TITLE
			+ ", " + SleepRecord.KEY_SLEEP_DATA + ", " + SleepRecord.KEY_MIN
			+ ", " + SleepRecord.KEY_ALARM + ", " + SleepRecord.KEY_RATING
			+ ", " + SleepRecord.KEY_DURATION + ", " + SleepRecord.KEY_SPIKES
			+ ", " + SleepRecord.KEY_TIME_FELL_ASLEEP + ", "
			+ SleepRecord.KEY_NOTE + ");";


	private final SleepHistoryDBOpenHelper databaseOpenHelper;

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The Context within which to work, used to create the DB
	 */
	public SleepHistoryDatabase(final Context context) {
		databaseOpenHelper = new SleepHistoryDBOpenHelper(context);
	}

	/**
	 * Proxy call to underlying openhelper: Add a sleep to db.
	 * 
	 * @return rowId or -1 if failed
	 * @throws IOException
	 */
	public void addSleep(final Context context, final SleepRecord sleepRecord)
			throws IOException {
		databaseOpenHelper.addSleep(sleepRecord);
	}

	public void close() {
		databaseOpenHelper.close();
	}

	public boolean deleteRow(final long id) {
		final SQLiteDatabase db = databaseOpenHelper.getWritableDatabase();
		final boolean value = db.delete(FTS_VIRTUAL_TABLE, "rowid=?",
				new String[] { Long.toString(id) }) > 0;
		db.close();
		return value;
	}

	/**
	 * Returns a Cursor positioned at the record specified by rowId
	 * 
	 * @param rowId
	 *            id of word to retrieve
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor positioned to matching word, or null if not found.
	 * @throws ClassNotFoundException
	 * @throws IOException
	 * @throws IllegalArgumentException
	 * @throws StreamCorruptedException
	 */
	public Cursor getSleep(final String rowId, final String[] columns) {
		final String selection = "rowid = ?";
		final String[] selectionArgs = new String[] { rowId };

		return query(selection, selectionArgs, columns);
		/*
		 * This builds a query that looks like: SELECT <columns> FROM <table>
		 * WHERE rowid = <rowId>
		 */
	}

	/**
	 * Returns a Cursor over all records that match the given query
	 * 
	 * @param query
	 *            The string to search for
	 * @param columns
	 *            The columns to include, if null then all are included
	 * @return Cursor over all words that match, or null if none found.
	 */
	public Cursor getSleepMatches(final String query, final String[] columns) {
		final String selection = SleepRecord.KEY_TITLE + " MATCH ?";
		final String[] selectionArgs = new String[] { query + "*" };

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
	private Cursor query(final String selection, final String[] selectionArgs,
			final String[] columns) {
		/*
		 * The SQLiteBuilder provides a map for all possible columns requested
		 * to actual columns in the database, creating a simple column alias
		 * mechanism by which the ContentProvider does not need to know the real
		 * column names
		 */
		final SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(FTS_VIRTUAL_TABLE);
		builder.setProjectionMap(SleepRecord.COLUMN_MAP);
		final SQLiteDatabase db = databaseOpenHelper.getReadableDatabase();
		final Cursor cursor = builder.query(db, columns, selection,
				selectionArgs, null, null, null);

		if (cursor == null) {
			return null;
		} else if (!cursor.moveToFirst()) {
			cursor.close();
			return null;
		}
		return cursor;
	}
}