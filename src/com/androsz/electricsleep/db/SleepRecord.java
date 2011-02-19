package com.androsz.electricsleep.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
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
import android.provider.BaseColumns;
import android.text.format.Time;
import android.util.Log;

import com.androsz.electricsleepdonate.R;
import com.androsz.electricsleepbeta.util.PointD;

public class SleepRecord {

	public static final HashMap<String, String> COLUMN_MAP = buildColumnMap();

	// The columns we'll include in the dictionary table
	// DATABASE_VERSION = 3
	public static final String KEY_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;

	public static final String KEY_SLEEP_DATA = "sleep_data";
	public static final String KEY_MIN = "sleep_data_min";
	public static final String KEY_ALARM = "sleep_data_alarm";
	public static final String KEY_RATING = "sleep_data_rating";
	// DATABASE_VERSION = 4
	public static final String KEY_DURATION = "KEY_SLEEP_DATA_DURATION";
	public static final String KEY_SPIKES = "KEY_SLEEP_DATA_SPIKES";
	public static final String KEY_TIME_FELL_ASLEEP = "KEY_SLEEP_DATA_TIME_FELL_ASLEEP";
	public static final String KEY_NOTE = "KEY_SLEEP_DATA_NOTE";

	/**
	 * Builds a map for all columns that may be requested, which will be given
	 * to the SQLiteQueryBuilder. This is a good way to define aliases for
	 * column names, but must include all columns, even if the value is the key.
	 * This allows the ContentProvider to request columns w/o the need to know
	 * real column names and create the alias itself.
	 */
	private static HashMap<String, String> buildColumnMap() {

		final HashMap<String, String> map = new HashMap<String, String>();
		map.put(KEY_TITLE, KEY_TITLE);
		map.put(KEY_SLEEP_DATA, KEY_SLEEP_DATA);
		map.put(KEY_MIN, KEY_MIN);
		map.put(KEY_ALARM, KEY_ALARM);
		map.put(KEY_RATING, KEY_RATING);
		map.put(KEY_DURATION, KEY_DURATION);
		map.put(KEY_SPIKES, KEY_SPIKES);
		map.put(KEY_TIME_FELL_ASLEEP, KEY_TIME_FELL_ASLEEP);
		map.put(KEY_NOTE, KEY_NOTE);

		map.put(BaseColumns._ID, "rowid AS " + BaseColumns._ID);
		map.put(SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID);
		map.put(SearchManager.SUGGEST_COLUMN_SHORTCUT_ID, "rowid AS "
				+ SearchManager.SUGGEST_COLUMN_SHORTCUT_ID);
		return map;
	}

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
	static void computePositions(ArrayList<SleepRecord> eventsList) {
		if (eventsList == null)
			return;

		doComputePositions(eventsList);
	}

	private static void doComputePositions(ArrayList<SleepRecord> eventsList) {
		final ArrayList<SleepRecord> activeList = new ArrayList<SleepRecord>();
		final ArrayList<SleepRecord> groupList = new ArrayList<SleepRecord>();

		long colMask = 0;
		int maxCols = 0;
		for (final SleepRecord record : eventsList) {

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
				for (final SleepRecord ev : groupList) {
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
		for (final SleepRecord ev : groupList) {
			ev.setMaxColumns(maxCols);
		}
	}

	public static int findFirstZeroBit(long val) {
		for (int ii = 0; ii < 64; ++ii) {
			if ((val & (1L << ii)) == 0)
				return ii;
		}
		return 64;
	}

	/**
	 * Loads <i>days</i> days worth of instances starting at <i>start</i>.
	 */
	public static void loadEvents(Context context,
			ArrayList<SleepRecord> events, long start, int days, int requestId,
			AtomicInteger sequenceNumber) {

		Cursor c = null;

		events.clear();
		try {
			final Time local = new Time();
			int count;

			local.set(start);
			Time.getJulianDay(start, local.gmtoff);
			local.monthDay += days;
			final long end = local.normalize(true /* ignore isDst */);

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
			final SleepHistoryDatabase shdb = new SleepHistoryDatabase(context);
			// TODO: hook this into sleep db

			c = shdb.getSleepMatches(context.getString(R.string.to),
					new String[] { BaseColumns._ID, SleepRecord.KEY_TITLE,
							SleepRecord.KEY_ALARM, SleepRecord.KEY_DURATION,
							SleepRecord.KEY_MIN, SleepRecord.KEY_NOTE,
							SleepRecord.KEY_RATING, SleepRecord.KEY_SLEEP_DATA,
							SleepRecord.KEY_SPIKES, SleepRecord.KEY_SLEEP_DATA,
							SleepRecord.KEY_TIME_FELL_ASLEEP });

			shdb.close();

			if (c == null) {
				Log.e("Cal", "loadEvents() returned null cursor!");
				return;
			}

			// Check if we should return early because there are more recent
			// load requests waiting.
			if (requestId != sequenceNumber.get())
				return;

			count = c.getCount();

			if (count == 0)
				return;

			context.getResources();
			do {
				final SleepRecord s = new SleepRecord(c);
				final long startTime = s.getStartTime();
				if (startTime > start && startTime < end) {
					events.add(s);
				}
			} while (c.moveToNext());

			computePositions(events);
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static byte[] objectToByteArray(final Object obj) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);

		return baos.toByteArray();
	}

	// The coordinates of the event rectangle drawn on the screen.
	public float left;

	public float right;

	public float top;
	public float bottom;

	private int mColumn;

	private int mMaxColumns;

	public final String title;

	public List<PointD> chartData;

	public final double min;

	//

	public final double alarm;

	public final int rating;
	public final long duration;
	public final int spikes;
	public final long fellAsleep;
	public final String note;

	@SuppressWarnings("unchecked")
	public SleepRecord(final Cursor cursor) {

		title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
		chartData = null;
		try {
			chartData = (List<PointD>) byteArrayToObject(cursor.getBlob(cursor
					.getColumnIndexOrThrow(KEY_SLEEP_DATA)));
		} catch (final StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (final ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		min = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_MIN));
		alarm = cursor.getDouble(cursor.getColumnIndexOrThrow(KEY_ALARM));
		rating = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_RATING));

		duration = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_DURATION));
		spikes = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_SPIKES));
		fellAsleep = cursor.getLong(cursor
				.getColumnIndexOrThrow(KEY_TIME_FELL_ASLEEP));
		note = cursor.getString(cursor.getColumnIndexOrThrow(KEY_NOTE));
	}

	public SleepRecord(final String title, final List<PointD> chartData,
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

	public static CharSequence getTimespanText(long timespanMs,
			final Resources res) {
		final Calendar timespan = getTimeDiffCalendar(timespanMs);
		final int hours = Math.min(24, timespan.get(Calendar.HOUR_OF_DAY));
		final int minutes = timespan.get(Calendar.MINUTE);
		return res.getQuantityString(R.plurals.hour, hours, hours) + " "
				+ res.getQuantityString(R.plurals.minute, minutes, minutes);
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

	public long getTimeToFallAsleep() {
		return this.fellAsleep - getStartTime();
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

	public static Calendar getTimeDiffCalendar(final long time) {
		// set calendar to GMT +0
		final Calendar timeDiffCalendar = Calendar.getInstance(TimeZone
				.getTimeZone(TimeZone.getAvailableIDs(0)[0]));
		timeDiffCalendar.setTimeInMillis(time);
		return timeDiffCalendar;
	}

	public long insertIntoDb(final SQLiteDatabase db) throws IOException {
		long insertResult = -1;
		final ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_TITLE, title);

		initialValues.put(KEY_SLEEP_DATA, objectToByteArray(chartData));

		initialValues.put(KEY_MIN, min);
		initialValues.put(KEY_ALARM, alarm);
		initialValues.put(KEY_RATING, rating);
		initialValues.put(KEY_DURATION, duration);
		initialValues.put(KEY_SPIKES, spikes);
		initialValues.put(KEY_TIME_FELL_ASLEEP, fellAsleep);
		initialValues.put(KEY_NOTE, note);

		insertResult = db.insert(SleepHistoryDatabase.FTS_VIRTUAL_TABLE, null,
				initialValues);
		db.close();
		return insertResult;
	}

	public void setColumn(int column) {
		mColumn = column;
	}

	public void setMaxColumns(int maxColumns) {
		mMaxColumns = maxColumns;
	}
}
