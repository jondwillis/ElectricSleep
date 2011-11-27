package com.androsz.electricsleepbeta.app;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.StaleDataException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.ActionBar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.View;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSessions;
import com.androsz.electricsleepbeta.widget.calendar.MonthView;
import com.androsz.electricsleepbeta.widget.calendar.Utils;
import com.viewpagerindicator.TitlePageIndicator;
import com.viewpagerindicator.TitleProvider;

public class HistoryMonthActivity extends HostActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private final class IndicatorPageChangeListener implements OnPageChangeListener {
		private final TitlePageIndicator indicator;

		private IndicatorPageChangeListener(TitlePageIndicator indicator) {
			this.indicator = indicator;
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			lastPosition = position;
		}

		private int lastSettledPosition = 1;
		private int lastPosition = 1;

		@Override
		public void onPageScrollStateChanged(int state) {

			if (state == ViewPager.SCROLL_STATE_IDLE) {

				if (lastSettledPosition == lastPosition)
					return;

				MonthView leftMonth = (MonthView) monthPager.getChildAt(0);
				MonthView centerMonth = (MonthView) monthPager.getChildAt(1);
				MonthView rightMonth = (MonthView) monthPager.getChildAt(2);

				final Time oldCenterTime = new Time(centerMonth.getTime());

				String[] newTitles = new String[3];

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

				newTitles[0] = Utils
						.formatMonthYear(HistoryMonthActivity.this, leftMonth.getTime());
				newTitles[1] = Utils.formatMonthYear(HistoryMonthActivity.this,
						centerMonth.getTime());
				newTitles[2] = Utils.formatMonthYear(HistoryMonthActivity.this,
						rightMonth.getTime());

				monthAdapter.setTitles(newTitles);

				// always set to middle page to continue to be able to
				// scroll up/down
				indicator.setCurrentItem(1, false);
				eventsChanged();
			}
		}

		@Override
		public void onPageSelected(int position) {
			focusedPage = position;
		}
	}

	private class MonthPagerAdapter extends PagerAdapter implements TitleProvider {

		private String[] titles = new String[] { "", "", "" };

		public String[] getTitles() {
			return titles;
		}

		public void setTitles(String[] titles) {
			this.titles = titles.clone();
		}

		public MonthView addMonthViewAt(ViewPager container, int position, Time time) {
			final MonthView mv = new MonthView(HistoryMonthActivity.this);
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
		public void finishUpdate(View container) {
		}

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public String getTitle(int position) {
			String title = titles[position];
			return title;
		}

		@Override
		public Object instantiateItem(View container, int position) {
			MonthView childAt = (MonthView) ((ViewPager) container).getChildAt(position);
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

				MonthView mv = addMonthViewAt((ViewPager) container, position, time);

				titles[position] = Utils.formatMonthYear(HistoryMonthActivity.this, mv.getTime());

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
	}

	private class SessionsContentObserver extends ContentObserver {

		public SessionsContentObserver() {
			super(new Handler());
		}

		@Override
		public void onChange(boolean selfChange) {

			getSupportLoaderManager().getLoader(0).forceLoad();
			super.onChange(selfChange);
		}
	}

	private static final int DAY_OF_WEEK_KINDS[] = { Calendar.SUNDAY, Calendar.MONDAY,
			Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY,
			Calendar.SATURDAY };
	private static final int DAY_OF_WEEK_LABEL_IDS[] = { R.id.day0, R.id.day1, R.id.day2,
			R.id.day3, R.id.day4, R.id.day5, R.id.day6 };
	private ViewPager monthPager;
	private MonthPagerAdapter monthAdapter;

	private int startDay;

	ArrayList<Long[]> mSessions = new ArrayList<Long[]>(0);

	private int focusedPage = 0;

	private SessionsContentObserver sessionsObserver;

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

	void eventsChanged() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				for (int i = 0; i < monthPager.getChildCount(); i++) {
					final MonthView mv = (MonthView) monthPager.getChildAt(i);
					mv.forceReloadEvents(mSessions);
				}
			}
		});
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history_month;
	}

	public ArrayList<Long[]> getSessionsInInterval(long startMillis, int days) {
		synchronized (mSessions) {
			final ArrayList<Long[]> sessions = new ArrayList<Long[]>(20);
			final Time local = new Time();

			local.set(startMillis);

			// expand start and days to include days shown from previous month
			// and next month. can be slightly wasteful.
			// start -= 1000 * 60 * 60 * 24 * 7; // 7 days
			// days += 7;

			Time.getJulianDay(startMillis, local.gmtoff);
			local.monthDay += days;
			final long endMillis = local.normalize(true);
			// endMillis+=startMillis;

			for (final Long[] session : mSessions) {
				final long startTime = session[0];
				final long endTime = session[1];
				if (startTime >= startMillis && startTime <= endMillis) {
					sessions.add(session);
				}
			}

			// TODO
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
		sessionsObserver = new SessionsContentObserver();
		getContentResolver().registerContentObserver(SleepSessions.MainTable.CONTENT_URI, true,
				sessionsObserver);
		getSupportLoaderManager().initLoader(0, null, this);

		ActionBar bar = getSupportActionBar();
		// bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		final Time now = new Time();
		now.setToNow();

		// Get first day of week based on locale and populate the day headers
		startDay = Calendar.getInstance().getFirstDayOfWeek();
		final int diff = startDay - Calendar.SUNDAY - 1;
		final int startDay = Utils.getFirstDayOfWeek();
		final int weekendColor = getResources().getColor(R.color.primary1);

		for (int day = 0; day < 7; day++) {
			final String dayString = DateUtils.getDayOfWeekString(
					(DAY_OF_WEEK_KINDS[day] + diff) % 7 + 1, DateUtils.LENGTH_MEDIUM);
			final TextView label = (TextView) findViewById(DAY_OF_WEEK_LABEL_IDS[day]);
			label.setText(dayString);
			if (Utils.isSunday(day, startDay) || Utils.isSaturday(day, startDay)) {
				label.setTextColor(weekendColor);
			}
		}

		monthAdapter = new MonthPagerAdapter();
		monthPager = (ViewPager) findViewById(R.id.monthpager);
		monthPager.setAdapter(monthAdapter);

		final TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setFooterColor(getResources().getColor(R.color.primary1));
		indicator.setViewPager(monthPager, 1);
		indicator.setOnPageChangeListener(new IndicatorPageChangeListener(indicator));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, SleepSessions.MainTable.CONTENT_URI,
				SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, null, null, null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_multiple_history, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		getContentResolver().unregisterContentObserver(sessionsObserver);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mSessions = new ArrayList<Long[]>(0);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				try {

					mSessions = SleepSessions.getStartAndEndTimesFromCursor(
							HistoryMonthActivity.this, data);
					eventsChanged();
				} catch (IllegalStateException ex) {
				} catch (StaleDataException ex) {
				}
			}
		}).start();
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
}
