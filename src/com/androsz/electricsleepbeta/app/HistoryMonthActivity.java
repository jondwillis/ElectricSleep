package com.androsz.electricsleepbeta.app;

import java.util.ArrayList;
import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.TitlePageIndicator;
import android.support.v4.view.TitleProvider;
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

public class HistoryMonthActivity extends HostActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private class MonthPagerAdapter extends PagerAdapter implements TitleProvider {

		ViewPager container;

		private MonthView addMonthViewAt(int position, Time time) {
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
			((ViewPager) container).removeViewAt(position);
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
			final MonthView mv = (MonthView) container.getChildAt(position);
			return Utils.formatMonthYear(HistoryMonthActivity.this, mv.getTime());
		}

		@Override
		public Object instantiateItem(View container, int position) {
			this.container = (ViewPager) container;
			final Time time = new Time();
			time.set(System.currentTimeMillis());
			time.month += (position - 1); // add the offset from the center time
			time.normalize(true);

			return addMonthViewAt(position, time);
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return view == object;
		}

		private MonthView removeMonthViewAt(ViewPager container, int position) {
			final MonthView mv = (MonthView) container.getChildAt(position);
			container.removeViewAt(position);
			return mv;
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
					mv.forceReloadEvents();
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

		final long time = Utils.timeFromIntentInMillis(getIntent());

		final Time now = new Time();
		now.set(time);

		// Get first day of week based on locale and populate the day headers
		startDay = Calendar.getInstance().getFirstDayOfWeek();
		final int diff = startDay - Calendar.SUNDAY - 1;
		final int startDay = Utils.getFirstDayOfWeek();
		final int sundayColor = getResources().getColor(R.color.sunday_text_color);
		final int saturdayColor = getResources().getColor(R.color.saturday_text_color);

		for (int day = 0; day < 7; day++) {
			final String dayString = DateUtils.getDayOfWeekString(
					(DAY_OF_WEEK_KINDS[day] + diff) % 7 + 1, DateUtils.LENGTH_MEDIUM);
			final TextView label = (TextView) findViewById(DAY_OF_WEEK_LABEL_IDS[day]);
			label.setText(dayString);
			if (Utils.isSunday(day, startDay)) {
				label.setTextColor(sundayColor);
			} else if (Utils.isSaturday(day, startDay)) {
				label.setTextColor(saturdayColor);
			}
		}

		// setTitle(Utils.formatMonthYear(this, now));

		monthAdapter = new MonthPagerAdapter();
		monthPager = (ViewPager) findViewById(R.id.monthpager);
		monthPager.setAdapter(monthAdapter);
		final TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(monthPager, 1);
		indicator.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_IDLE) {

					final Time oldTopTime = new Time(((MonthView) monthPager.getChildAt(0))
							.getTime());
					final Time oldCenterTime = new Time(((MonthView) monthPager.getChildAt(1))
							.getTime());
					final Time oldBottomTime = new Time(((MonthView) monthPager.getChildAt(2))
							.getTime());

					if (focusedPage == 0) {
						// HistoryMonthActivity.this.setTitle(Utils
						// .formatMonthYear(HistoryMonthActivity.this,
						// oldTopTime));

						final Time time = new Time(oldTopTime);
						time.month--;
						time.normalize(true);

						// TODO: load and switch shown events
						((MonthView) monthPager.getChildAt(0)).setSelectedTime(time);
						((MonthView) monthPager.getChildAt(1)).setSelectedTime(oldTopTime);
						((MonthView) monthPager.getChildAt(2)).setSelectedTime(oldCenterTime);

					} else if (focusedPage == 2) {

						// HistoryMonthActivity.this.setTitle(Utils
						// .formatMonthYear(HistoryMonthActivity.this,
						// oldBottomTime));

						final Time time = new Time(oldBottomTime);
						time.month++;
						time.normalize(true);

						((MonthView) monthPager.getChildAt(0)).setSelectedTime(oldCenterTime);
						((MonthView) monthPager.getChildAt(1)).setSelectedTime(oldBottomTime);
						((MonthView) monthPager.getChildAt(2)).setSelectedTime(time);
					}

					// always set to middle page to continue to be able to
					// scroll up/down
					indicator.setCurrentItem(1, false);
				}
			}

			@Override
			public void onPageSelected(int position) {
				focusedPage = position;
			}
		});
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
				mSessions = SleepSessions.getStartAndEndTimesFromCursor(HistoryMonthActivity.this,
						data);
				eventsChanged();
				// TODO: notify MonthViews that mEvents have change in a
				// ..better way?
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
