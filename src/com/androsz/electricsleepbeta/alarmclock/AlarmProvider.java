/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.androsz.electricsleepbeta.alarmclock;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class AlarmProvider extends ContentProvider {
	private static class DatabaseHelper extends SQLiteOpenHelper {
		private static final String DATABASE_NAME = "alarms.db";
		private static final int DATABASE_VERSION = 6;

		public DatabaseHelper(final Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(final SQLiteDatabase db) {
			db.execSQL("CREATE TABLE alarms (" + "_id INTEGER PRIMARY KEY," + "hour INTEGER, "
					+ "minutes INTEGER, " + "daysofweek INTEGER, " + "alarmtime INTEGER, "
					+ "enabled INTEGER, " + "vibrate INTEGER, " + "message TEXT, " + "alert TEXT, "
					+ "timeToIgnore INTEGER);");

			// insert default alarms
			final String insertMe = "INSERT INTO alarms "
					+ "(hour, minutes, daysofweek, alarmtime, enabled, vibrate, message, alert, timeToIgnore) "
					+ "VALUES ";
			db.execSQL(insertMe + "(8, 30, 31, 0, 0, 1, '', '', 0);");
			db.execSQL(insertMe + "(9, 00, 96, 0, 0, 1, '', '', 0);");
		}

		@Override
		public void onUpgrade(final SQLiteDatabase db, final int oldVersion,
				final int currentVersion) {
			if (Log.LOGV) {
				Log.v("Upgrading alarms database from version " + oldVersion + " to "
						+ currentVersion);
			}
			if (oldVersion == 5 && currentVersion == 6) {
				db.execSQL("ALTER TABLE alarms ADD COLUMN " + Alarm.Columns.TIME_TO_IGNORE
						+ " INTEGER");
			} else {
				db.execSQL("DROP TABLE IF EXISTS alarms");
				onCreate(db);
			}
		}
	}

	private static final int ALARMS = 1;
	private static final int ALARMS_ID = 2;
	private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sURLMatcher.addURI("com.androsz.electricsleepbeta.alarmclock", "alarm", ALARMS);
		sURLMatcher.addURI("com.androsz.electricsleepbeta.alarmclock", "alarm/#", ALARMS_ID);
	}

	private SQLiteOpenHelper mOpenHelper;

	public AlarmProvider() {
	}

	@Override
	public int delete(final Uri url, String where, final String[] whereArgs) {
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sURLMatcher.match(url)) {
		case ALARMS:
			count = db.delete("alarms", where, whereArgs);
			break;
		case ALARMS_ID:
			final String segment = url.getPathSegments().get(1);
			Long.parseLong(segment);
			if (TextUtils.isEmpty(where)) {
				where = "_id=" + segment;
			} else {
				where = "_id=" + segment + " AND (" + where + ")";
			}
			count = db.delete("alarms", where, whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Cannot delete from URL: " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public String getType(final Uri url) {
		final int match = sURLMatcher.match(url);
		switch (match) {
		case ALARMS:
			return "vnd.android.cursor.dir/alarms";
		case ALARMS_ID:
			return "vnd.android.cursor.item/alarms";
		default:
			throw new IllegalArgumentException("Unknown URL");
		}
	}

	@Override
	public Uri insert(final Uri url, final ContentValues initialValues) {
		if (sURLMatcher.match(url) != ALARMS) {
			throw new IllegalArgumentException("Cannot insert into URL: " + url);
		}

		final ContentValues values = new ContentValues(initialValues);

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		final long rowId = db.insert("alarms", Alarm.Columns.MESSAGE, values);
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + url);
		}
		if (Log.LOGV) {
			Log.v("Added alarm rowId = " + rowId);
		}

		final Uri newUrl = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, rowId);
		getContext().getContentResolver().notifyChange(newUrl, null);
		return newUrl;
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(final Uri url, final String[] projectionIn, final String selection,
			final String[] selectionArgs, final String sort) {
		final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		// Generate the body of the query
		final int match = sURLMatcher.match(url);
		switch (match) {
		case ALARMS:
			qb.setTables("alarms");
			break;
		case ALARMS_ID:
			qb.setTables("alarms");
			qb.appendWhere("_id=");
			qb.appendWhere(url.getPathSegments().get(1));
			break;
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		final Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null, null, sort);

		if (ret == null) {
			if (Log.LOGV) {
				Log.v("Alarms.query: failed");
			}
		} else {
			ret.setNotificationUri(getContext().getContentResolver(), url);
		}

		return ret;
	}

	@Override
	public int update(final Uri url, final ContentValues values, final String where,
			final String[] whereArgs) {
		int count;
		long rowId = 0;
		final int match = sURLMatcher.match(url);
		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		switch (match) {
		case ALARMS_ID: {
			final String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			count = db.update("alarms", values, "_id=" + rowId, null);
			break;
		}
		default: {
			throw new UnsupportedOperationException("Cannot update URL: " + url);
		}
		}
		if (Log.LOGV) {
			Log.v("*** notifyChange() rowId: " + rowId + " url " + url);
		}
		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}
}
