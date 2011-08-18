package com.androsz.electricsleepbeta.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;

public final class SleepHistory {
	SleepHistory(){}
	
	public static final String AUTHORITY = SleepHistory.class.getCanonicalName();

	private final static class Helper extends SQLiteOpenHelper {

		private static final String DB_NAME = "sleephistory";

		private static final int DB_VERSION = 5;

		public Helper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			final StringBuilder sb = new StringBuilder();

			sb.append("CREATE TABLE ");
			sb.append(MainTable.TABLE_NAME);
			sb.append(" (");
			sb.append(MainTable._ID);
			sb.append("INTEGER PRIMARY KEY,");
			sb.append(MainTable.KEY_NAME);
			sb.append(" TEXT,");
			sb.append(MainTable.KEY_COLOR);
			sb.append(" INTEGER);");

			db.execSQL(sb.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + MainTable.TABLE_NAME);
			onCreate(db);
		}
	}
	
	/**
	 * Definition of the contract for the main table of our provider.
	 */
	public static final class MainTable implements BaseColumns {

		// This class cannot be instantiated
		private MainTable() {
		}

		/**
		 * The table name offered by this provider
		 */
		public static final String TABLE_NAME = "notebooks";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME);

		/**
		 * The content URI base for a single row of data. Callers must append a
		 * numeric row id to this Uri to retrieve a row
		 */
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://"
				+ AUTHORITY + "/" + TABLE_NAME + "/");

		/**
		 * The MIME type of {@link #CONTENT_URI}.
		 */
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.androsz.flatnote.notebooks";

		/**
		 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single
		 * row.
		 */
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.androsz.flatnote.notebooks";

		/**
		 * Keys for the columns names
		 */
		public static final String KEY_NAME = "KEY_NAME";
		private static final String KEY_COLOR = "KEY_COLOR";

		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = KEY_NAME
				+ " COLLATE LOCALIZED ASC";

		public static final String[] COLUMN_PROJECTION = new String[] {
				KEY_NAME, KEY_COLOR };
	}
	
}
