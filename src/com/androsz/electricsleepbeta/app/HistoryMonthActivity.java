package com.androsz.electricsleepbeta.app;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.DirectionalViewPager;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.db.SleepSessions;
import com.androsz.electricsleepbeta.util.PointD;
import com.androsz.electricsleepbeta.widget.calendar.MonthView;
import com.androsz.electricsleepbeta.widget.calendar.Utils;

public class HistoryMonthActivity extends HostActivity implements
		LoaderManager.LoaderCallbacks<Cursor> {

	private static final int DAY_OF_WEEK_KINDS[] = { Calendar.SUNDAY,
			Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
			Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY };
	private static final int DAY_OF_WEEK_LABEL_IDS[] = { R.id.day0, R.id.day1,
			R.id.day2, R.id.day3, R.id.day4, R.id.day5, R.id.day6 };
	private DirectionalViewPager monthPager;
	private MonthPagerAdapter monthAdapter;
	private Time mTime;
	private int mStartDay;
	LinkedHashMap<Long, SleepSession> mSessions = new LinkedHashMap<Long, SleepSession>(
			0);

	private int focusedPage = 0;

	private SessionsContentObserver sessionsObserver;

	private class MonthPagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return 5;
		}

		@Override
		public void startUpdate(View container) {			
		}
		
		public int getItemPosition(Object object) {
		    return POSITION_NONE;
		}
		
		public void replaceViewAt(DirectionalViewPager container, int position, MonthView replacement)
		{
			container.removeViewAt(position);
			container.addView(replacement, position);
		}
		
		@Override
		public Object instantiateItem(View container, int position) {
			
			Time time = new Time(mTime);
			time.month += (position - 1); //add the offset from the center time
			time.normalize(true);

			MonthView mv = new MonthView(HistoryMonthActivity.this);
			mv.setLayoutParams(new ViewSwitcher.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			mv.setSelectedTime(time);
			
			
		    ((DirectionalViewPager) container).addView(mv, position);
		    
			return mv;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((DirectionalViewPager) container).removeViewAt(position);
		}

		@Override
		public void finishUpdate(View container) {
		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			 return view == object;
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
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

	void eventsChanged() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO
				// final MonthView view = getCurrentMonthView();
				// view.forceReloadEvents();
			}
		});
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history_month;
	}

	public LinkedHashMap<Long, SleepSession> getSessionsInInterval(
			long startMillis, int days) {
		synchronized (mSessions) {
			final LinkedHashMap<Long, SleepSession> sessions = new LinkedHashMap<Long, SleepSession>(
					20);
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

			for (final Entry<Long, SleepSession> entry : mSessions.entrySet()) {
				final SleepSession s = entry.getValue();
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

			// TODO
			// SleepSession.computePositions(sessions.values());

			return sessions;
		}

	}

	public int getStartDay() {
		return mStartDay;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

		monthAdapter = new MonthPagerAdapter();
		monthPager = (DirectionalViewPager) findViewById(R.id.monthpager);
		monthPager.setAdapter(monthAdapter);
		monthPager.setCurrentItem(2, false);
		monthPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				focusedPage = position;
			}

			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == DirectionalViewPager.SCROLL_STATE_IDLE) {
					Log.d("ElectricSleep", "IDLE at page " + focusedPage);

					if (focusedPage == 0) {
						
						Time time = new Time(mTime);
						time.month-=2;
						time.normalize(true);
						MonthView mv = new MonthView(HistoryMonthActivity.this);
						mv.setLayoutParams(new ViewSwitcher.LayoutParams(
								android.view.ViewGroup.LayoutParams.MATCH_PARENT,
								android.view.ViewGroup.LayoutParams.MATCH_PARENT));
						mv.setSelectedTime(time);
						
						//monthAdapter.replaceViewAt(monthPager, 0, mv);
						monthAdapter.destroyItem(monthPager, 2, monthPager.getChildAt(2));
						monthAdapter.destroyItem(monthPager, 1, monthPager.getChildAt(1));
						monthAdapter.destroyItem(monthPager, 0, monthPager.getChildAt(0));
						monthAdapter.instantiateItem(monthPager, 0);
						monthAdapter.instantiateItem(monthPager, 1);
						monthAdapter.instantiateItem(monthPager, 2);
					} else if (focusedPage == 2) {
						mTime.month++;
						mTime.normalize(true);
					}

					HistoryMonthActivity.this.setTitle(Utils.formatMonthYear(HistoryMonthActivity.this, mTime));
					//monthAdapter.notifyDataSetChanged();
					
					// always set to middle page to continue to be able to
					// scroll up/down
					monthPager.setCurrentItem(1, false);
				}
			}
		});
		monthPager.setOrientation(DirectionalViewPager.VERTICAL);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, SleepSessions.MainTable.CONTENT_URI,
				SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, null, null,
				null);
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
		mSessions = new LinkedHashMap<Long, SleepSession>(0);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				android.os.Process
						.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
				mSessions = SleepSessions.getSessionsFromCursor(
						HistoryMonthActivity.this, data);
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
