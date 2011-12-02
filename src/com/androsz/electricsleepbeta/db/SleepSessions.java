package com.androsz.electricsleepbeta.db;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.androsz.electricsleepbeta.util.DBUtils;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/*
 * Modernized (as of September 2011), compatible, and simplified version of old SleepContentProvider and SleepHistoryDatabase
 */
public class SleepSessions {

	/**
	 * Helper that receives callbacks for database creation, upgrade, etc.
	 */
	private final static class Helper extends SQLiteOpenHelper {

		private static final String DB_NAME = "sleephistory";

		private static final int DB_VERSION = 5;

		public Helper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO: use stringbuilder
			final String FTS_TABLE_CREATE = "CREATE VIRTUAL TABLE " + MainTable.TABLE_NAME
					+ " USING fts3 (" + MainTable.KEY_TITLE + ", " + MainTable.KEY_SLEEP_DATA
					+ ", " + MainTable.KEY_MIN + ", " + MainTable.KEY_ALARM + ", "
					+ MainTable.KEY_RATING + ", " + MainTable.KEY_DURATION + ", "
					+ MainTable.KEY_SPIKES + ", " + MainTable.KEY_TIME_FELL_ASLEEP + ", "
					+ MainTable.KEY_NOTE + ");";
			db.execSQL(FTS_TABLE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO
			// db.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);
			// onCreate(db);
		}
	}

	/**
	 * Definition of the contract for the main table of our provider.
	 */
	public static final class MainTable implements BaseColumns {

		public static final String TABLE_NAME = "FTSsleephistory";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

		/**
		 * The content URI base for a single row of data. Callers must append a
		 * numeric row id to this Uri to retrieve a row
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/"
				+ TABLE_NAME + "/");

		/**
		 * The MIME type of {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.androsz.electricsleepbeta.sleephistory";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * row.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.androsz.electricsleepbeta.sleephistory";

		/**
		 * Keys for the columns names
		 */

		// The columns!
		public static final String KEY_ALARM = "sleep_data_alarm";

		public static final String KEY_ROW_ID = "rowid";
		// DATABASE_VERSION = 4
		public static final String KEY_DURATION = "KEY_SLEEP_DATA_DURATION";
		public static final String KEY_MIN = "sleep_data_min";
		public static final String KEY_NOTE = "KEY_SLEEP_DATA_NOTE";
		public static final String KEY_RATING = "sleep_data_rating";
		public static final String KEY_SLEEP_DATA = "sleep_data";
		public static final String KEY_SPIKES = "KEY_SLEEP_DATA_SPIKES";
		public static final String KEY_TIME_FELL_ASLEEP = "KEY_SLEEP_DATA_TIME_FELL_ASLEEP";
		// DATABASE_VERSION = 3
		public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = KEY_ROW_ID + " ASC";

		public static final String[] ALL_COLUMNS_PROJECTION = new String[] { KEY_ROW_ID, KEY_TITLE,
				KEY_ALARM, KEY_DURATION, KEY_MIN, KEY_NOTE, KEY_RATING, KEY_SLEEP_DATA, KEY_SPIKES,
				KEY_TIME_FELL_ASLEEP };

		// This class cannot be instantiated
		private MainTable() {
		}
	}

	/**
	 * A very simple implementation of a content provider.
	 */
	public static class Provider extends ContentProvider {
		// A projection map used to select columns from the database
		private final HashMap<String, String> projectionMap;
		// Uri matcher to decode incoming URIs.
		private final UriMatcher uriMatcher;

		private static final int TABLE = 0;
		private static final int ROW = 1;

		// Handle to a new DatabaseHelper.
		private Helper helper;

		/**
		 * Global provider initialization.
		 */
		public Provider() {
			// Create and initialize URI matcher.
			uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
			uriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME, TABLE);
			uriMatcher.addURI(AUTHORITY, MainTable.TABLE_NAME + "/#", ROW);

			// Create and initialize projection map for all columns. This is
			// simply an identity mapping.
			projectionMap = new HashMap<String, String>();

			projectionMap.put(MainTable.KEY_ROW_ID, "rowid AS " + MainTable._ID);
			// projectionMap.put(MainTable._ID, MainTable._ID);
			// projectionMap.put(MainTable.KEY_ROW_ID, MainTable.KEY_ROW_ID);
			projectionMap.put(MainTable.KEY_TITLE, MainTable.KEY_TITLE);
			projectionMap.put(MainTable.KEY_SLEEP_DATA, MainTable.KEY_SLEEP_DATA);
			projectionMap.put(MainTable.KEY_MIN, MainTable.KEY_MIN);
			projectionMap.put(MainTable.KEY_ALARM, MainTable.KEY_ALARM);
			projectionMap.put(MainTable.KEY_RATING, MainTable.KEY_RATING);
			projectionMap.put(MainTable.KEY_DURATION, MainTable.KEY_DURATION);
			projectionMap.put(MainTable.KEY_SPIKES, MainTable.KEY_SPIKES);
			projectionMap.put(MainTable.KEY_TIME_FELL_ASLEEP, MainTable.KEY_TIME_FELL_ASLEEP);
			projectionMap.put(MainTable.KEY_NOTE, MainTable.KEY_NOTE);

		}

		@Override
		public int delete(Uri uri, String selection, String[] selectionArgs) {
			final SQLiteDatabase db = helper.getWritableDatabase();

			int count;

			switch (uriMatcher.match(uri)) {
			case TABLE:
				// If URI is main table, delete uses incoming where clause and
				// args.
				count = db.delete(MainTable.TABLE_NAME, selection, selectionArgs);
				break;

			// If the incoming URI matches a single note ID, does the delete
			// based on the
			// incoming data, but modifies the where clause to restrict it to
			// the
			// particular note ID.
			case ROW:
				// If URI is for a particular row ID, delete is based on
				// incoming
				// data but modified to restrict to the given ID.

				String finalWhere = DatabaseUtils.concatenateWhere(MainTable.KEY_ROW_ID + " = "
						+ uri.getLastPathSegment(), selection);
				count = db.delete(MainTable.TABLE_NAME, finalWhere, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			notifyChange(uri);

			return count;
		}

		@Override
		public String getType(Uri uri) {
			switch (uriMatcher.match(uri)) {
			case TABLE:
				return MainTable.CONTENT_TYPE;
			case ROW:
				return MainTable.CONTENT_ITEM_TYPE;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		}

		@Override
		public Uri insert(Uri uri, ContentValues initialValues) {
			if (uriMatcher.match(uri) != TABLE) {
				// Can only insert into to main URI.
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			ContentValues values;

			if (initialValues != null) {
				values = new ContentValues(initialValues);
			} else {
				throw new SQLException("Cannot insert uninitialized ContentValues.");
			}

			final SQLiteDatabase db = helper.getWritableDatabase();
			
			//hack to work around an sqlite bug returning the wrong rowid on some devices.
			SQLiteStatement statement = db.compileStatement(String.format(
					"SELECT IFNULL(max(%s), 0) as %s from %s", MainTable.KEY_ROW_ID,
					MainTable.KEY_ROW_ID, MainTable.TABLE_NAME));
			long maxRowId = statement.simpleQueryForLong();
			long rowId = maxRowId + 1;
			
			values.put(MainTable.KEY_ROW_ID, rowId);
			db.insert(MainTable.TABLE_NAME, null, values);
			

			// If the insert succeeded, the row ID exists.
			if (rowId != -1) {
				final Uri noteUri = ContentUris
						.withAppendedId(MainTable.CONTENT_ID_URI_BASE, rowId);
				notifyChange(noteUri);
				return noteUri;
			}

			throw new SQLException("Failed to insert row into " + uri);
		}

		private void notifyChange(Uri uri) {
			getContext().getContentResolver().notifyChange(uri, null);
		}

		/**
		 * Perform provider creation.
		 */
		@Override
		public boolean onCreate() {
			helper = new Helper(getContext());
			// Assumes that any failures will be reported by a thrown exception.
			return true;
		}

		@Override
		public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
				String sortOrder) {
			// Constructs a new query builder and sets its table name
			final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(MainTable.TABLE_NAME);
			qb.setProjectionMap(projectionMap);
			switch (uriMatcher.match(uri)) {
			case TABLE:
				// If the incoming URI is for main table.
				break;

			case ROW:
				// The incoming URI is for a single row.
				qb.appendWhere(MainTable.KEY_ROW_ID + " = " + uri.getLastPathSegment());
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			if (TextUtils.isEmpty(sortOrder)) {
				sortOrder = MainTable.DEFAULT_SORT_ORDER;
			}

			final SQLiteDatabase db = helper.getReadableDatabase();

			Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);

			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		}

		@Override
		public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
			final SQLiteDatabase db = helper.getWritableDatabase();
			int count;
			String finalWhere;

			switch (uriMatcher.match(uri)) {
			case TABLE:
				// If URI is main table, update uses incoming where clause and
				// args.
				count = db.update(MainTable.TABLE_NAME, values, selection, selectionArgs);
				break;

			case ROW:
				// If URI is for a particular row ID, update is based on
				// incoming
				// data but modified to restrict to the given ID.
				// finalWhere = DatabaseUtils.concatenateWhere(
				// BaseColumns._ID + " = " + ContentUris.parseId(uri),
				// selection);
				count = db.update(MainTable.TABLE_NAME, values, selection, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}

			notifyChange(uri);

			return count;
		}
	}

	public static final String AUTHORITY = "com.androsz.electricsleepbeta.db.sleepcontentprovider";

	public static Uri createSession(Context context, SleepSession sleepRecord) {
		final ContentResolver contentResolver = context.getContentResolver();

		final ContentValues values = new ContentValues();
		values.put(MainTable.KEY_TITLE, sleepRecord.title);

		try {
			values.put(MainTable.KEY_SLEEP_DATA,
					SleepSession.objectToByteArray(sleepRecord.chartData));
		} catch (final IOException e) {
			GoogleAnalyticsTracker.getInstance().trackEvent(Integer.toString(VERSION.SDK_INT),
					Build.MODEL, "createSessionIOException : " + e.getMessage(), 0);
		}

		values.put(MainTable.KEY_MIN, sleepRecord.min);
		values.put(MainTable.KEY_ALARM, sleepRecord.alarm);
		values.put(MainTable.KEY_RATING, sleepRecord.rating);
		values.put(MainTable.KEY_DURATION, sleepRecord.duration);
		values.put(MainTable.KEY_SPIKES, sleepRecord.spikes);
		values.put(MainTable.KEY_TIME_FELL_ASLEEP, sleepRecord.fellAsleep);
		values.put(MainTable.KEY_NOTE, sleepRecord.note);

		final Uri uri = contentResolver.insert(MainTable.CONTENT_URI, values);

		return uri;
	}

	public static int deleteSession(final Context context, CharSequence sessionTitle) {
		final ContentResolver contentResolver = context.getContentResolver();

		final int numRowsDeleted = contentResolver.delete(MainTable.CONTENT_URI,
				MainTable.KEY_TITLE + "='" + sessionTitle + "'", null);

		return numRowsDeleted;
	}

	public static int deleteSession(final Context context, Long rowId) {
		final ContentResolver contentResolver = context.getContentResolver();

		final int numRowsDeleted = contentResolver.delete(
				Uri.withAppendedPath(MainTable.CONTENT_URI, Long.toString(rowId)), null, null);

		return numRowsDeleted;
	}

	public static Cursor getSleepMatches(Context context, final String query, final String[] columns) {
		final ContentResolver contentResolver = context.getContentResolver();
		final String selection = MainTable.KEY_TITLE + " MATCH ?";
		final String[] selectionArgs = new String[] { query + "*" };

		return contentResolver
				.query(MainTable.CONTENT_URI, columns, selection, selectionArgs, null);
	}

	public static ArrayList<Long[]> getStartAndEndTimesFromCursor(Context context, Cursor c) {
		final ArrayList<Long[]> sessions = new ArrayList<Long[]>(20);

		if (c != null && !c.isClosed() && c.moveToFirst()) {

			do {
				final SleepSession session = new SleepSession(c);
				sessions.add(new Long[] { session.getStartTimeOfDay(), session.getEndTimeOfDay(),
						(long) session.getStartJulianDay(), (long) session.getEndJulianDay(),
						c.getLong(0) });
			} while (c.moveToNext());
			c.close();
		}

		return sessions;
	}

	/**
	 * @param context
	 * @param sleepRecord
	 *            Modified SleepRecord that will replace a SleepRecord with the
	 *            same title
	 * @return the number of rows updated
	 */
	public static int updateSession(final Context context, SleepSession sleepRecord) {
		final ContentResolver contentResolver = context.getContentResolver();

		final ContentValues values = new ContentValues();
		values.put(MainTable.KEY_TITLE, sleepRecord.title);

		try {
			values.put(MainTable.KEY_SLEEP_DATA,
					SleepSession.objectToByteArray(sleepRecord.chartData));
		} catch (final IOException e) {
			GoogleAnalyticsTracker.getInstance().trackEvent(Integer.toString(VERSION.SDK_INT),
					Build.MODEL, "updateSessionIOException : " + e.getMessage(), 0);
		}

		values.put(MainTable.KEY_MIN, sleepRecord.min);
		values.put(MainTable.KEY_ALARM, sleepRecord.alarm);
		values.put(MainTable.KEY_RATING, sleepRecord.rating);
		values.put(MainTable.KEY_DURATION, sleepRecord.duration);
		values.put(MainTable.KEY_SPIKES, sleepRecord.spikes);
		values.put(MainTable.KEY_TIME_FELL_ASLEEP, sleepRecord.fellAsleep);
		values.put(MainTable.KEY_NOTE, sleepRecord.note);

		final int numRowsUpdated = contentResolver.update(MainTable.CONTENT_URI, values,
				MainTable.KEY_TITLE + "='" + sleepRecord.title + "'", null);

		return numRowsUpdated;
	}

	/*
	 * This class cannot be instantiated
	 */
	SleepSessions() {
	}
}
