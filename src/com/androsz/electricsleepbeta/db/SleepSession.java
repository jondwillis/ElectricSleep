package com.androsz.electricsleepbeta.db;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.achartengine.model.PointD;

import android.content.res.Resources;
import android.database.Cursor;
import android.os.Build;
import android.os.Build.VERSION;
import android.text.format.Time;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSessions.MainTable;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class SleepSession {

	public static Object byteArrayToObject(final byte[] bytes) throws StreamCorruptedException,
			IOException, ClassNotFoundException {
		final ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
		final ObjectInputStream ois = new ObjectInputStream(bais);

		return ois.readObject();
	}

	public static Calendar getTimeDiffCalendar(final long time) {
		// set calendar to GMT +0
		final Calendar timeDiffCalendar = Calendar.getInstance(TimeZone.getTimeZone(TimeZone
				.getAvailableIDs(0)[0]));
		timeDiffCalendar.setTimeInMillis(time);
		return timeDiffCalendar;
	}

	public static CharSequence getTimespanText(long timespanMs, final Resources res) {
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

	public final double alarm;

	public List<PointD> chartData;

	public final long duration;

	public final long fellAsleep;

	public final double min;

	public final String note;
	public final int rating;
	public final int spikes;

	public final String title;

	@SuppressWarnings("unchecked")
	public SleepSession(final Cursor cursor) {

		title = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.KEY_TITLE));
		chartData = null;
		try {
			chartData = (List<PointD>) byteArrayToObject(cursor.getBlob(cursor
					.getColumnIndexOrThrow(MainTable.KEY_SLEEP_DATA)));
		} catch (final Exception e) {

			GoogleAnalyticsTracker.getInstance().trackEvent(Integer.toString(VERSION.SDK_INT),
					Build.MODEL, "sleepSessionInstatiation : " + e.getMessage(), 0);
		}

		min = cursor.getDouble(cursor.getColumnIndexOrThrow(MainTable.KEY_MIN));
		alarm = cursor.getDouble(cursor.getColumnIndexOrThrow(MainTable.KEY_ALARM));
		rating = cursor.getInt(cursor.getColumnIndexOrThrow(MainTable.KEY_RATING));

		duration = cursor.getLong(cursor.getColumnIndexOrThrow(MainTable.KEY_DURATION));
		spikes = cursor.getInt(cursor.getColumnIndexOrThrow(MainTable.KEY_SPIKES));
		fellAsleep = cursor.getLong(cursor.getColumnIndexOrThrow(MainTable.KEY_TIME_FELL_ASLEEP));
		note = cursor.getString(cursor.getColumnIndexOrThrow(MainTable.KEY_NOTE));
	}

	public SleepSession(final String title, final List<PointD> chartData, final double min,
			final double alarm, final int rating, final long duration, final int spikes,
			final long fellAsleep, final String note) {
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

	public int getSleepScore() {
		int score = 0;
		final float ratingPct = (rating - 1) / 4f;
		// final float deepPct = Math.min(1, 15f / spikes);
		final float fifteenMinutes = 1000 * 60 * 15;
		final float eightHours = 1000 * 60 * 60 * 8;
		final float diffFrom8HoursPct = 1 - Math.abs((duration - eightHours) / eightHours);
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

}
