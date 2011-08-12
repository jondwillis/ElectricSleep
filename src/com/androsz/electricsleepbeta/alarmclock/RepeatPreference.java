/*
 * Copyright (C) 2008 The Android Open Source Project
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

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class RepeatPreference extends ListPreference {

	// Initial value that can be set with the values saved in the database.
	private final Alarm.DaysOfWeek mDaysOfWeek = new Alarm.DaysOfWeek(0);
	// New value that will be set if a positive result comes back from the
	// dialog.
	private final Alarm.DaysOfWeek mNewDaysOfWeek = new Alarm.DaysOfWeek(0);

	public RepeatPreference(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		final String[] weekdays = new DateFormatSymbols().getWeekdays();
		final String[] values = new String[] { weekdays[Calendar.MONDAY],
				weekdays[Calendar.TUESDAY], weekdays[Calendar.WEDNESDAY],
				weekdays[Calendar.THURSDAY], weekdays[Calendar.FRIDAY],
				weekdays[Calendar.SATURDAY], weekdays[Calendar.SUNDAY], };
		setEntries(values);
		setEntryValues(values);
	}

	public Alarm.DaysOfWeek getDaysOfWeek() {
		return mDaysOfWeek;
	}

	@Override
	protected void onDialogClosed(final boolean positiveResult) {
		if (positiveResult) {
			mDaysOfWeek.set(mNewDaysOfWeek);
			setSummary(mDaysOfWeek.toString(getContext(), true));
			callChangeListener(mDaysOfWeek);
		}
	}

	@Override
	protected void onPrepareDialogBuilder(final Builder builder) {
		final CharSequence[] entries = getEntries();
		getEntryValues();

		builder.setMultiChoiceItems(entries, mDaysOfWeek.getBooleanArray(),
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which, final boolean isChecked) {
						mNewDaysOfWeek.set(which, isChecked);
					}
				});
	}

	public void setDaysOfWeek(final Alarm.DaysOfWeek dow) {
		mDaysOfWeek.set(dow);
		mNewDaysOfWeek.set(dow);
		setSummary(dow.toString(getContext(), true));
	}
}
