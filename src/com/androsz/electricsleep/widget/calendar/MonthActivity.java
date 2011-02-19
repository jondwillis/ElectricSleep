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

package com.androsz.electricsleep.widget.calendar;

//*import static android.provider.Calendar.EVENT_BEGIN_TIME;
//*import dalvik.system.VMRuntime;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.androsz.electricsleepdonate.R;
import com.androsz.electricsleep.app.CustomTitlebarActivity;
import com.androsz.electricsleep.db.SleepContentProvider;

public class MonthActivity extends CustomTitlebarActivity implements
		ViewSwitcher.ViewFactory, Navigator, AnimationListener {
	private Animation mInAnimationPast;
	private Animation mInAnimationFuture;
	private Animation mOutAnimationPast;
	private Animation mOutAnimationFuture;
	private ViewSwitcher mSwitcher;
	private Time mTime;

	private ContentResolver mContentResolver;
	EventLoader mEventLoader;
	private int mStartDay;

	private static final int DAY_OF_WEEK_LABEL_IDS[] = { R.id.day0, R.id.day1,
			R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 };
	private static final int DAY_OF_WEEK_KINDS[] = { Calendar.SUNDAY,
			Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
			Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };

	/**
	 * Listens for intent broadcasts
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_CHANGED)
					|| action.equals(Intent.ACTION_DATE_CHANGED)
					|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				eventsChanged();
			}
		}
	};

	// Create an observer so that we can update the views whenever a
	// Calendar event changes.
	private final ContentObserver mObserver = new ContentObserver(new Handler()) {
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			eventsChanged();
		}
	};

	void eventsChanged() {
		final MonthView view = (MonthView) mSwitcher.getCurrentView();
		view.reloadEvents();
	}

	@Override
	public boolean getAllDay() {
		return false;
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.month_activity;
	}

	@Override
	public long getSelectedTime() {
		final MonthView mv = (MonthView) mSwitcher.getCurrentView();
		return mv.getSelectedTimeInMillis();
	}

	int getStartDay() {
		return mStartDay;
	}

	/* Navigator interface methods */
	@Override
	public void goTo(Time time, boolean animate) {
		setTitle(Utils.formatMonthYear(this, time));

		final MonthView current = (MonthView) mSwitcher.getCurrentView();
		current.dismissPopup();

		final Time currentTime = current.getTime();

		// Compute a month number that is monotonically increasing for any
		// two adjacent months.
		// This is faster than calling getSelectedTime() because we avoid
		// a call to Time#normalize().
		if (animate) {
			final int currentMonth = currentTime.month + currentTime.year * 12;
			final int nextMonth = time.month + time.year * 12;
			if (nextMonth < currentMonth) {
				mSwitcher.setInAnimation(mInAnimationPast);
				mSwitcher.setOutAnimation(mOutAnimationPast);
			} else {
				mSwitcher.setInAnimation(mInAnimationFuture);
				mSwitcher.setOutAnimation(mOutAnimationFuture);
			}
		}

		final MonthView next = (MonthView) mSwitcher.getNextView();
		next.setSelectionMode(current.getSelectionMode());
		next.setSelectedTime(time);
		next.reloadEvents();
		next.animationStarted();
		mSwitcher.showNext();
		next.requestFocus();
		mTime = time;
	}

	@Override
	public void goToToday() {
		final Time now = new Time();
		now.set(System.currentTimeMillis());
		now.minute = 0;
		now.second = 0;
		now.normalize(false);

		setTitle(Utils.formatMonthYear(this, now));
		mTime = now;

		final MonthView view = (MonthView) mSwitcher.getCurrentView();
		view.setSelectedTime(now);
		view.reloadEvents();
	}

	/* ViewSwitcher.ViewFactory interface methods */
	@Override
	public View makeView() {
		final MonthView mv = new MonthView(this, this);
		mv.setLayoutParams(new ViewSwitcher.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		mv.setSelectedTime(mTime);
		return mv;
	}

	// Notifies the MonthView when an animation has finished.
	@Override
	public void onAnimationEnd(Animation animation) {
		final MonthView monthView = (MonthView) mSwitcher.getCurrentView();
		monthView.animationFinished();
	}

	@Override
	public void onAnimationRepeat(Animation animation) {
	}

	@Override
	public void onAnimationStart(Animation animation) {
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		// Eliminate extra GCs during startup by setting the initial heap size
		// to 4MB.
		// TODO: We should restore the old heap size once the activity reaches
		// the idle state
		// VMRuntime.getRuntime().setMinimumHeapSize(INITIAL_HEAP_SIZE);

		mContentResolver = getContentResolver();

		final long time = Utils.timeFromIntentInMillis(getIntent());

		mTime = new Time();
		mTime.set(time);
		mTime.normalize(true);

		// Get first day of week based on locale and populate the day headers
		mStartDay = Calendar.getInstance().getFirstDayOfWeek();
		final int diff = mStartDay - Calendar.SUNDAY - 1;
		final int startDay = Utils.getFirstDayOfWeek();
		final int sundayColor = getResources().getColor(
				R.color.sunday_text_color);
		final int saturdayColor = getResources().getColor(
				R.color.saturday_text_color);

		for (int day = 0; day < 7; day++) {
			final String dayString = DateUtils.getDayOfWeekString(
					(DAY_OF_WEEK_KINDS[day] + diff) % 7 + 1,
					DateUtils.LENGTH_MEDIUM);
			final TextView label = (TextView) findViewById(DAY_OF_WEEK_LABEL_IDS[day]);
			label.setText(dayString);
			if (Utils.isSunday(day, startDay)) {
				label.setTextColor(sundayColor);
			} else if (Utils.isSaturday(day, startDay)) {
				label.setTextColor(saturdayColor);
			}
		}

		setTitle(Utils.formatMonthYear(this, mTime));

		mEventLoader = new EventLoader(this);

		mSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
		mSwitcher.setFactory(this);
		mSwitcher.getCurrentView().requestFocus();

		mInAnimationPast = AnimationUtils.loadAnimation(this,
				R.anim.slide_down_in);
		mOutAnimationPast = AnimationUtils.loadAnimation(this,
				R.anim.slide_down_out);
		mInAnimationFuture = AnimationUtils.loadAnimation(this,
				R.anim.slide_up_in);
		mOutAnimationFuture = AnimationUtils.loadAnimation(this,
				R.anim.slide_up_out);

		mInAnimationPast.setAnimationListener(this);
		mInAnimationFuture.setAnimationListener(this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		final long timeMillis = Utils.timeFromIntentInMillis(intent);
		if (timeMillis > 0) {
			final Time time = new Time();
			time.set(timeMillis);
			goTo(time, false);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			mEventLoader.stopBackgroundThread();
		}
		mContentResolver.unregisterContentObserver(mObserver);
		unregisterReceiver(mIntentReceiver);

		MonthView view = (MonthView) mSwitcher.getCurrentView();
		view.dismissPopup();
		view = (MonthView) mSwitcher.getNextView();
		view.dismissPopup();
		mEventLoader.stopBackgroundThread();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mEventLoader.startBackgroundThread();
		eventsChanged();

		// *SharedPreferences prefs =
		// CalendarPreferenceActivity.getSharedPreferences(this);
		// String str =
		// prefs.getString(CalendarPreferenceActivity.KEY_DETAILED_VIEW,
		// CalendarPreferenceActivity.DEFAULT_DETAILED_VIEW);
		// view1.setDetailedView(str);
		// view2.setDetailedView(str);

		// Register for Intent broadcasts
		final IntentFilter filter = new IntentFilter();

		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(mIntentReceiver, filter);

		mContentResolver.registerContentObserver(
				SleepContentProvider.CONTENT_URI, true, mObserver);
	}
}
