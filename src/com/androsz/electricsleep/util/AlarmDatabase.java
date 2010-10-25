package com.androsz.electricsleep.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.util.Log;

/**
 * Helper class provides interoperability with Android AlarmClock database.
 * Based on com.android.alarmclock.Alarms class
 * <p/>
 * Copyright (c) 2005-2008, The Android Open Source Project Copyright (c) 2009,
 * Alexander Kosenkov Copyright (c) 2010, Jonathan Willis (modifications and
 * adoption into ElectricSleep) Licensed under the Apache License, Version 2.0
 * (the "License");
 * <p/>
 * Project home: http://code.google.com/p/android-alarmclock-database/
 */
public class AlarmDatabase {

	/**
	 * Represents single Alarm Clock record in a database. Immutable
	 */
	public static class Record {
		public final int id;
		public final boolean vibrate;
		public final String message;
		public final String audio;

		public final int hour;
		public final int minute;

		private Calendar nearestAlarmDate;
		private final int daysOfWeek;

		public Record(final Cursor cur) {
			if (column_id == -1) {
				column_id = cur.getColumnIndexOrThrow("_id");
				column_hour = cur.getColumnIndexOrThrow("hour");
				column_minutes = cur.getColumnIndexOrThrow("minutes");
				column_daysofweek = cur.getColumnIndexOrThrow("daysofweek");
				column_vibrate = cur.getColumnIndexOrThrow("vibrate");
				column_message = cur.getColumnIndexOrThrow("message");
				column_alert = cur.getColumnIndexOrThrow("alert");
			}

			// Get the field values
			hour = cur.getInt(column_hour);
			minute = cur.getInt(column_minutes);
			daysOfWeek = cur.getInt(column_daysofweek);
			id = cur.getInt(column_id);
			vibrate = cur.getInt(column_vibrate) == 1;
			message = cur.getString(column_message);
			audio = cur.getString(column_alert);
		}

		@Override
		public boolean equals(final Object o) {
			if (this == o) {
				return true;
			}
			if (o == null || getClass() != o.getClass()) {
				return false;
			}

			final Record record = (Record) o;

			if (daysOfWeek != record.daysOfWeek) {
				return false;
			}
			if (hour != record.hour) {
				return false;
			}
			if (id != record.id) {
				return false;
			}
			if (minute != record.minute) {
				return false;
			}
			if (vibrate != record.vibrate) {
				return false;
			}
			if (audio != null ? !audio.equals(record.audio)
					: record.audio != null) {
				return false;
			}
			if (message != null ? !message.equals(record.message)
					: record.message != null) {
				return false;
			}

			return true;
		}

		public Calendar getNearestAlarmDate() {
			if (nearestAlarmDate == null) {
				nearestAlarmDate = calculateNextAlarm(hour, minute, daysOfWeek,
						System.currentTimeMillis());
			}
			return nearestAlarmDate;
		}

		@Override
		public int hashCode() {
			int result = id;
			result = 31 * result + (vibrate ? 1 : 0);
			result = 31 * result + (message != null ? message.hashCode() : 0);
			result = 31 * result + (audio != null ? audio.hashCode() : 0);
			result = 31 * result + hour;
			result = 31 * result + minute;
			result = 31 * result + daysOfWeek;
			return result;
		}
	}

	public static Calendar calculateNextAlarm(final int hour, final int minute,
			final int daysOfWeek, final long minimumTime) {

		// newRecord with now
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(minimumTime);

		final int nowHour = c.get(Calendar.HOUR_OF_DAY);
		final int nowMinute = c.get(Calendar.MINUTE);

		// if alarmclock is behind current time, advance one day
		if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final int addDays = getNextAlarm(c, daysOfWeek);
		/*
		 * Log.v("** TIMES * " + c.getTimeInMillis() + " hour " + hour +
		 * " minute " + minute + " dow " + c.get(Calendar.DAY_OF_WEEK) +
		 * " from now " + addDays);
		 */
		if (addDays > 0) {
			c.add(Calendar.DAY_OF_WEEK, addDays);
		}
		return c;
	}

	/**
	 * Call startActivity() on result of this method to show default UI for
	 * changing Alarm Clock settings
	 * 
	 * @param packageManager
	 *            may be null
	 * @return Intent for changing alarmclock settings
	 */
	public static Intent changeAlarmSettings(final PackageManager packageManager) {
		final Intent i = new Intent();
		i.setAction(Intent.ACTION_MAIN);

		i.setClassName("com.androsz.electricsleep.deskclock",
				"com.androsz.electricsleep.deskclock.AlarmClock");
		ResolveInfo resolved = packageManager.resolveActivity(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (resolved != null) {
			return i; // 2.2
		}
		
		

		i.setClassName("com.htc.android.worldclock",
				"com.htc.android.worldclock.WorldClockTabControl");
		resolved = packageManager.resolveActivity(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (resolved != null) {
			return i; // HTC custom UI
		}

		i.setClassName("com.motorola.blur.alarmclock",
				"com.motorola.blur.alarmclock.AlarmClock");
		resolved = packageManager.resolveActivity(i,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (resolved != null) {
			return i; // Motoblur clock
		}

		return null;
	}

	/**
	 * returns number of days from today until next alarmclock
	 * 
	 * @param c
	 *            must be set to today
	 * @param mDays
	 *            alarmclock-clock internal days representation
	 * @return days count
	 */
	private static int getNextAlarm(final Calendar c, final int mDays) {
		if (mDays == 0) {
			return -1;
		}
		final int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

		int day, dayCount;
		for (dayCount = 0; dayCount < 7; dayCount++) {
			day = (today + dayCount) % 7;
			if ((mDays & 1 << day) > 0) {
				break;
			}
		}
		return dayCount;
	}

	public static List<ApplicationInfo> getPossibleAlarmClocks(
			final PackageManager packageManager) {
		final List<ApplicationInfo> appsInfo = packageManager
				.getInstalledApplications(0);
		final List<ApplicationInfo> clockApps = new ArrayList<ApplicationInfo>();
		for (final ApplicationInfo appInfo : appsInfo) {
			final String x = appInfo.dataDir.toLowerCase();
			if (x.contains("clock") || x.contains("alarm")) {
				clockApps.add(appInfo);
			}
		}
		return clockApps;
	}

	public static void triggerAlarm(final Context context, final Alarm alarm) {
		final Intent intent = new Intent("com.androsz.electricsleep.deskclock.ALARM_ALERT");

		final Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra("intent.extra.alarm_raw", out.marshall());

		context.sendBroadcast(intent);
	}

	private final Uri mAlarmUri;
	private final ContentResolver mContentResolver;
	private final ContentObserver mContentObserver;

	private static int column_id = -1;

	private static int column_hour;

	private static int column_minutes;

	private static int column_daysofweek;

	private static int column_vibrate;

	private static int column_message;

	private static int column_alert;

	/**
	 * Creates database connection and subscribes for updates
	 * 
	 * @param contentResolver
	 *            get from context
	 * @param contentObserver
	 *            will be called for external database updates
	 */
	public AlarmDatabase(final ContentResolver contentResolver,
			final ContentObserver contentObserver, final String packageName) {
		mContentResolver = contentResolver;
		mAlarmUri = Uri.parse("content://" + packageName + "/alarm");

		if (contentObserver != null) {
			mContentObserver = contentObserver;
			contentResolver.registerContentObserver(mAlarmUri, true,
					mContentObserver);
		} else {
			mContentObserver = null;
		}
	}

	/**
	 * Creates database connection and subscribes for updates
	 * 
	 * @param contentResolver
	 *            get from context
	 * @param contentObserver
	 *            Runnable to be called if database changes
	 * @param handler
	 */
	public AlarmDatabase(final ContentResolver contentResolver,
			final Runnable contentObserver, final Handler handler,
			final String packageName) {
		this(contentResolver, new ContentObserver(handler) {
			@Override
			public boolean deliverSelfNotifications() {
				return false;
			}

			@Override
			public void onChange(final boolean selfChange) {
				contentObserver.run();
			}
		}, packageName);
	}

	/**
	 * Creates database connection with no updates subscription. You'd probably
	 * need to subscribe for updates
	 * 
	 * @param contentResolver
	 *            get from context
	 */
	public AlarmDatabase(final ContentResolver contentResolver,
			final String packageName) {
		this(contentResolver, (ContentObserver) null, packageName);
	}

	@Override
	protected void finalize() throws Throwable {
		if (mContentResolver != null && mContentObserver != null) {
			mContentResolver.unregisterContentObserver(mContentObserver);
		}
		super.finalize();
	}

	public Record getAlarmById(final int alarmId) {
		final Cursor cur = mContentResolver.query(mAlarmUri, null, "_id=?",
				new String[] { String.valueOf(alarmId) }, null);

		if (!cur.moveToFirst()) {
			Log.w("AlarmDatabase", "no record for id " + alarmId);
			return null;
		}
		final Record entity = new Record(cur);
		cur.close();

		return entity;
	}

	public Alarm getNearestEnabledAlarm() throws UnsupportedOperationException {
		return getNearestEnabledAlarm(System.currentTimeMillis());
	}

	public Alarm getNearestEnabledAlarm(final long minimumTime)
			throws UnsupportedOperationException {
		final Cursor cur = mContentResolver.query(mAlarmUri, null, "enabled=?",
				new String[] { "1" }, null);

		if (cur == null) {
			Log.w("AlarmDatabase", "Cannot resolve provider for " + mAlarmUri);
			throw new UnsupportedOperationException();
		}

		if (!cur.moveToFirst()) {
			Log.d("AlarmDatabase", "No enabled alarms");
			return null;
		}

		Alarm nearest = null;

		do {
			final Alarm current = new Alarm(cur); // NB: side-effect!!
			final Calendar cal = current.getNearestAlarmDate();

			if ((nearest == null || cal
					.compareTo(nearest.getNearestAlarmDate()) == -1)
					&& cal.getTimeInMillis() > minimumTime) {
				nearest = new Alarm(cur);
				nearest.nearestAlarmDate = cal;
			}

		} while (cur.moveToNext());
		cur.close();
		return nearest;
	}
}