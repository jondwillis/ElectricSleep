/*
 * Copyright (C) 2009 The Android Open Source Project
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

import java.text.DateFormatSymbols;
import java.util.Calendar;

import android.content.Context;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import com.androsz.electricsleepbeta.R;

public final class Alarm implements Parcelable {

	// ////////////////////////////
	// Column definitions
	// ////////////////////////////
	public static class Columns implements BaseColumns {
		public static final int ALARM_ALERT_INDEX = 8;

		public static final int ALARM_DAYS_OF_WEEK_INDEX = 3;

		public static final int ALARM_ENABLED_INDEX = 5;

		public static final int ALARM_HOUR_INDEX = 1;

		/**
		 * These save calls to cursor.getColumnIndexOrThrow() THEY MUST BE KEPT
		 * IN SYNC WITH ABOVE QUERY COLUMNS
		 */
		public static final int ALARM_ID_INDEX = 0;

		public static final int ALARM_MESSAGE_INDEX = 7;

		public static final int ALARM_MINUTES_INDEX = 2;

		/**
		 * Alarm time in UTC milliseconds from the epoch.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String ALARM_TIME = "alarmtime";

		public static final int ALARM_TIME_INDEX = 4;

		public static final int ALARM_TIME_TO_IGNORE_INDEX = 9;

		public static final int ALARM_VIBRATE_INDEX = 6;

		/**
		 * Audio alert to play when alarm triggers
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String ALERT = "alert";

		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://com.androsz.electricsleepbeta.alarmclock/alarm");
		/**
		 * Days of week coded as integer
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String DAYS_OF_WEEK = "daysofweek";
		/**
		 * True if alarm is active
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String ENABLED = "enabled";
		/**
		 * Hour in 24-hour localtime 0 - 23.
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String HOUR = "hour";
		/**
		 * Message to show when alarm triggers Note: not currently used
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String MESSAGE = "message";
		/**
		 * Minutes in localtime 0 - 59
		 * <P>
		 * Type: INTEGER
		 * </P>
		 */
		public static final String MINUTES = "minutes";
		/**
		 * Time to ignore - used for ignoring alarms that would normally happen
		 * after smart alarm
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String TIME_TO_IGNORE = "timeToIgnore";
		/**
		 * True if alarm should vibrate
		 * <P>
		 * Type: BOOLEAN
		 * </P>
		 */
		public static final String VIBRATE = "vibrate";
		// Used when filtering enabled alarms.
		public static final String WHERE_ENABLED = ENABLED + "=1";
		
		static final String[] ALARM_QUERY_COLUMNS = { _ID, HOUR, MINUTES,
			DAYS_OF_WEEK, ALARM_TIME, ENABLED, VIBRATE, MESSAGE, ALERT,
			TIME_TO_IGNORE };
		/**
		 * The default sort order for this table
		 */
		public static final String DEFAULT_SORT_ORDER = HOUR + ", " + MINUTES 
				+ " ASC";


	}

	/*
	 * Days of week code as a single int. 0x00: no day 0x01: Monday 0x02:
	 * Tuesday 0x04: Wednesday 0x08: Thursday 0x10: Friday 0x20: Saturday 0x40:
	 * Sunday
	 */
	static final class DaysOfWeek {

		private static int[] DAY_MAP = new int[] { Calendar.MONDAY,
				Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
				Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY, };

		// Bitmask of all repeating days
		private int mDays;

		DaysOfWeek(final int days) {
			mDays = days;
		}

		// Returns days of week encoded in an array of booleans.
		public boolean[] getBooleanArray() {
			final boolean[] ret = new boolean[7];
			for (int i = 0; i < 7; i++) {
				ret[i] = isSet(i);
			}
			return ret;
		}

		public int getCoded() {
			return mDays;
		}

		/**
		 * returns number of days from today until next alarm
		 * 
		 * @param c
		 *            must be set to today
		 */
		public int getNextAlarm(final Calendar c) {
			if (mDays == 0) {
				return -1;
			}

			final int today = (c.get(Calendar.DAY_OF_WEEK) + 5) % 7;

			int day = 0;
			int dayCount = 0;
			for (; dayCount < 7; dayCount++) {
				day = (today + dayCount) % 7;
				if (isSet(day)) {
					break;
				}
			}
			return dayCount;
		}

		public boolean isRepeatSet() {
			return mDays != 0;
		}

		private boolean isSet(final int day) {
			return (mDays & 1 << day) > 0;
		}

		public void set(final DaysOfWeek dow) {
			mDays = dow.mDays;
		}

		public void set(final int day, final boolean set) {
			if (set) {
				mDays |= 1 << day;
			} else {
				mDays &= ~(1 << day);
			}
		}

		public String toString(final Context context, final boolean showNever) {
			final StringBuilder ret = new StringBuilder();

			// no days
			if (mDays == 0) {
				return showNever ? context.getText(R.string.never).toString()
						: "";
			}

			// every day
			if (mDays == 0x7f) {
				return context.getText(R.string.every_day).toString();
			}

			// count selected days
			int dayCount = 0, days = mDays;
			while (days > 0) {
				if ((days & 1) == 1) {
					dayCount++;
				}
				days >>= 1;
			}

			// short or long form?
			final DateFormatSymbols dfs = new DateFormatSymbols();
			final String[] dayList = dayCount > 1 ? dfs.getShortWeekdays()
					: dfs.getWeekdays();

			// selected days
			for (int i = 0; i < 7; i++) {
				if ((mDays & 1 << i) != 0) {
					ret.append(dayList[DAY_MAP[i]]);
					dayCount -= 1;
					if (dayCount > 0) {
						ret.append(context.getText(R.string.day_concat));
					}
				}
			}
			return ret.toString();
		}
	}

	// ////////////////////////////
	// Parcelable apis
	// ////////////////////////////
	public static final Parcelable.Creator<Alarm> CREATOR = new Parcelable.Creator<Alarm>() {
		@Override
		public Alarm createFromParcel(final Parcel p) {
			return new Alarm(p);
		}

		@Override
		public Alarm[] newArray(final int size) {
			return new Alarm[size];
		}
	};

	// ////////////////////////////
	// end Parcelable apis
	// ////////////////////////////

	public Uri alert;

	// ////////////////////////////
	// End column definitions
	// ////////////////////////////

	public DaysOfWeek daysOfWeek;
	public boolean enabled;
	public int hour;
	// Public fields
	public int id;
	public String label;
	public int minutes;
	public boolean silent;
	public long time;
	public long timeToIgnore;
	public boolean vibrate;

	// Creates a default alarm at the current time.
	public Alarm() {
		id = -1;
		final Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		hour = c.get(Calendar.HOUR_OF_DAY);
		minutes = c.get(Calendar.MINUTE);
		vibrate = true;
		daysOfWeek = new DaysOfWeek(0);
		alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		timeToIgnore = 0;

	}

	public Alarm(final Cursor c) {
		id = c.getInt(Columns.ALARM_ID_INDEX);
		enabled = c.getInt(Columns.ALARM_ENABLED_INDEX) == 1;
		hour = c.getInt(Columns.ALARM_HOUR_INDEX);
		minutes = c.getInt(Columns.ALARM_MINUTES_INDEX);
		daysOfWeek = new DaysOfWeek(c.getInt(Columns.ALARM_DAYS_OF_WEEK_INDEX));
		time = c.getLong(Columns.ALARM_TIME_INDEX);
		vibrate = c.getInt(Columns.ALARM_VIBRATE_INDEX) == 1;
		label = c.getString(Columns.ALARM_MESSAGE_INDEX);
		timeToIgnore = c.getLong(Columns.ALARM_TIME_TO_IGNORE_INDEX);
		final String alertString = c.getString(Columns.ALARM_ALERT_INDEX);
		if (Alarms.ALARM_ALERT_SILENT.equals(alertString)) {
			if (Log.LOGV) {
				Log.v("Alarm is marked as silent");
			}
			silent = true;
		} else {
			if (alertString != null && alertString.length() != 0) {
				alert = Uri.parse(alertString);
			}

			// If the database alert is null or it failed to parse, use the
			// default alert.
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_ALARM);
			}
		}
	}

	public Alarm(final Parcel p) {
		id = p.readInt();
		enabled = p.readInt() == 1;
		hour = p.readInt();
		minutes = p.readInt();
		daysOfWeek = new DaysOfWeek(p.readInt());
		time = p.readLong();
		vibrate = p.readInt() == 1;
		label = p.readString();
		alert = (Uri) p.readParcelable(null);
		silent = p.readInt() == 1;
		timeToIgnore = p.readLong();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public String getLabelOrDefault(final Context context) {
		if (label == null || label.length() == 0) {
			return context.getString(R.string.default_label);
		}
		return label;
	}

	@Override
	public void writeToParcel(final Parcel p, final int flags) {
		p.writeInt(id);
		p.writeInt(enabled ? 1 : 0);
		p.writeInt(hour);
		p.writeInt(minutes);
		p.writeInt(daysOfWeek.getCoded());
		p.writeLong(time);
		p.writeInt(vibrate ? 1 : 0);
		p.writeString(label);
		p.writeParcelable(alert, flags);
		p.writeInt(silent ? 1 : 0);
		p.writeLong(timeToIgnore);
	}
}
