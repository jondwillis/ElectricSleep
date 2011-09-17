package com.androsz.electricsleepbeta.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSessions.MainTable;
import com.androsz.electricsleepbeta.util.PointD;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SleepSession {

	public static Object byteArrayToObject(final byte[] bytes)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final ObjectInputStream ois = new ObjectInputStream(bais);

		return ois.readObject();
	}

	/**
	 * Computes a position for each event. Each event is displayed as a
	 * non-overlapping rectangle. For normal events, these rectangles are
	 * displayed in separate columns in the week view and day view. For all-day
	 * events, these rectangles are displayed in separate rows along the top. In
	 * both cases, each event is assigned two numbers: N, and Max, that specify
	 * that this event is the Nth event of Max number of events that are
	 * displayed in a group. The width and position of each rectangle depend on
	 * the maximum number of rectangles that occur at the same time.
	 * 
	 * @param eventsList
	 *            the list of events, sorted into increasing time order
	 */
	public static void computePositions(Collection<SleepSession> eventsList) {
		if (eventsList == null) {
			return;
		}

		doComputePositions(eventsList);
	}

	private static void doComputePositions(Collection<SleepSession> eventsList) {
		final Collection<SleepSession> activeList = new ArrayList<SleepSession>();
		final Collection<SleepSession> groupList = new ArrayList<SleepSession>();

		long colMask = 0;
		int maxCols = 0;
		for (final SleepSession record : eventsList) {

			// long start = record.getStartTime();

			// Remove the inactive events. An event on the active list
			// becomes inactive when its end time is less than or equal to
			// the current event's start time.
			/*
			 * Iterator<SleepRecord> iter = activeList.iterator(); while
			 * (iter.hasNext()) { SleepRecord active = iter.next(); if
			 * (active.getEndTime() <= start) { colMask &= ~(1L <<
			 * active.getColumn()); iter.remove(); } }
			 */

			// If the active list is empty, then reset the max columns, clear
			// the column bit mask, and empty the groupList.
			if (activeList.isEmpty()) {
				for (final SleepSession ev : groupList) {
					ev.setMaxColumns(maxCols);
				}
				maxCols = 0;
				colMask = 0;
				groupList.clear();
			}

			// Find the first empty column. Empty columns are represented by
			// zero bits in the column mask "colMask".
			int col = findFirstZeroBit(colMask);
			if (col == 64) {
				col = 63;
			}
			colMask |= (1L << col);
			record.setColumn(col);
			activeList.add(record);
			groupList.add(record);
			final int len = activeList.size();
			if (maxCols < len) {
				maxCols = len;
			}
		}
		for (final SleepSession ev : groupList) {
			ev.setMaxColumns(maxCols);
		}
	}

	public static int findFirstZeroBit(long val) {
		for (int ii = 0; ii < 64; ++ii) {
			if ((val & (1L << ii)) == 0) {
				return ii;
			}
		}
		return 64;
	}

	public static Calendar getTimeDiffCalendar(final long time) {
		// set calendar to GMT +0
		final Calendar timeDiffCalendar = Calendar.getInstance(TimeZone
				.getTimeZone(TimeZone.getAvailableIDs(0)[0]));
		timeDiffCalendar.setTimeInMillis(time);
		return timeDiffCalendar;
	}

	public static CharSequence getTimespanText(long timespanMs,
			final Resources res) {
		final Calendar timespan = getTimeDiffCalendar(timespanMs);
		final int hours = Math.min(24, timespan.get(Calendar.HOUR_OF_DAY));
		final int minutes = timespan.get(Calendar.MINUTE);
		return res.getQuantityString(R.plurals.hour, hours, hours) + " "
				+ res.getQuantityString(R.plurals.minute, minutes, minutes);
	}

	/**
	 * Loads <i>days</i> days worth of instances starting at <i>start</i>.
	 */
	public static void loadEvents(Context context,
			Collection<SleepSession> events, long start, int days) {

		events.clear();
			final Time local = new Time();
			int count;

			local.set(start);

			// expand start and days to include days shown from previous month
			// and next month. can be slightly wasteful.
			//start -= 1000 * 60 * 60 * 24 * 7; // 7 days
			//days += 7;

			Time.getJulianDay(start, local.gmtoff);
			local.monthDay += days;
			final long end = local.normalize(true );

			// Widen the time range that we query by one day on each end
			// so that we can catch all-day events. All-day events are
			// stored starting at midnight in UTC but should be included
			// in the list of events starting at midnight local time.
			// This may fetch more events than we actually want, so we
			// filter them out below.
			//
			// The sort order is: events with an earlier start time occur
			// first and if the start times are the same, then events with
			// a later end time occur first. The later end time is ordered
			// first so that long rectangles in the calendar views appear on
			// the left side. If the start and end times of two events are
			// the same then we sort alphabetically on the title. This isn't
			// required for correctness, it just adds a nice touch.

			// String orderBy = "";//Instances.SORT_CALENDAR_VIEW;
			// final SleepHistoryDatabase shdb = new
			// SleepHistoryDatabase(context);
			// TODO: hook this into sleep db

			context.getResources();
			for(SleepSession s : events)
			{
				final long startTime = s.getStartTime();
				if (startTime >= start && startTime <= end) {
					final List<PointD> justFirstAndLast = new ArrayList<PointD>();
					justFirstAndLast.add(s.chartData.get(0));
					justFirstAndLast
							.add(s.chartData.get(s.chartData.size() - 1));
					s.chartData = justFirstAndLast; // remove reference to the
													// list, helps lessen memory
													// usage
					events.add(s);
				}
			}

			computePositions(events);
	}

	public static byte[] objectToByteArray(final Object obj) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);

		return baos.toByteArray();
	}

	public final double alarm;
	public float bottom;

	public List<PointD> chartData;

	public final long duration;

	public final long fellAsleep;

	// The coordinates of the event rectangle drawn on the screen.
	public float left;

	private int mColumn;

	//

	public final double min;

	private int mMaxColumns;
	public final String note;
	public final int rating;
	public float right;
	public final int spikes;

	public final String title;

	public float top;

	@SuppressWarnings("unchecked")
	public SleepSession(final Cursor cursor) {

		title = cursor.getString(cursor
				.getColumnIndexOrThrow(MainTable.KEY_TITLE));
		chartData = null;
		try {
			chartData = (List<PointD>) byteArrayToObject(cursor.getBlob(cursor
					.getColumnIndexOrThrow(MainTable.KEY_SLEEP_DATA)));
		} catch (Exception e) {

			GoogleAnalyticsTracker.getInstance().trackEvent(
					Integer.toString(VERSION.SDK_INT), Build.MODEL,
					"sleepSessionInstatiation : " + e.getMessage(), 0);
		}

		min = cursor.getDouble(cursor.getColumnIndexOrThrow(MainTable.KEY_MIN));
		alarm = cursor.getDouble(cursor
				.getColumnIndexOrThrow(MainTable.KEY_ALARM));
		rating = cursor.getInt(cursor
				.getColumnIndexOrThrow(MainTable.KEY_RATING));

		duration = cursor.getLong(cursor
				.getColumnIndexOrThrow(MainTable.KEY_DURATION));
		spikes = cursor.getInt(cursor
				.getColumnIndexOrThrow(MainTable.KEY_SPIKES));
		fellAsleep = cursor.getLong(cursor
				.getColumnIndexOrThrow(MainTable.KEY_TIME_FELL_ASLEEP));
		note = cursor.getString(cursor
				.getColumnIndexOrThrow(MainTable.KEY_NOTE));
	}

	public SleepSession(final String title, final List<PointD> chartData,
			final double min, final double alarm, final int rating,
			final long duration, final int spikes, final long fellAsleep,
			final String note) {
		this.title = title;
		this.chartData = chartData;
		this.min = min;
		this.alarm = alarm;
		this.rating = rating;
		this.duration = duration;
		this.spikes = spikes;
		this.fellAsleep = fellAsleep;
		this.note = note;
	}

	public int getColumn() {
		return mColumn;
	}

	public CharSequence getDurationText(final Resources res) {
		return getTimespanText(duration, res);
	}

	public int getEndJulianDay() {
		final Time local = new Time();
		local.set(getEndTime());
		final long millis = local.normalize(true /* ignore DST */);
		return Time.getJulianDay(millis, local.gmtoff);
	}

	public long getEndTime() {
		return Math.round(chartData.get(chartData.size() - 1).x);
	}

	public int getEndTimeOfDay() {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getEndTime());
		return cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY) * 60);
	}

	public CharSequence getFellAsleepText(final Resources res) {
		return getTimespanText(getTimeToFallAsleep(), res);
	}

	public int getMaxColumns() {
		return mMaxColumns;
	}

	public int getSleepScore() {
		int score = 0;
		final float ratingPct = (rating - 1) / 4f;
		// final float deepPct = Math.min(1, 15f / spikes);
		final float fifteenMinutes = 1000 * 60 * 15;
		final float eightHours = 1000 * 60 * 60 * 8;
		final float diffFrom8HoursPct = 1 - Math.abs((duration - eightHours)
				/ eightHours);
		final float timeToFallAsleepPct = fifteenMinutes
				/ Math.max(getTimeToFallAsleep(), fifteenMinutes);
		// ratingPct *= 1;
		// deepPct *= 1;
		// diffFrom8HoursPct *= 1.4;
		// timeToFallAsleepPct *= 0.6;

		score = Math
				.round((ratingPct /* + deepPct */+ diffFrom8HoursPct + timeToFallAsleepPct) / 3 * 100);

		return score;
	}

	public int getStartJulianDay() {
		final Time local = new Time();
		local.set(getStartTime());
		final long millis = local.normalize(true /* ignore DST */);
		return Time.getJulianDay(millis, local.gmtoff);
	}

	public long getStartTime() {
		return Math.round(chartData.get(0).x);
	}

	public int getStartTimeOfDay() {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getStartTime());
		return cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY) * 60);
	}

	public long getTimeToFallAsleep() {
		return this.fellAsleep - getStartTime();
	}

	public void setColumn(int column) {
		mColumn = column;
	}

	public void setMaxColumns(int maxColumns) {
		mMaxColumns = maxColumns;
	}
}
