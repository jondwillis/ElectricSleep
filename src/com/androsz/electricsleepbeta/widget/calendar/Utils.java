/*
 * Copyright (C) 2006 The Android Open Source Project
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

package com.androsz.electricsleepbeta.widget.calendar;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.ViewFlipper;

import com.androsz.electricsleepbeta.R;

public class Utils {
	private static final int CLEAR_ALPHA_MASK = 0x00FFFFFF;
	protected static final String CLOSE_EMAIL_MARKER = ">";
	/* The corner should be rounded on the top right and bottom right */
	private static final float[] CORNERS = new float[] { 0, 0, 5, 5, 5, 5, 0, 0 };
	// TODO: replace this with the correct i18n way to do this
	public static final String englishNthDay[] = { "", "1st", "2nd", "3rd", "4th", "5th", "6th",
			"7th", "8th", "9th", "10th", "11th", "12th", "13th", "14th", "15th", "16th", "17th",
			"18th", "19th", "20th", "21st", "22nd", "23rd", "24th", "25th", "26th", "27th", "28th",
			"29th", "30th", "31st" };

	private static final int HIGH_ALPHA = 255 << 24;
	private static final int LOW_ALPHA = 150 << 24;

	private static final int MED_ALPHA = 180 << 24;

	protected static final String OPEN_EMAIL_MARKER = " <";

	public static final void applyAlphaAnimation(ViewFlipper v) {
		final AlphaAnimation in = new AlphaAnimation(0.0f, 1.0f);

		in.setStartOffset(0);
		in.setDuration(500);

		final AlphaAnimation out = new AlphaAnimation(1.0f, 0.0f);

		out.setStartOffset(0);
		out.setDuration(500);

		v.setInAnimation(in);
		v.setOutAnimation(out);
	}

	/**
	 * Scan through a cursor of calendars and check if names are duplicated.
	 * 
	 * This travels a cursor containing calendar display names and fills in the
	 * provided map with whether or not each name is repeated.
	 * 
	 * @param isDuplicateName
	 *            The map to put the duplicate check results in.
	 * @param cursor
	 *            The query of calendars to check
	 * @param nameIndex
	 *            The column of the query that contains the display name
	 */
	public static void checkForDuplicateNames(Map<String, Boolean> isDuplicateName, Cursor cursor,
			int nameIndex) {
		isDuplicateName.clear();
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			final String displayName = cursor.getString(nameIndex);
			// Set it to true if we've seen this name before, false otherwise
			if (displayName != null) {
				isDuplicateName.put(displayName, isDuplicateName.containsKey(displayName));
			}
		}
	}

	/**
	 * Compares two cursors to see if they contain the same data.
	 * 
	 * @return Returns true of the cursors contain the same data and are not
	 *         null, false otherwise
	 */
	public static boolean compareCursors(Cursor c1, Cursor c2) {
		if (c1 == null || c2 == null) {
			return false;
		}

		final int numColumns = c1.getColumnCount();
		if (numColumns != c2.getColumnCount()) {
			return false;
		}

		if (c1.getCount() != c2.getCount()) {
			return false;
		}

		c1.moveToPosition(-1);
		c2.moveToPosition(-1);
		while (c1.moveToNext() && c2.moveToNext()) {
			for (int i = 0; i < numColumns; i++) {
				if (!TextUtils.equals(c1.getString(i), c2.getString(i))) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Formats the given Time object so that it gives the month and year (for
	 * example, "September 2007").
	 * 
	 * @param time
	 *            the time to format
	 * @return the string containing the weekday and the date
	 */
	public static String formatMonthYear(Context context, Time time) {
		return time.format(context.getResources().getString(R.string.month_year));
	}

	public static String formatNth(int nth) {
		return "the " + englishNthDay[nth];
	}

	public static Drawable getColorChip(int color) {
		/*
		 * We want the color chip to have a nice gradient using the color of the
		 * calendar. To do this we use a GradientDrawable. The color supplied
		 * has an alpha of FF so we first do: color & 0x00FFFFFF to clear the
		 * alpha. Then we add our alpha to it. We use 3 colors to get a step
		 * effect where it starts off very light and quickly becomes dark and
		 * then a slow transition to be even darker.
		 */
		color &= CLEAR_ALPHA_MASK;
		final int startColor = color | HIGH_ALPHA;
		final int middleColor = color | MED_ALPHA;
		final int endColor = color | LOW_ALPHA;
		final int[] colors = new int[] { startColor, middleColor, endColor };
		final GradientDrawable d = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
				colors);
		d.setCornerRadii(CORNERS);
		return d;
	}

	/**
	 * Get first day of week as android.text.format.Time constant.
	 * 
	 * @return the first day of week in android.text.format.Time
	 */
	public static int getFirstDayOfWeek() {
		final int startDay = Calendar.getInstance().getFirstDayOfWeek();
		if (startDay == Calendar.SATURDAY) {
			return Time.SATURDAY;
		} else if (startDay == Calendar.MONDAY) {
			return Time.MONDAY;
		} else {
			return Time.SUNDAY;
		}
	}

	/**
	 * Determine whether the column position is Saturday or not.
	 * 
	 * @param column
	 *            the column position
	 * @param firstDayOfWeek
	 *            the first day of week in android.text.format.Time
	 * @return true if the column is Saturday position
	 */
	public static boolean isSaturday(int column, int firstDayOfWeek) {
		return (firstDayOfWeek == Time.SUNDAY && column == 6)
				|| (firstDayOfWeek == Time.MONDAY && column == 5)
				|| (firstDayOfWeek == Time.SATURDAY && column == 0);
	}

	/**
	 * Determine whether the column position is Sunday or not.
	 * 
	 * @param column
	 *            the column position
	 * @param firstDayOfWeek
	 *            the first day of week in android.text.format.Time
	 * @return true if the column is Sunday position
	 */
	public static boolean isSunday(int column, int firstDayOfWeek) {
		return (firstDayOfWeek == Time.SUNDAY && column == 0)
				|| (firstDayOfWeek == Time.MONDAY && column == 6)
				|| (firstDayOfWeek == Time.SATURDAY && column == 1);
	}

	public static MatrixCursor matrixCursorFromCursor(Cursor cursor) {
		final MatrixCursor newCursor = new MatrixCursor(cursor.getColumnNames());
		final int numColumns = cursor.getColumnCount();
		final String data[] = new String[numColumns];
		cursor.moveToPosition(-1);
		while (cursor.moveToNext()) {
			for (int i = 0; i < numColumns; i++) {
				data[i] = cursor.getString(i);
			}
			newCursor.addRow(data);
		}
		return newCursor;
	}

	/**
	 * Sets the time to the beginning of the day (midnight) by clearing the
	 * hour, minute, and second fields.
	 */
	static void setTimeToStartOfDay(Time time) {
		time.second = 0;
		time.minute = 0;
		time.hour = 0;
	}

	public static void startActivity(Context context, Class<?> cls, long time) {
		final Intent intent = new Intent(Intent.ACTION_VIEW);

		intent.setClass(context, cls);
		intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);

		context.startActivity(intent);
	}

	public static final Time timeFromIntent(Intent intent) {
		final Time time = new Time();
		time.set(timeFromIntentInMillis(intent));
		return time;
	}

	/**
	 * If the given intent specifies a time (in milliseconds since the epoch),
	 * then that time is returned. Otherwise, the current time is returned.
	 */
	public static final long timeFromIntentInMillis(Intent intent) {
		// If the time was specified, then use that. Otherwise, use the current
		// time.
		final Uri data = intent.getData();
		long millis = 0;
		if (data != null && data.isHierarchical()) {
			final List<String> path = data.getPathSegments();
			if (path.size() == 2 && path.get(0).equals("time")) {
				try {
					millis = Long.valueOf(data.getLastPathSegment());
				} catch (final NumberFormatException e) {
					Log.i("Calendar", "timeFromIntentInMillis: Data existed but no valid time "
							+ "found. Using current time.");
				}
			}
		}
		if (millis <= 0) {
			millis = System.currentTimeMillis();
		}
		return millis;
	}
}
