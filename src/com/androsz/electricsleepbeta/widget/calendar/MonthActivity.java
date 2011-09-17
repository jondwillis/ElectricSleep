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

//*import static android.provider.Calendar.EVENT_BEGIN_TIME;
//*import dalvik.system.VMRuntime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.HostActivity;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.db.SleepSessions;
import com.androsz.electricsleepbeta.util.PointD;

public class MonthActivity extends HostActivity implements
		ViewSwitcher.ViewFactory, Navigator, LoaderManager.LoaderCallbacks<Cursor> {
	private static final int DAY_OF_WEEK_KINDS[] = { Calendar.SUNDAY,
			Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
			Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };
	private static final int DAY_OF_WEEK_LABEL_IDS[] = { R.id.day0, R.id.day1,
			R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 };

	private Animation mInAnimationFuture;
	private Animation mInAnimationPast;

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

	private Animation mOutAnimationFuture;

	private Animation mOutAnimationPast;
	private int mStartDay;

	private ViewSwitcher mSwitcher;

	private Time mTime;

	void eventsChanged() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				final MonthView view = (MonthView) mSwitcher.getCurrentView();
				view.forceReloadEvents();
			}
		});
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
		next.forceReloadEvents();
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
		view.forceReloadEvents();
	}

	/* ViewSwitcher.ViewFactory interface methods */
	@Override
	public View makeView() {
		final MonthView mv = null;
		mv.setLayoutParams(new ViewSwitcher.LayoutParams(
				android.view.ViewGroup.LayoutParams.MATCH_PARENT,
				android.view.ViewGroup.LayoutParams.MATCH_PARENT));
		mv.setSelectedTime(mTime);
		return mv;
	}

	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		sessionsObserver = new SessionsContentObserver();
		getContentResolver().registerContentObserver(
				SleepSessions.MainTable.CONTENT_URI, true, sessionsObserver);
		getSupportLoaderManager().initLoader(0, null, this);

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

		// mEventLoader = new EventLoader(this);

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
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(sessionsObserver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_multiple_history, menu);
		return super.onCreateOptionsMenu(menu);
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
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_delete_all:
			// TODO
			break;
		case R.id.menu_item_export_all:
			// TODO
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private SessionsContentObserver sessionsObserver;

	private class SessionsContentObserver extends ContentObserver {

		public SessionsContentObserver() {
			super(new Handler());
		}

		public void onChange(boolean selfChange) {

			getSupportLoaderManager().getLoader(0).forceLoad();
			super.onChange(selfChange);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(mIntentReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter();

		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		registerReceiver(mIntentReceiver, filter);

	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, SleepSessions.MainTable.CONTENT_URI,
				SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, null, null,
				null);
	}

	// to avoid NPE's, initialize
	LinkedHashMap<Long, SleepSession> mSessions = new LinkedHashMap<Long, SleepSession>(
			0);

	public LinkedHashMap<Long, SleepSession> getSessionsInInterval(
			long startMillis, int days) {
		synchronized (mSessions) {
			LinkedHashMap<Long, SleepSession> sessions = new LinkedHashMap<Long, SleepSession>(
					20);
			final Time local = new Time();

			local.set(startMillis);

			// expand start and days to include days shown from previous month
			// and next month. can be slightly wasteful.
			// start -= 1000 * 60 * 60 * 24 * 7; // 7 days
			// days += 7;

			Time.getJulianDay(startMillis, local.gmtoff);
			local.monthDay += days;
			long endMillis = local.normalize(true);
			// endMillis+=startMillis;

			for (SleepSession s : mSessions.values()) {
				final long startTime = s.getStartTime();
				if (startTime >= startMillis && startTime <= endMillis) {
					final List<PointD> justFirstAndLast = new ArrayList<PointD>();
					justFirstAndLast.add(s.chartData.get(0));
					justFirstAndLast
							.add(s.chartData.get(s.chartData.size() - 1));
					s.chartData = justFirstAndLast; // remove reference to the
													// list, helps lessen memory
													// usage
					sessions.put(startTime, s);
				}
			}

			//TODO
			//SleepSession.computePositions(sessions.values());

			return sessions;
		}
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				mSessions = SleepSessions.getSessionsFromCursor(
						MonthActivity.this, data);
				eventsChanged();
				// TODO: notify MonthViews that mEvents have change in a
				// ..better way?
			}
		}).start();
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// TODO Auto-generated method stub

	}
}
