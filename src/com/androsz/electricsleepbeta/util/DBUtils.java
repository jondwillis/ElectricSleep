package com.androsz.electricsleepbeta.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBUtils {
	public static List<String> GetColumns(final SQLiteDatabase db,
			final String tableName) {
		List<String> ar = null;
		Cursor c = null;
		try {
			c = db.rawQuery("select * from " + tableName + " limit 1", null);
			if (c != null) {
				ar = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return ar;
	}

	public static String join(final List<String> list, final String delim) {
		final StringBuilder buf = new StringBuilder();
		final int num = list.size();
		for (int i = 0; i < num; i++) {
			if (i != 0) {
				buf.append(delim);
			}
			buf.append(list.get(i));
		}
		return buf.toString();
	}
}
