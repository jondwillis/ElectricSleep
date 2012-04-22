package com.androsz.electricsleepbeta.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.StaleDataException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.calendar.MonthView;
import com.androsz.electricsleepbeta.widget.calendar.Utils;
import com.myzeo.android.Log;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class HistoryMonthFragment extends AnalyticFragment implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = HistoryMonthFragment.class
			.getSimpleName();

	private final class IndicatorPageChangeListener implements
			OnPageChangeListener {
		private final TitlePageIndicator indicator;

		private IndicatorPageChangeListener(TitlePageIndicator indicator) {
			this.indicator = indicator;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {

			if (state == ViewPager.SCROLL_STATE_IDLE) {

				MonthView leftMonth = (MonthView) monthPager.getChildAt(0);
				MonthView centerMonth = (MonthView) monthPager.getChildAt(1);
				MonthView rightMonth = (MonthView) monthPager.getChildAt(2);

				final Time oldCenterTime = new Time(centerMonth.getTime());

				String[] newTitles = new String[3];

				int focusedPage = HistoryMonthFragment.this.focusedPage;
				if (focusedPage == 0) {

					final Time oldTopTime = new Time(leftMonth.getTime());

					final Time time = new Time(oldTopTime);
					time.month--;
					time.normalize(true);

					// TODO: load and switch shown events
					leftMonth.setTime(time);
					centerMonth.setTime(oldTopTime);
					rightMonth.setTime(oldCenterTime);
				} else if (focusedPage == 2) {

					final Time oldBottomTime = new Time(rightMonth.getTime());

					final Time time = new Time(oldBottomTime);
					time.month++;
					time.normalize(true);

					leftMonth.setTime(oldCenterTime);
					centerMonth.setTime(oldBottomTime);
					rightMonth.setTime(time);
				}

				Activity a = getActivity();
				newTitles[0] = Utils.formatMonthYear(a, leftMonth.getTime());
				newTitles[1] = Utils.formatMonthYear(a, centerMonth.getTime());
				newTitles[2] = Utils.formatMonthYear(a, rightMonth.getTime());

				monthAdapter.setTitles(newTitles);

				// always set to middle page to continue to be able to
				// scroll up/down
				indicator.setCurrentItem(1, false);
				eventsChanged(focusedPage);
			}
		}

		@Override
		public void onPageSelected(int position) {
			focusedPage = position;
		}
	}

	private class MonthPagerAdapter extends PagerAdapter implements
			TitleProvider {

		private String[] titles = new String[] { "", "", "" };

		public String[] getTitles() {
			return titles;
		}

		public void setTitles(String[] titles) {
			this.titles = titles.clone();
		}

		public MonthView addMonthViewAt(ViewPager container, int position,
				Time time) {
			final MonthView mv = new MonthView(HistoryMonthFragment.this);
			mv.setLayoutParams(new ViewSwitcher.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			mv.setSelectedTime(time);

			container.addView(mv, position);
			return mv;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			// simply reuse items...
			// ((ViewPager) container).removeViewAt(position);
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public String getTitle(int position) {
			String title = titles[position];
			return title;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			MonthView childAt = (MonthView) ((ViewPager) container)
					.getChildAt(position);
			if (childAt == null) {
				final Time time = new Time();
				time.setToNow();

				// set to first day in month. this prevents errors when the
				// current
				// month (TODAY) has more days than the neighbor month.
				time.set(1, time.month, time.year);
				time.month += (position - 1); // add the offset from the center
												// time
				time.normalize(true);

				MonthView mv = addMonthViewAt((ViewPager) container, position,
						time);

				titles[position] = Utils.formatMonthYear(getActivity(),
						mv.getTime());

				return mv;
			}
			return childAt;
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View container) {
		}

		boolean alreadyLoaded = false;

		@Override
		public void finishUpdate(View container) {
			if (!alreadyLoaded
					&& ((ViewPager) container).getChildCount() == getCount()) {
				((HostActivity) getActivity()).getSupportLoaderManager()
						.getLoader(0).forceLoad();
				alreadyLoaded = true;
			}
		}
	}

	private class SessionsContentObserver extends ContentObserver {

		public SessionsContentObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {

			((HostActivity) getActivity()).getSupportLoaderManager()
					.getLoader(0).forceLoad();
			super.onChange(selfChange);
		}
	}

	private static final int DAY_OF_WEEK_KINDS[] = { Calendar.SUNDAY,
			Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
			Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };
	private static final int DAY_OF_WEEK_LABEL_IDS[] = { R.id.day0, R.id.day1,
			R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 };
	private ViewPager monthPager;
	private MonthPagerAdapter monthAdapter;

	private int startDay;

	List<Long[]> mSessions = new ArrayList<Long[]>(0);

	private int focusedPage = 0;

	private SessionsContentObserver sessionsObserver;

	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (action.equals(Intent.ACTION_TIME_CHANGED)
					|| action.equals(Intent.ACTION_DATE_CHANGED)
					|| action.equals(Intent.ACTION_TIMEZONE_CHANGED)) {
				eventsChanged(-1);
			}
		}
	};

	void eventsChanged(final int whichPage) {
		try {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// if (whichPage == -1) {
					ViewPager vp = monthPager;
					for (int i = 0; i < vp.getChildCount(); i++) {
						final MonthView mv = (MonthView) monthPager
								.getChildAt(i);
						Time t = mv.getTime();
						mv.forceReloadEvents(mSessions);/*
														 * getSessionsInInterval(
														 * t. toMillis(true),
														 * 31));
														 */
					}
					// } else {
					// final MonthView mv = (MonthView)
					// monthPager.getChildAt(whichPage);
					// Time t = mv.getTime();
					// mv.forceReloadEvents(getSessionsInInterval(mv.getTime().toMillis(true),
					// 31));
					// }
				}
			});
		} catch (NullPointerException npe) {
			Log.d("getActivity() caused NPE in eventsChanged(...)");
		}
	}

	public ArrayList<Long[]> getSessionsInInterval(long startMillis, int days) {

		final ArrayList<Long[]> sessions = new ArrayList<Long[]>(20);
		final Time local = new Time();

		local.set(startMillis);

		// expand start and days to include days shown from previous month
		// and next month. can be slightly wasteful.
		// start -= 1000 * 60 * 60 * 24 * 7; // 7 days
		// days += 7;
		final int startJulianDay = Time.getJulianDay(startMillis, local.gmtoff);
		local.monthDay += days;
		local.normalize(true);
		final int endJulianDay = Time.getJulianDay(local.toMillis(true),
				local.gmtoff);
		Log.d(TAG, "Getting interval from: " + startJulianDay + " to: "
				+ endJulianDay);

		synchronized (mSessions) {
			for (final Long[] session : mSessions) {
				final long sessionStartJulianDay = session[2];
				if (sessionStartJulianDay >= startJulianDay
						&& sessionStartJulianDay < endJulianDay) {
					Log.d(TAG, "Adding session: " + session);
					sessions.add(session);
				}
			}

			// TODO ?
			// SleepSession.computePositions(sessions.values());

			return sessions;
		}
	}

	public int getStartDay() {
		return startDay;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		HostActivity a = (HostActivity) getActivity();
		sessionsObserver = new SessionsContentObserver();
		a.getContentResolver().registerContentObserver(
				SleepSession.CONTENT_URI, true, sessionsObserver);

		a.getSupportLoaderManager().initLoader(0, null,
				HistoryMonthFragment.this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getSherlockActivity()
				.setSupportProgressBarIndeterminateVisibility(true);
		View root = inflater.inflate(R.layout.fragment_history_month,
				container, false);

		// Get first day of week based on locale and populate the day headers
		startDay = Calendar.getInstance().getFirstDayOfWeek();
		final int diff = startDay - Calendar.SUNDAY - 1;
		final int startDay = Utils.getFirstDayOfWeek();

		for (int day = 0; day < 7; day++) {
			final String dayString = DateUtils.getDayOfWeekString(
					(DAY_OF_WEEK_KINDS[day] + diff) % 7 + 1,
					DateUtils.LENGTH_MEDIUM);
			final TextView label = (TextView) root
					.findViewById(DAY_OF_WEEK_LABEL_IDS[day]);
			label.setText(dayString);
		}

		monthAdapter = new MonthPagerAdapter();
		monthPager = (ViewPager) root.findViewById(R.id.monthpager);
		monthPager.setAdapter(monthAdapter);

		final TitlePageIndicator indicator = (TitlePageIndicator) root
				.findViewById(R.id.indicator);
		indicator.setFooterColor(getResources().getColor(R.color.primary1));
		indicator.setViewPager(monthPager, 1);
		indicator.setOnPageChangeListener(new IndicatorPageChangeListener(
				indicator));

		return root;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), SleepSession.CONTENT_URI, null,
				null, null, null);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater mi) {
		mi.inflate(R.menu.menu_history_calendar, menu);
		super.onCreateOptionsMenu(menu, mi);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		getActivity().getContentResolver().unregisterContentObserver(
				sessionsObserver);
		mSwitchToOtherHistoryTask.cancel(true);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		new Thread(new Runnable() {

			// @Override
			public void run() {
				// android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				try {
					mSessions = new ArrayList<Long[]>(0);
					mSessions = SleepSession.getStartEndTimestamps(data);
				} catch (IllegalArgumentException ex) {
					Log.d(TAG,
							"Failure to provide proper arguments when accessing session data.",
							ex);
				} catch (IllegalStateException ex) {
					Log.d(TAG, "Sleep sessions in illegal state.", ex);
				} catch (StaleDataException ex) {
					Log.d(TAG, "Sleep session data was stale.", ex);
				} finally {
					eventsChanged(-1);
				}
			}
		}).start();
		getSherlockActivity().setSupportProgressBarIndeterminateVisibility(
				false);
	}

	/**
	 * Switches to the List view and saves that the user should see it again the
	 * next time they view history.
	 */
	private final AsyncTask<Void, Void, Void> mSwitchToOtherHistoryTask = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {

			SherlockFragmentActivity activity = getSherlockActivity();
			final SharedPreferences userPrefs = activity.getSharedPreferences(
					SettingsActivity.PREFERENCES_ENVIRONMENT,
					Context.MODE_PRIVATE);

			userPrefs
					.edit()
					.putBoolean(
							SettingsActivity.PREFERENCES_KEY_HISTORY_VIEW_AS_LIST,
							true).commit();

			activity.getSupportFragmentManager().beginTransaction()
					.replace(android.R.id.content, new HistoryListFragment())
					.commit();

			return null;
		}
	};

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_list:
			try {
				mSwitchToOtherHistoryTask.execute();
			} catch (IllegalStateException ise) {
				Log.d(TAG, "the task is already running or has finished.");
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().unregisterReceiver(mIntentReceiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		final IntentFilter filter = new IntentFilter();

		filter.addAction(Intent.ACTION_TIME_CHANGED);
		filter.addAction(Intent.ACTION_DATE_CHANGED);
		filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
		getActivity().registerReceiver(mIntentReceiver, filter);

	}
}