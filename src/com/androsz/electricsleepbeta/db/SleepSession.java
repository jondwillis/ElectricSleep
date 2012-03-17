package com.androsz.electricsleepbeta.db;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.util.PointD;
import static com.androsz.electricsleepbeta.db.ElectricSleepProvider.TimestampColumns;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Build;
import android.provider.BaseColumns;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class SleepSession implements BaseColumns, SleepSessionKeys,
		TimestampColumns {

	private static final String TAG = SleepSession.class.getSimpleName();

	/** Value that marks whether or not the database row id is valid. */
	private static final long ROW_INVALID = 0;

	/**
	 * Path used to access sleep session both via the provider as well as the
	 * table name.
	 */
	public static final String PATH = "sleep_sessions";

	public static final Uri CONTENT_URI = ElectricSleepProvider.BASE_CONTENT_URI
			.buildUpon().appendPath(PATH).build();

	/**
	 * The MIME type of {@link #CONTENT_URI}.
	 */
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.androsz.electricsleepbeta."
			+ PATH;

	/**
	 * The MIME type of a {@link #CONTENT_URI} sub-directory of a single row.
	 */
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.androsz.electricsleepbeta."
			+ PATH;

	public static final String SORT_ORDER = START_TIMESTAMP + " DESC";

	static String[] PROJECTION = new String[] { _ID, START_TIMESTAMP,
			START_JULIAN_DAY, END_TIMESTAMP, TIMEZONE, DATA, DURATION, NOTE,
			RATING, SPIKES, CALIBRATION_LEVEL, MIN, FELL_ASLEEP_TIMESTAMP,
			CREATED_ON, UPDATED_ON };

	float mCalibrationLevel;
	List<PointD> mData;
	long mEndTimestamp;
	long mId;
	String mNote;
	TimeZone mTimezone;
	double mMin;
	int mRating;
	int mSpikes;
	long mDuration;
	long mFellAsleepTimestamp;
	long mStartTimestamp;
	int mStartJulianDay;

	long mCreatedOn;
	long mUpdatedOn;

	public SleepSession(final Cursor cursor) {
		mId = cursor.getLong(cursor.getColumnIndex(_ID));
		mStartTimestamp = cursor
				.getLong(cursor.getColumnIndex(START_TIMESTAMP));
		mStartJulianDay = cursor
				.getInt(cursor.getColumnIndex(START_JULIAN_DAY));
		mEndTimestamp = cursor.getLong(cursor.getColumnIndex(END_TIMESTAMP));
		mTimezone = TimeZone.getTimeZone(cursor.getString(cursor
				.getColumnIndex(TIMEZONE)));
		mCalibrationLevel = cursor.getFloat(cursor
				.getColumnIndex(CALIBRATION_LEVEL));
		mMin = cursor.getDouble(cursor.getColumnIndex(MIN));
		mRating = cursor.getInt(cursor.getColumnIndex(RATING));
		mSpikes = cursor.getInt(cursor.getColumnIndex(SPIKES));
		mDuration = cursor.getLong(cursor.getColumnIndex(DURATION));
		mFellAsleepTimestamp = cursor.getLong(cursor
				.getColumnIndex(FELL_ASLEEP_TIMESTAMP));
		mNote = cursor.getString(cursor.getColumnIndex(NOTE));

		mCreatedOn = cursor.getLong(cursor.getColumnIndex(CREATED_ON));
		mUpdatedOn = cursor.getLong(cursor.getColumnIndex(UPDATED_ON));

		try {
			mData = (List<PointD>) byteArrayToObject(cursor.getBlob(cursor
					.getColumnIndexOrThrow(DATA)));
		} catch (final Exception e) {
			GoogleAnalyticsTracker.getInstance().trackEvent(
					Integer.toString(VERSION.SDK_INT), Build.MODEL,
					"sleepSessionInstatiation : " + e.getMessage(), 0);
		}
	}

	public SleepSession(final long startTimestamp, final long endTimestamp,
			final List<PointD> data, final double min,
			final float calibrationLevel, final int rating,
			final long duration, final int spikes, final long fellAsleep,
			final String note) {
		mStartTimestamp = startTimestamp;
		mStartJulianDay = getZeoJulianDay(startTimestamp);
		mEndTimestamp = endTimestamp;
		mData = data;
		mMin = min;
		mCalibrationLevel = calibrationLevel;
		mRating = rating;
		mDuration = duration;
		mSpikes = spikes;
		mFellAsleepTimestamp = fellAsleep;
		mNote = note;
		mTimezone = TimeZone.getDefault();
	}

	public float getCalibrationLevel() {
		return mCalibrationLevel;
	}

	public List<PointD> getData() {
		return mData;
	}

	/**
	 * Return annotation of date / time text such as: Thu, Jan 14
	 *
	 * WARNING: the following does not follow the 6pm to 6am convention.
	 */
	public String getDayText(final Context context) {
		// TODO The following must follow the 6pm to 6am rule.
		return DateUtils.formatDateTime(context, mStartTimestamp,
				DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE
						| DateUtils.FORMAT_ABBREV_ALL);
	}

	public long getDuration() {
		return mDuration;
	}

	@Deprecated
	public CharSequence getDurationText(final Resources res) {
		return getTimespanText(mDuration, res);
	}

	public String getEfficiency() {
		return Integer.toString(getSleepScore());
	}

	public int getEndJulianDay() {
		final Time local = new Time();
		local.set(getEndTimestamp());
		final long millis = local.normalize(true /* ignore DST */);
		return Time.getJulianDay(millis, local.gmtoff);
	}

	public long getEndTimestamp() {
		return mEndTimestamp;
	}

	public long getEndTimeOfDay() {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getEndTimestamp());
		return cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY) * 60);
	}

	public long getLocalizedStartTimestamp() {
		return mStartTimestamp + mTimezone.getOffset(mStartTimestamp);
	}

	public long getLocalizedEndTimestamp() {
		return mEndTimestamp + mTimezone.getOffset(mEndTimestamp);
	}

	@Deprecated
	public String getNote() {
		return mNote;
	}

	public String getNotes() {
		return mNote;
	}

	public int getRating() {
		return mRating;
	}

	public int getSleepScore() {
		int score = 0;
		final float ratingPct = (mRating - 1) / 4f;
		// final float deepPct = Math.min(1, 15f / spikes);
		final float fifteenMinutes = 1000 * 60 * 15;
		final float eightHours = 1000 * 60 * 60 * 8;
		
		final float diffFrom8HoursPct = 1 - Math.max(0, Math.abs((mDuration - eightHours))
				/ eightHours);

		long timeToFallAsleep = getTimeToFallAsleep();
		if(timeToFallAsleep == DID_NOT_FALL_ASLEEP)
		{
			// we didn't fall asleep. Give a zero-score?
			score = 0;
		}
		else
		{
			final float timeToFallAsleepPct = fifteenMinutes
					/ Math.max(timeToFallAsleep, fifteenMinutes);

			score = Math
					.round((ratingPct /* + deepPct */+ diffFrom8HoursPct + timeToFallAsleepPct) / 3 * 100);
		}

		return score;
	}

	public int getSpikes() {
		return mSpikes;
	}

	/**
	 * Computes the julian day for the given timestamp while taking into
	 * consideration Zeo's rules for gauging a night of sleep between the 12am
	 * to 6am time window.
	 */
	public static int getZeoJulianDay(final long timestamp) {
		final Time startTime = new Time();
		startTime.set(timestamp);
		int julianDay = Time.getJulianDay(startTime.normalize(true),
				startTime.gmtoff);

		// Begin process of determining if this record was from 12am to 6am.
		final Time midnight = new Time();
		midnight.set(startTime);
		midnight.hour = 0;
		midnight.minute = 0;
		midnight.second = 0;
		midnight.normalize(true);

		final Time morning = new Time();
		morning.set(midnight);
		morning.hour = 6;
		morning.normalize(true);

		if (Time.compare(startTime, midnight) >= 0
				&& Time.compare(startTime, morning) <= 0) {
			--julianDay;
		}

		return julianDay;
	}

	/**
	 * Return the julian day for this sleep session while taking into account
	 * Zeo's 6pm to 6am rule for determining what date this sleep applies to.
	 *
	 * The Zeo 6pm to 6am rule states that any sleep that occurs from 12am to
	 * 6am is actually sleep representing the day prior.
	 */
	public int getStartJulianDay() {
		return getZeoJulianDay(mStartTimestamp);
	}

	public long getStartTimestamp() {
		return mStartTimestamp;
	}

	public long getStartTimeOfDay() {
		final Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getStartTimestamp());
		return cal.get(Calendar.MINUTE) + (cal.get(Calendar.HOUR_OF_DAY) * 60);
	}

	public static final long DID_NOT_FALL_ASLEEP = 0;
	public long getTimeToFallAsleep() {
		if (mFellAsleepTimestamp > mStartTimestamp) {
			return mFellAsleepTimestamp - getStartTimestamp();
		} else {
			return DID_NOT_FALL_ASLEEP;
		}
	}

	public CharSequence getTimeToFallAsleepText(final Resources res) {
		long timeToFallAsleep = getTimeToFallAsleep();
		if (timeToFallAsleep == DID_NOT_FALL_ASLEEP) {
            return "----";
        }
        return getTimespanText(timeToFallAsleep, res);
	}

	public CharSequence getTotalRecordAbbrevTime(final Resources res) {
		return getTimespanAbbrevText(mDuration, res);
	}

	public CharSequence getTotalRecordTime(final Resources res) {
		return getTimespanText(mDuration, res);
	}

	public String getTimesDisrupted() {
		return Integer.toString(mSpikes);
	}

	public TimeZone getTimeZone() {
		return mTimezone;
	}

	/**
	 * Return a title for the night of sleep similar to the form: M/D/Y HH:MM -
	 * HH:MM but of course using the user's Android settings.
	 *
	 * WARNING: timezone is not properly handled here.
	 */
	public String getTitle(final Context context) {
		return DateUtils.formatDateTime(context, mStartTimestamp,
				DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_NUMERIC_DATE)
				+ " "
				+ DateUtils.formatDateTime(context, mStartTimestamp,
						DateUtils.FORMAT_SHOW_TIME)
				+ " - "
				+ DateUtils.formatDateTime(context, mEndTimestamp,
						DateUtils.FORMAT_SHOW_TIME);
	}

	public ContentValues toContentValues() {
		final ContentValues values = new ContentValues(11);
		if (mId != ROW_INVALID) {
			values.put(_ID, mId);
		}
		values.put(START_TIMESTAMP, mStartTimestamp);
		values.put(START_JULIAN_DAY, mStartJulianDay);
		values.put(END_TIMESTAMP, mEndTimestamp);
		values.put(TIMEZONE, mTimezone.getID());
		values.put(DURATION, mDuration);
		values.put(NOTE, mNote);
		values.put(RATING, mRating);
		values.put(SPIKES, mSpikes);
		values.put(CALIBRATION_LEVEL, mCalibrationLevel);
		values.put(MIN, mMin);
		values.put(FELL_ASLEEP_TIMESTAMP, mFellAsleepTimestamp);

		try {
			values.put(DATA, SleepSession.objectToByteArray(mData));
		} catch (IOException e) {
			Log.w(TAG, "Failure to marshall sleep data to byte array.");
			GoogleAnalyticsTracker.getInstance().trackEvent(
					Integer.toString(VERSION.SDK_INT), Build.MODEL,
					"createSessionIOException : " + e.getMessage(), 0);
		}
		return values;
	}

	public static Object byteArrayToObject(final byte[] bytes)
			throws StreamCorruptedException, IOException,
			ClassNotFoundException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final ObjectInputStream ois = new ObjectInputStream(bais);

		return ois.readObject();
	}

	public static Calendar getTimeDiffCalendar(final long time) {
		// set calendar to GMT +0
		final Calendar timeDiffCalendar = Calendar.getInstance(TimeZone
				.getTimeZone(TimeZone.getAvailableIDs(0)[0]));
		timeDiffCalendar.setTimeInMillis(time);
		return timeDiffCalendar;
	}

	public static CharSequence getTimespanAbbrevText(long timespanMs,
			final Resources res) {
		final Calendar timespan = getTimeDiffCalendar(timespanMs);
		final int hours = Math.min(24, timespan.get(Calendar.HOUR_OF_DAY));
		final int minutes = timespan.get(Calendar.MINUTE);
		final int seconds = timespan.get(Calendar.SECOND);
		StringBuilder builder = new StringBuilder();
		if (hours > 0) {
			builder.append(Integer.toString(hours) + "h");
		}
		if (minutes > 0) {
			if (hours > 0) {
				builder.append(" ");
			}
			builder.append(Integer.toString(minutes) + "m");
		}
		if (seconds > 0 && hours == 0) {
			if (hours > 0 || minutes > 0) {
				builder.append(" ");
			}
			builder.append(Integer.toString(seconds) + "s");
		}
		return builder.toString();
	}

	public static CharSequence getTimespanText(long timespanMs,
			final Resources res) {
		final Calendar timespan = getTimeDiffCalendar(timespanMs);
		final int hours = Math.min(24, timespan.get(Calendar.HOUR_OF_DAY));
		final int minutes = timespan.get(Calendar.MINUTE);
		return res.getQuantityString(R.plurals.hour, hours, hours) + " "
				+ res.getQuantityString(R.plurals.minute, minutes, minutes);
	}

	public static byte[] objectToByteArray(final Object obj) throws IOException {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final ObjectOutputStream oos = new ObjectOutputStream(baos);
		oos.writeObject(obj);

		return baos.toByteArray();
	}

	/**
	 * Given a cursor generate a List of long values that contain: start
	 * timestamp end timestamp start julian day end julian day database row id
	 *
	 * WARNING We assume that the cursor given to us will have projection in the
	 * form: new String[] {_ID, START_TIMESTAMP, END_TIMESTAMP, TIMEZONE}
	 */
	public static List<Long[]> getStartEndTimestamps(Cursor cursor) {
		List<Long[]> result = new ArrayList<Long[]>();
		if (cursor.moveToFirst()) {
			do {
				// TODO this is convoluted
				final long id = cursor.getLong(0);
				final long startTimestamp = cursor.getLong(1);
				final long endTimestamp = cursor.getLong(2);
				final SleepSession session = new SleepSession(cursor);
				result.add(new Long[] { startTimestamp, endTimestamp,
						(long) session.getStartJulianDay(),
						(long) session.getEndJulianDay(), id });
			} while (cursor.moveToNext());
		}
		return result;
	}
}

interface SleepSessionKeys {
	String CALIBRATION_LEVEL = "calibration_level";
	String DATA = "data";
	String DURATION = "duration";
	String END_TIMESTAMP = "end_timestamp";
	String FELL_ASLEEP_TIMESTAMP = "fell_asleep_timestamp";
	String MIN = "min";
	String NOTE = "note";
	String RATING = "rating";
	String SLEEP_DATA = "data";
	String SPIKES = "spikes";
	String START_JULIAN_DAY = "start_julian_day";
	String START_TIMESTAMP = "start_timestamp";
	String TIMEZONE = "timezone";
}
