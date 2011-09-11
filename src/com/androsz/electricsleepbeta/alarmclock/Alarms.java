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

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.provider.Settings;
import android.text.format.DateFormat;

import com.androsz.electricsleepbeta.app.SettingsActivity;

/**
 * The Alarms provider supplies info about Alarm Clock settings
 */
public class Alarms {

	// This action triggers the AlarmReceiver as well as the AlarmKlaxon. It
	// is a public action used in the manifest for receiving Alarm broadcasts
	// from the alarm manager.
	public static final String ALARM_ALERT_ACTION = "com.androsz.electricsleepbeta.alarmclock.ALARM_ALERT";

	// This string is used to indicate a silent alarm in the db.
	public static final String ALARM_ALERT_SILENT = "com.androsz.electricsleepbeta.alarmclock.silent";

	// AlarmAlertFullScreen listens for this broadcast intent, so that other
	// applications
	// can dismiss the alarm (after ALARM_ALERT_ACTION and before
	// ALARM_DONE_ACTION).
	public static final String ALARM_DISMISS_ACTION = "com.androsz.electricsleepbeta.alarmclock.ALARM_DISMISS";

	// A public action that is broadcasted when the user dismisses an alarm.
	// Other applications may listen for this to.
	public static final String ALARM_DISMISSED_BY_USER_ACTION = "com.androsz.electricsleepbeta.alarmclock.ALARM_DISMISSED_BY_USER";

	// A public action sent by AlarmKlaxon when the alarm has stopped sounding
	// for any reason (e.g. because it has been dismissed from
	// AlarmAlertFullScreen,
	// or killed due to an incoming phone call, etc).
	public static final String ALARM_DONE_ACTION = "com.androsz.electricsleepbeta.alarmclock.ALARM_DONE";

	// This string is used to identify the alarm id passed to SetAlarm from the
	// list of alarms.
	public static final String ALARM_ID = "alarm_id";

	// This string is used when passing an Alarm object through an intent.
	public static final String ALARM_INTENT_EXTRA = "intent.extra.alarm";

	// This is a private action used by the AlarmKlaxon to update the UI to
	// show the alarm has been killed.
	public static final String ALARM_KILLED = "com.androsz.electricsleepbeta.alarmclock.alarm_killed";

	// Extra in the ALARM_KILLED intent to indicate to the user how long the
	// alarm played before being killed.
	public static final String ALARM_KILLED_TIMEOUT = "com.androsz.electricsleepbeta.alarmclock.alarm_killed_timeout";

	// This extra is the raw Alarm object data. It is used in the
	// AlarmManagerService to avoid a ClassNotFoundException when filling in
	// the Intent extras.
	public static final String ALARM_RAW_DATA = "intent.extra.alarm_raw";

	// AlarmAlertFullScreen listens for this broadcast intent, so that other
	// applications
	// can snooze the alarm (after ALARM_ALERT_ACTION and before
	// ALARM_DONE_ACTION).
	public static final String ALARM_SNOOZE_ACTION = "com.androsz.electricsleepbeta.alarmclock.ALARM_SNOOZE";

	// A public action that is broadcasted when the user cancels snoozing
	// Other applications may listen for this.
	public static final String ALARM_SNOOZE_CANCELED_BY_USER_ACTION = "com.androsz.electricsleepbeta.alarmclock.ALARM_SNOOZE_CANCELED_BY_USER";

	// This intent is sent from the notification when the user cancels the
	// snooze alert.
	public static final String CANCEL_SNOOZE = "com.androsz.electricsleepbeta.alarmclock.cancel_snooze";

	private final static String DM12 = "E h:mm aa";
	private final static String DM24 = "E k:mm";

	private final static String M12 = "h:mm aa";
	// Shared with DigitalClock
	final static String M24 = "kk:mm";

	public final static String PREF_SNOOZE_ID = "snooze_id";
	final static String PREF_SNOOZE_TIME = "snooze_time";

	/**
	 * Creates a new Alarm and fills in the given alarm's id.
	 */
	public static long addAlarm(final Context context, final Alarm alarm) {
		final ContentValues values = createContentValues(alarm);
		final Uri uri = context.getContentResolver().insert(
				Alarm.Columns.CONTENT_URI, values);
		alarm.id = (int) ContentUris.parseId(uri);

		final long timeInMillis = calculateAlarm(alarm);
		if (alarm.enabled) {
			clearSnoozeIfNeeded(context, timeInMillis);
		}
		setNextAlert(context);
		return timeInMillis;
	}

	private static long calculateAlarm(final Alarm alarm) {
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek,
				System.currentTimeMillis()).getTimeInMillis();
	}

	static Calendar calculateAlarm(final int hour, final int minute,
			final Alarm.DaysOfWeek daysOfWeek) {
		return calculateAlarm(hour, minute, daysOfWeek,
				System.currentTimeMillis());
	}

	/**
	 * Given an alarm in hours and minutes, return a time suitable for setting
	 * in AlarmManager.
	 */
	static Calendar calculateAlarm(final int hour, final int minute,
			final Alarm.DaysOfWeek daysOfWeek, final long timeToStartAt) {

		// start with now
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timeToStartAt);

		final int nowHour = c.get(Calendar.HOUR_OF_DAY);
		final int nowMinute = c.get(Calendar.MINUTE);

		// if alarm is behind current time, advance one day
		if (hour < nowHour || hour == nowHour && minute <= nowMinute) {
			c.add(Calendar.DAY_OF_YEAR, 1);
		}
		c.set(Calendar.HOUR_OF_DAY, hour);
		c.set(Calendar.MINUTE, minute);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);

		final int addDays = daysOfWeek.getNextAlarm(c);
		if (addDays > 0) {
			c.add(Calendar.DAY_OF_WEEK, addDays);
		}
		return c;
	}

	private static long calculateAlarmIgnoringNext(final Alarm alarm) {
		return calculateAlarm(alarm.hour, alarm.minutes, alarm.daysOfWeek,
				alarm.time + 1).getTimeInMillis();
	}

	/**
	 * @param context
	 * @return
	 */
	public static Alarm calculateNextAlert(final Context context) {
		Alarm alarm = null;
		long minTime = Long.MAX_VALUE;
		final long now = System.currentTimeMillis();
		final Cursor cursor = getFilteredAlarmsCursor(context
				.getContentResolver());

		if (cursor != null) {
			if (cursor.moveToFirst()) {
				do {
					final Alarm a = new Alarm(cursor);
					// repeating alarm... calculate the true time
					if (a.daysOfWeek.isRepeatSet() || a.time == 0) {
						a.time = calculateAlarm(a);
					} else if (a.time < now) {
						// Expired alarm, disable it and move along.
						enableAlarmInternal(context, a, false);
						continue;
					}
					if (a.time < minTime) {
						if (a.time == a.timeToIgnore) {
							a.time = calculateAlarmIgnoringNext(a);
						}
						if (a.time < minTime) {
							alarm = a;
							minTime = a.time;
						}
					}
				} while (cursor.moveToNext());
			}
			cursor.close();
		}
		return alarm;
	}

	private static void clearSnoozeIfNeeded(final Context context,
			final long alarmTime) {
		// If this alarm fires before the next snooze, clear the snooze to
		// enable this alarm.
		final SharedPreferences prefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES, 0);
		final long snoozeTime = prefs.getLong(PREF_SNOOZE_TIME, 0);
		if (alarmTime < snoozeTime) {
			clearSnoozePreference(context, prefs);
		}
	}

	// Helper to remove the snooze preference. Do not use clear because that
	// will erase the clock preferences. Also clear the snooze notification in
	// the window shade.
	private static void clearSnoozePreference(final Context context,
			final SharedPreferences prefs) {
		final int alarmId = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (alarmId != -1) {
			final NotificationManager nm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			nm.cancel(alarmId);
		}

		final SharedPreferences.Editor ed = prefs.edit();
		ed.remove(PREF_SNOOZE_ID);
		ed.remove(PREF_SNOOZE_TIME);
		ed.commit();
	}

	private static ContentValues createContentValues(final Alarm alarm) {
		final ContentValues values = new ContentValues(8);
		if (!alarm.daysOfWeek.isRepeatSet()) {
			calculateAlarm(alarm);
		}

		values.put(Alarm.Columns.ENABLED, alarm.enabled ? 1 : 0);
		values.put(Alarm.Columns.HOUR, alarm.hour);
		values.put(Alarm.Columns.MINUTES, alarm.minutes);
		values.put(Alarm.Columns.ALARM_TIME, alarm.time);
		values.put(Alarm.Columns.DAYS_OF_WEEK, alarm.daysOfWeek.getCoded());
		values.put(Alarm.Columns.VIBRATE, alarm.vibrate);
		values.put(Alarm.Columns.MESSAGE, alarm.label);

		// A null alert Uri indicates a silent alarm.
		values.put(
				Alarm.Columns.ALERT,
				alarm.alert == null ? ALARM_ALERT_SILENT : alarm.alert
						.toString());

		return values;
	}

	/**
	 * Removes an existing Alarm. If this alarm is snoozing, disables snooze.
	 * Sets next alert.
	 */
	public static void deleteAlarm(final Context context, final int alarmId) {

		final ContentResolver contentResolver = context.getContentResolver();
		/* If alarm is snoozing, lose it */
		disableSnoozeAlert(context, alarmId);

		final Uri uri = ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI,
				alarmId);
		contentResolver.delete(uri, "", null);

		setNextAlert(context);
	}

	/**
	 * Disables alert in AlarmManger and StatusBar.
	 * 
	 * @param id
	 *            Alarm ID.
	 */
	static void disableAlert(final Context context) {
		final AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		final PendingIntent sender = PendingIntent.getBroadcast(context, 0,
				new Intent(ALARM_ALERT_ACTION),
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(sender);
		setStatusBarIcon(context, false);
		saveNextAlarm(context, "");
	}

	/**
	 * Disables non-repeating alarms that have passed. Called at boot.
	 */
	public static void disableExpiredAlarms(final Context context) {
		final Cursor cur = getFilteredAlarmsCursor(context.getContentResolver());
		final long now = System.currentTimeMillis();

		if (cur.moveToFirst()) {
			do {
				final Alarm alarm = new Alarm(cur);
				// A time of 0 means this alarm repeats. If the time is
				// non-zero, check if the time is before now.
				if (alarm.time != 0 && !alarm.daysOfWeek.isRepeatSet() && alarm.time < now) {
					if (Log.LOGV) {
						Log.v("** DISABLE " + alarm.id + " now " + now
								+ " set " + alarm.time);
					}
					enableAlarmInternal(context, alarm, false);
				}
			} while (cur.moveToNext());
		}
		cur.close();
	}

	/**
	 * Disable the snooze alert if the given id matches the snooze id.
	 */
	static void disableSnoozeAlert(final Context context, final int id) {
		final SharedPreferences prefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES, 0);
		final int snoozeId = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (snoozeId == -1) {
			// No snooze set, do nothing.
			return;
		} else if (snoozeId == id) {
			// This is the same id so clear the shared prefs.
			clearSnoozePreference(context, prefs);
		}
	}

	/**
	 * A convenience method to enable or disable an alarm.
	 * 
	 * @param id
	 *            corresponds to the _id column
	 * @param enabled
	 *            corresponds to the ENABLED column
	 */

	public static void enableAlarm(final Context context, final int id,
			final boolean enabled) {
		enableAlarmInternal(context, id, enabled);
		setNextAlert(context);
	}

	private static void enableAlarmInternal(final Context context,
			final Alarm alarm, final boolean enabled) {
		if (alarm == null) {
			return;
		}
		final ContentResolver resolver = context.getContentResolver();

		final ContentValues values = new ContentValues(2);
		values.put(Alarm.Columns.ENABLED, enabled ? 1 : 0);

		// If we are enabling the alarm, calculate alarm time since the time
		// value in Alarm may be old.
		if (enabled) {
			long time = 0;
			if (!alarm.daysOfWeek.isRepeatSet()) {
				time = calculateAlarm(alarm);
			}
			values.put(Alarm.Columns.ALARM_TIME, time);
			// reset the time to ignore
			values.put(Alarm.Columns.TIME_TO_IGNORE, 0);
		} else {
			// Clear the snooze if the id matches.
			disableSnoozeAlert(context, alarm.id);
		}

		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);
	}

	private static void enableAlarmInternal(final Context context,
			final int id, final boolean enabled) {
		enableAlarmInternal(context,
				getAlarm(context.getContentResolver(), id), enabled);
	}

	/**
	 * Sets alert in AlarmManger and StatusBar. This is what will actually
	 * launch the alert when the alarm triggers.
	 * 
	 * @param alarm
	 *            Alarm.
	 * @param atTimeInMillis
	 *            milliseconds since epoch
	 */
	public static void enableAlert(final Context context, final Alarm alarm,
			final long atTimeInMillis) {
		final AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);

		if (Log.LOGV) {
			Log.v("** setAlert id " + alarm.id + " atTime " + atTimeInMillis);
		}

		final Intent intent = new Intent(ALARM_ALERT_ACTION);

		// XXX: This is a slight hack to avoid an exception in the remote
		// AlarmManagerService process. The AlarmManager adds extra data to
		// this Intent which causes it to inflate. Since the remote process
		// does not know about the Alarm class, it throws a
		// ClassNotFoundException.
		//
		// To avoid this, we marshall the data ourselves and then parcel a plain
		// byte[] array. The AlarmReceiver class knows to build the Alarm
		// object from the byte[] array.
		final Parcel out = Parcel.obtain();
		alarm.writeToParcel(out, 0);
		out.setDataPosition(0);
		intent.putExtra(ALARM_RAW_DATA, out.marshall());

		final PendingIntent sender = PendingIntent.getBroadcast(context, 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);

		am.set(AlarmManager.RTC_WAKEUP, atTimeInMillis, sender);

		setStatusBarIcon(context, true);

		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(atTimeInMillis);
		final String timeString = formatDayAndTime(context, c);
		saveNextAlarm(context, timeString);
	}

	/**
	 * If there is a snooze set, enable it in AlarmManager
	 * 
	 * @return true if snooze is set
	 */
	private static boolean enableSnoozeAlert(final Context context) {
		final SharedPreferences prefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES, 0);

		final int id = prefs.getInt(PREF_SNOOZE_ID, -1);
		if (id == -1) {
			return false;
		}
		final long time = prefs.getLong(PREF_SNOOZE_TIME, -1);

		// Get the alarm from the db.
		final Alarm alarm = getAlarm(context.getContentResolver(), id);
		if (alarm == null) {
			return false;
		}
		// The time in the database is either 0 (repeating) or a specific time
		// for a non-repeating alarm. Update this value so the AlarmReceiver
		// has the right time to compare.
		alarm.time = time;

		enableAlert(context, alarm, time);
		return true;
	}

	/**
	 * Shows day and time -- used for lock screen
	 */
	private static String formatDayAndTime(final Context context,
			final Calendar c) {
		final String format = get24HourMode(context) ? DM24 : DM12;
		return c == null ? "" : (String) DateFormat.format(format, c);
	}

	/* used by AlarmAlert */
	static String formatTime(final Context context, final Calendar c) {
		final String format = get24HourMode(context) ? M24 : M12;
		return c == null ? "" : (String) DateFormat.format(format, c);
	}

	static String formatTime(final Context context, final int hour,
			final int minute, final Alarm.DaysOfWeek daysOfWeek) {
		final Calendar c = calculateAlarm(hour, minute, daysOfWeek);
		return formatTime(context, c);
	};

	/**
	 * @return true if clock is set to 24-hour mode
	 */
	static boolean get24HourMode(final Context context) {
		return android.text.format.DateFormat.is24HourFormat(context);
	}

	/**
	 * Return an Alarm object representing the alarm id in the database. Returns
	 * null if no alarm exists.
	 */
	public static Alarm getAlarm(final ContentResolver contentResolver,
			final int alarmId) {
		final Cursor cursor = contentResolver.query(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarmId),
				Alarm.Columns.ALARM_QUERY_COLUMNS, null, null, null);
		Alarm alarm = null;
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				alarm = new Alarm(cursor);
			}
			cursor.close();
		}
		return alarm;
	}

	/**
	 * Queries all alarms
	 * 
	 * @return cursor over all alarms
	 */
	public static Cursor getAlarmsCursor(final ContentResolver contentResolver) {
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, null, null,
				Alarm.Columns.DEFAULT_SORT_ORDER);
	}

	// Private method to get a more limited set of alarms from the database.
	private static Cursor getFilteredAlarmsCursor(
			final ContentResolver contentResolver) {
		return contentResolver.query(Alarm.Columns.CONTENT_URI,
				Alarm.Columns.ALARM_QUERY_COLUMNS, Alarm.Columns.WHERE_ENABLED,
				null, null);
	}

	/**
	 * Save time of the next alarm, as a formatted string, into the system
	 * settings so those who care can make use of it.
	 */
	static void saveNextAlarm(final Context context, final String timeString) {
		Settings.System.putString(context.getContentResolver(),
				Settings.System.NEXT_ALARM_FORMATTED, timeString);
	}

	static void saveSnoozeAlert(final Context context, final int id,
			final long time) {
		final SharedPreferences prefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES, 0);
		if (id == -1) {
			clearSnoozePreference(context, prefs);
		} else {
			final SharedPreferences.Editor ed = prefs.edit();
			ed.putInt(PREF_SNOOZE_ID, id);
			ed.putLong(PREF_SNOOZE_TIME, time);
			ed.commit();
		}
		// Set the next alert after updating the snooze.
		setNextAlert(context);
	}

	/**
	 * A convenience method to set an alarm in the Alarms content provider.
	 * 
	 * @return Time when the alarm will fire.
	 */
	public static long setAlarm(final Context context, final Alarm alarm) {
		final ContentValues values = createContentValues(alarm);
		final ContentResolver resolver = context.getContentResolver();
		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);

		final long timeInMillis = calculateAlarm(alarm);

		if (alarm.enabled) {
			// Disable the snooze if we just changed the snoozed alarm. This
			// only does work if the snoozed alarm is the same as the given
			// alarm.
			// TODO: disableSnoozeAlert should have a better name.
			disableSnoozeAlert(context, alarm.id);

			// Disable the snooze if this alarm fires before the snoozed alarm.
			// This works on every alarm since the user most likely intends to
			// have the modified alarm fire next.
			clearSnoozeIfNeeded(context, timeInMillis);
		}

		setNextAlert(context);

		return timeInMillis;
	}

	/**
	 * Called at system startup, on time/timezone change, and whenever the user
	 * changes alarm settings. Activates snooze if set, otherwise loads all
	 * alarms, activates next alert.
	 */
	public static void setNextAlert(final Context context) {
		if (!enableSnoozeAlert(context)) {
			final Alarm alarm = calculateNextAlert(context);
			if (alarm != null) {
				enableAlert(context, alarm, alarm.time);
			} else {
				disableAlert(context);
			}
		}
	}

	/**
	 * Tells the StatusBar whether the alarm is enabled or disabled
	 */
	private static void setStatusBarIcon(final Context context,
			final boolean enabled) {
		final Intent alarmChanged = new Intent(
				"android.intent.action.ALARM_CHANGED");
		alarmChanged.putExtra("alarmSet", enabled);
		context.sendBroadcast(alarmChanged);
	}

	public static void setTimeToIgnore(final Context context,
			final Alarm alarm, final long timeToIgnore) {
		final ContentValues values = createContentValues(alarm);
		values.put(Alarm.Columns.TIME_TO_IGNORE, timeToIgnore);
		final ContentResolver resolver = context.getContentResolver();
		resolver.update(
				ContentUris.withAppendedId(Alarm.Columns.CONTENT_URI, alarm.id),
				values, null, null);
	}
}
