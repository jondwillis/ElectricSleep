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

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Calendar;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.Time;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.HistoryListFragment;
import com.androsz.electricsleepbeta.app.HistoryMonthFragment;
import com.androsz.electricsleepbeta.app.ReviewSleepActivity;
import com.androsz.electricsleepbeta.db.SleepSession;

public class MonthView extends View {

    private static int BUSY_BITS_MARGIN = 4;
	private static int BUSY_BITS_WIDTH = 10;
	private static int EVENT_NUM_DAYS = 31;
	private static int HORIZONTAL_FLING_THRESHOLD = 33;
	private static float HOUR_GAP = 0f;
	private static float MIN_EVENT_HEIGHT = 1f;
	private static int MONTH_DAY_GAP = 1;
	private static int MONTH_DAY_TEXT_SIZE = 20;
	private static float mScale = 0; // Used for supporting different screen
										// densities
	/**
	 * The selection modes are HIDDEN, PRESSED, SELECTED, and LONGPRESS.
	 */
	private static final int SELECTION_HIDDEN = 0;

	private static final int SELECTION_LONGPRESS = 3;

	private static final int SELECTION_PRESSED = 1;
	private static final int SELECTION_SELECTED = 2;
	private static int TEXT_TOP_MARGIN = 7;

	// densities
	private static int WEEK_GAP = 0;

	// An array of which days have events for quick reference
	private final boolean[] eventDay = new boolean[EVENT_NUM_DAYS];

	// For drawing to an off-screen Canvas
	private Bitmap mBitmap;

	private final Rect mBitmapRect = new Rect();
	private int mBorder;
	private Drawable mBoxLongPressed;
	private Drawable mBoxPressed;

	private Drawable mBoxSelected;
	private int mBusybitsColor;
	private Canvas mCanvas;
	private int mCellHeight;

	private int mCellWidth;

	private DayOfMonthCursor mCursor;

	// Bitmap caches.
	// These improve performance by minimizing calls to NinePatchDrawable.draw()
	// for common
	// drawables for day backgrounds.
	// mDayBitmapCache is indexed by a unique integer constructed from the
	// width/height.
	private final SparseArray<Bitmap> mDayBitmapCache = new SparseArray<Bitmap>(4);

	private List<Long[]> mSessions = new ArrayList<Long[]>(0);
	/**
	 * The first Julian day of the current month.
	 */
	private int mFirstJulianDay;

	private GestureDetector mGestureDetector;
	private boolean mLaunchDayView;
	private int mMonthDayNumberColor;
	// Cached colors
    private int mMonthBackgroundColor;

	private int mMonthOtherMonthColor;
	private int mMonthOtherMonthDayNumberColor;

	private int mMonthSaturdayColor;
	private int mMonthSundayColor;

	private int mMonthTodayNumberColor;

    private int mEventOnColor;
    private int mEventOffColor;

    // This Time object is used to set the time for the other Month view.
	private final Time mOtherViewCalendar = new Time();
	private final HistoryMonthFragment mParentActivity;

	// Pre-allocate and reuse
	private final Rect mRect = new Rect();

	private final RectF mRectF = new RectF();

	private boolean mRedrawScreen = true;

	private Resources mResources;
	private final Time mSavedTime = new Time(); // the time when we entered this
												// view
	private int mSelectionMode = SELECTION_HIDDEN;
	// These booleans disable features that were taken out of the spec.
	private final boolean mShowWeekNumbers = false;
	private int mStartDay;
	// This Time object is used for temporary calculations and is allocated
	// once to avoid extra garbage collection
	private final Time mTempTime = new Time();
	private Time mToday;
	private Drawable mTodayBackground;

	private Time mViewCalendar;

	public MonthView(HistoryMonthFragment historyMonthActivity) {
		super(historyMonthActivity.getActivity());
		if (mScale == 0) {
			mScale = getContext().getResources().getDisplayMetrics().density;
			if (mScale != 1) {
				WEEK_GAP *= mScale;
				MONTH_DAY_GAP *= mScale;
				HOUR_GAP *= mScale;

				MONTH_DAY_TEXT_SIZE *= mScale;
				TEXT_TOP_MARGIN *= mScale;
				HORIZONTAL_FLING_THRESHOLD *= mScale;
				MIN_EVENT_HEIGHT *= mScale;
				BUSY_BITS_WIDTH *= mScale;
				BUSY_BITS_MARGIN *= mScale;
			}
		}

		mParentActivity = historyMonthActivity;
		init();
	}

	/**
	 * Clears the bitmap cache. Generally only needed when the screen size
	 * changed.
	 */
	private void clearBitmapCache() {
		recycleAndClearBitmapCache(mDayBitmapCache);
	}

	private void doDraw(Canvas canvas) {
		final boolean isLandscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;

		final Paint p = new Paint();
		final Rect r = mRect;
		final int columnDay1 = mCursor.getColumnOf(1);

		// Get the Julian day for the date at row 0, column 0.
		int day = mFirstJulianDay - columnDay1;

		int weekNum = 0;
		Calendar calendar = null;
		if (mShowWeekNumbers) {
			calendar = Calendar.getInstance();
			final boolean noPrevMonth = (columnDay1 == 0);

			// Compute the week number for the first row.
			weekNum = getWeekOfYear(0, 0, noPrevMonth, calendar);
		}

		for (int row = 0; row < 6; row++) {
			for (int column = 0; column < 7; column++) {
				drawBox(day, weekNum, row, column, canvas, p, r, isLandscape);
				day += 1;
			}

			if (mShowWeekNumbers) {
				weekNum += 1;
				if (weekNum >= 53) {
					final boolean inCurrentMonth = (day - mFirstJulianDay < EVENT_NUM_DAYS);
					weekNum = getWeekOfYear(row + 1, 0, inCurrentMonth, calendar);
				}
			}
		}

		drawGrid(canvas, p);
	}

	/**
	 * Draw a single box onto the canvas.
	 *
	 * @param day
	 *            The Julian day.
	 * @param weekNum
	 *            The week number.
	 * @param row
	 *            The row of the box (0-5).
	 * @param column
	 *            The column of the box (0-6).
	 * @param canvas
	 *            The canvas to draw on.
	 * @param p
	 *            The paint used for drawing.
	 * @param r
	 *            The rectangle used for each box.
	 * @param isLandscape
	 *            Is the current orientation landscape.
	 */
	private void drawBox(int day, int weekNum, int row, int column, Canvas canvas, Paint p, Rect r,
			boolean isLandscape) {

		// Only draw the selection if we are in the press state or if we have
		// moved the cursor with key input.
		boolean drawSelection = false;
		if (mSelectionMode != SELECTION_HIDDEN) {
			drawSelection = mCursor.isSelected(row, column);
		}

		final boolean withinCurrentMonth = mCursor.isWithinCurrentMonth(row, column);
		boolean isToday = false;
		final int dayOfBox = mCursor.getDayAt(row, column);
		if (dayOfBox == mToday.monthDay && mCursor.getYear() == mToday.year
				&& mCursor.getMonth() == mToday.month) {
			isToday = true;
		}

		final int y = WEEK_GAP + row * (WEEK_GAP + mCellHeight);
		final int x = mBorder + column * (MONTH_DAY_GAP + mCellWidth);

		r.left = x;
		r.top = y;
		r.right = x + mCellWidth;
		r.bottom = y + mCellHeight;

		// Adjust the left column, right column, and bottom row to leave
		// no border.
		if (column == 0) {
			r.left = -1;
		} else if (column == 6) {
			r.right += mBorder + 2;
		}

		if (row == 5) {
			r.bottom = getMeasuredHeight();
		}

		// Draw the cell contents (excluding monthDay number)
		if (!withinCurrentMonth) {
			// Adjust cell boundaries to compensate for the different border
			// style.
			r.top--;
			if (column != 0) {
				r.left--;
			}
			p.setStyle(Style.FILL);
			p.setColor(mMonthOtherMonthColor);
			canvas.drawRect(r, p);
		} else if (drawSelection) {
			if (mSelectionMode == SELECTION_SELECTED) {
				mBoxSelected.setBounds(r);
				mBoxSelected.draw(canvas);
			} else if (mSelectionMode == SELECTION_PRESSED) {
				mBoxPressed.setBounds(r);
				mBoxPressed.draw(canvas);
			} else {
				mBoxLongPressed.setBounds(r);
				mBoxLongPressed.draw(canvas);
			}

			// Places events for that day
			drawEvents(day, canvas, r, p, false /* draw bb background */);
		} else {
			// Today gets a different background
			if (isToday) {
				// We could cache this for a little bit more performance, but
				// it's not on the
				// performance radar...
				final Drawable background = mTodayBackground;
				background.setBounds(r);
				background.draw(canvas);
			} else {
                // Background for dates that are within the month.
                p.setStyle(Style.FILL);
                p.setColor(mMonthBackgroundColor);
                canvas.drawRect(r, p);
            }

			// Places events for that day
			drawEvents(day, canvas, r, p, !isToday /* draw bb background */);
		}

		// Draw the monthDay number
		p.setStyle(Paint.Style.FILL);
		p.setAntiAlias(true);
		p.setTypeface(null);
		p.setTextSize(MONTH_DAY_TEXT_SIZE);

		if (!withinCurrentMonth) {
			p.setColor(mMonthOtherMonthDayNumberColor);
		} else {
			if (isToday && !drawSelection) {
				p.setColor(mMonthTodayNumberColor);
			} else if (Utils.isSunday(column, mStartDay)) {
				p.setColor(mMonthSundayColor);
			} else if (Utils.isSaturday(column, mStartDay)) {
				p.setColor(mMonthSaturdayColor);
			} else {
				p.setColor(mMonthDayNumberColor);
			}
			// bolds the day if there's an event that day
			p.setFakeBoldText(eventDay[day - mFirstJulianDay]);
		}
		/*
		 * Drawing of day number is done hereeasy to find tags draw number draw
		 * day
		 */
		p.setTextAlign(Paint.Align.CENTER);
		// center of text
		final int textX = r.left + (r.right - BUSY_BITS_MARGIN - BUSY_BITS_WIDTH - r.left) / 2;
		final int textY = (int) (r.top + p.getTextSize() + TEXT_TOP_MARGIN); // bottom
		// of
		// text
		canvas.drawText(String.valueOf(mCursor.getDayAt(row, column)), textX, textY, p);
	}

	// Draw busybits for a single event
	private RectF drawEventRect(Rect rect, SessionGeometry session, Canvas canvas, Paint p) {

		p.setColor(mBusybitsColor);

		final int left = rect.right - BUSY_BITS_MARGIN - BUSY_BITS_WIDTH;
		final int bottom = rect.bottom - BUSY_BITS_MARGIN;

		final RectF rf = mRectF;
		rf.top = session.top;
		// Make sure we don't go below the bottom of the bb bar
		rf.bottom = Math.min(session.bottom, bottom);
		rf.left = left;
		rf.right = left + BUSY_BITS_WIDTH;

		canvas.drawRect(rf, p);

		return rf;
	}

	// /Create and draw the event busybits for this day
	private void drawEvents(int date, Canvas canvas, Rect rect, Paint p, boolean drawBg) {
		// The top of the busybits section lines up with the top of the day
		// number
		final int top = rect.top + TEXT_TOP_MARGIN + BUSY_BITS_MARGIN;
		final int left = rect.right - BUSY_BITS_MARGIN - BUSY_BITS_WIDTH;

        Paint paint = new Paint();
        paint.setColor(mEventOffColor);
        final int height = Math.abs(rect.bottom - rect.top);
        final int width = Math.abs(rect.right - rect.left);
        int cy = (int) (rect.top + (height / 1.4));
        int cx = (int) (rect.left + (width / 2));
        int r = (int) (width / 4);
        canvas.drawCircle(cx, cy, r, paint);
    }

	/**
	 * Draw the grid lines for the calendar
	 *
	 * @param canvas
	 *            The canvas to draw on.
	 * @param p
	 *            The paint used for drawing.
	 */
	private void drawGrid(Canvas canvas, Paint p) {
		p.setColor(Color.WHITE);
		p.setAntiAlias(false);

		final int width = getMeasuredWidth();
		final int height = getMeasuredHeight();

		for (int row = 0; row < 6; row++) {
			final int y = WEEK_GAP + row * (WEEK_GAP + mCellHeight) - 1;
			canvas.drawLine(0, y, width, y, p);
		}
		for (int column = 1; column < 7; column++) {
			final int x = mBorder + column * (MONTH_DAY_GAP + mCellWidth) - 1;
			canvas.drawLine(x, WEEK_GAP, x, height, p);
		}
	}

	private void drawingCalc(int width, int height) {
		mCellHeight = (height - (6 * WEEK_GAP)) / 6;
		// TODO
		// mEventGeometry
		// .setHourHeight((mCellHeight - BUSY_BITS_MARGIN * 2 - TEXT_TOP_MARGIN)
		// / 24.0f);
		mCellWidth = (width - (6 * MONTH_DAY_GAP)) / 7;
		mBorder = (width - 6 * (mCellWidth + MONTH_DAY_GAP) - mCellWidth) / 2;

		if (((mBitmap == null) || mBitmap.isRecycled() || (mBitmap.getHeight() != height) || (mBitmap
				.getWidth() != width)) && (width > 0) && (height > 0)) {
			if (mBitmap != null) {
				mBitmap.recycle();
			}
			mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			mCanvas = new Canvas(mBitmap);
		}

		mBitmapRect.top = 0;
		mBitmapRect.bottom = height;
		mBitmapRect.left = 0;
		mBitmapRect.right = width;
	}

	public void forceReloadEvents(final List<Long[]> sessions) {
		new Thread(new Runnable() {
			@Override
			public void run() {
//				android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_LOWEST);
				//final Time monthStart = mTempTime;
				//monthStart.set(mViewCalendar);
				//monthStart.monthDay = 1;
				//monthStart.hour = 0;
				//monthStart.minute = 0;
				//monthStart.second = 0;
				//final long startOfMonthMillis = monthStart.normalize(true);
				mSessions = new ArrayList<Long[]>(sessions);//new ArrayList<Long[]>(sessions);
				// Clear out event days
				Arrays.fill(eventDay, false);
				// Compute the new set of days with events
				for (final Long[] session : mSessions) {
					long startDay = session[2] - mFirstJulianDay;
					long endDay = session[3] - mFirstJulianDay + 1;
					if (startDay < EVENT_NUM_DAYS || endDay >= 0) {
						if (startDay < 0) {
							startDay = 0;
						}
						if (startDay > EVENT_NUM_DAYS) {
							startDay = EVENT_NUM_DAYS;
						}
						if (endDay < 0) {
							endDay = 0;
						}
						if (endDay > EVENT_NUM_DAYS) {
							endDay = EVENT_NUM_DAYS;
						}
						for (int j = (int) startDay; j < endDay; j++) {
							eventDay[j] = true;
						}
					}
				}

				mRedrawScreen = true;

				postInvalidate();
			}
		}).start();
	}

	private long getSelectedMillisFor(int x, int y) {
		final int row = (y - WEEK_GAP) / (WEEK_GAP + mCellHeight);
		int column = (x - mBorder) / (MONTH_DAY_GAP + mCellWidth);
		if (column > 6) {
			column = 6;
		}

		final DayOfMonthCursor c = mCursor;
		final Time time = mTempTime;
		time.set(mViewCalendar);
		time.set(0, 0, 0, time.monthDay, time.month, time.year);

		// Compute the day number from the row and column. If the row and
		// column are in a different month from the current one, then the
		// monthDay might be negative or it might be greater than the number
		// of days in this month, but that is okay because the normalize()
		// method will adjust the month (and year) if necessary.
		time.monthDay = 7 * row + column - c.getOffset() + 1;
		return time.normalize(true);
	}

	public long getSelectedTimeInMillis() {
		final Time time = mTempTime;
		time.set(mViewCalendar);

		time.month += mCursor.getSelectedMonthOffset();
		time.monthDay = mCursor.getSelectedDayOfMonth();

		// Restore the saved hour:minute:second offset from when we entered
		// this view.
		time.second = mSavedTime.second;
		time.minute = mSavedTime.minute;
		time.hour = mSavedTime.hour;
		return time.normalize(true);
	}

	public int getSelectionMode() {
		return mSelectionMode;
	}

	public Time getTime() {
		return mViewCalendar;
	}

	private int getWeekOfYear(int row, int column, boolean isWithinCurrentMonth, Calendar calendar) {
		calendar.set(Calendar.DAY_OF_MONTH, mCursor.getDayAt(row, column));
		if (isWithinCurrentMonth) {
			calendar.set(Calendar.MONTH, mCursor.getMonth());
			calendar.set(Calendar.YEAR, mCursor.getYear());
		} else {
			int month = mCursor.getMonth();
			int year = mCursor.getYear();
			if (row < 2) {
				// Previous month
				if (month == 0) {
					year--;
					month = 11;
				} else {
					month--;
				}
			} else {
				// Next month
				if (month == 11) {
					year++;
					month = 0;
				} else {
					month++;
				}
			}
			calendar.set(Calendar.MONTH, month);
			calendar.set(Calendar.YEAR, year);
		}

		return calendar.get(Calendar.WEEK_OF_YEAR);
	}

	private void init() {
		setFocusable(true);
		setClickable(true);
		mViewCalendar = new Time();
		final long now = System.currentTimeMillis();
		mViewCalendar.set(now);
		mViewCalendar.monthDay = 1;
		final long millis = mViewCalendar.normalize(true /* ignore DST */);
		mFirstJulianDay = Time.getJulianDay(millis, mViewCalendar.gmtoff);
		mStartDay = Utils.getFirstDayOfWeek();
		mViewCalendar.set(now);

		mCursor = new DayOfMonthCursor(mViewCalendar.year, mViewCalendar.month,
				mViewCalendar.monthDay, mParentActivity.getStartDay());
		mToday = new Time();
		mToday.set(System.currentTimeMillis());

		mResources = mParentActivity.getResources();
		mBoxSelected = mResources.getDrawable(R.drawable.month_view_selected);
		mBoxPressed = mResources.getDrawable(R.drawable.month_view_pressed);
		mBoxLongPressed = mResources.getDrawable(R.drawable.month_view_longpress);

		mTodayBackground = mResources.getDrawable(R.drawable.month_view_today_background);

		// Cache color lookups
		final Resources res = getResources();
        mMonthBackgroundColor = res.getColor(R.color.month_background);
		mMonthOtherMonthColor = res.getColor(R.color.month_other_month);
		mMonthOtherMonthDayNumberColor = res.getColor(R.color.month_other_month_day_number);
		mMonthDayNumberColor = res.getColor(R.color.month_day_number);
		mMonthTodayNumberColor = res.getColor(R.color.month_today_number);
		mMonthSaturdayColor = res.getColor(R.color.month_saturday);
		mMonthSundayColor = res.getColor(R.color.month_sunday);
		mBusybitsColor = res.getColor(R.color.primary1);
        mEventOnColor = res.getColor(R.color.primary_dark);
        mEventOffColor = res.getColor(R.color.month_day_event_off);

        mGestureDetector = new GestureDetector(getContext(),
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onDown(MotionEvent e) {
						// Launch the Day/Agenda view when the finger lifts up,
						// unless the finger moves before lifting up (onFling or
						// onScroll).
						mLaunchDayView = true;
						return true;
					}

					@Override
					public void onLongPress(MotionEvent e) {
						// If mLaunchDayView is true, then we haven't done any
						// scrolling
						// after touching the screen, so allow long-press to
						// proceed
						// with popping up the context menu.
						if (mLaunchDayView) {
							mLaunchDayView = false;
							mSelectionMode = SELECTION_LONGPRESS;
							mRedrawScreen = true;
							invalidate();
							performLongClick();
						}
					}

					@Override
					public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
							float distanceY) {
						// If the user moves his finger after touching, then do
						// not
						// launch the Day view when he lifts his finger. Also,
						// turn
						// off the selection.
						mLaunchDayView = false;

						if (mSelectionMode != SELECTION_HIDDEN) {
							mSelectionMode = SELECTION_HIDDEN;
							mRedrawScreen = true;
							invalidate();
						}
						return true;
					}

					@Override
					public void onShowPress(MotionEvent e) {
						// Highlight the selected day.
						setSelectedCell(e);
						mSelectionMode = SELECTION_PRESSED;
						mRedrawScreen = true;
						invalidate();
					}

					@Override
					public boolean onSingleTapUp(MotionEvent e) {
						if (mLaunchDayView) {
							setSelectedCell(e);
							mSelectionMode = SELECTION_SELECTED;
							mRedrawScreen = true;
							invalidate();
							mLaunchDayView = false;
							final int x = (int) e.getX();
							final int y = (int) e.getY();
							final long millis = getSelectedMillisFor(x, y);

							reviewSleepIfNecessary(millis);
						}

						return true;
					}

					public void setSelectedCell(MotionEvent e) {
						final int x = (int) e.getX();
						final int y = (int) e.getY();
						int row = (y - WEEK_GAP) / (WEEK_GAP + mCellHeight);
						int col = (x - mBorder) / (MONTH_DAY_GAP + mCellWidth);
						if (row > 5) {
							row = 5;
						}
						if (col > 6) {
							col = 6;
						}

						// Highlight the selected day.
						mCursor.setSelectedRowColumn(row, col);
					}
				});
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		// No need to hang onto the bitmaps...
		clearBitmapCache();
		if (mBitmap != null) {
			mBitmap.recycle();
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mRedrawScreen) {
			if (mCanvas == null) {
				drawingCalc(getWidth(), getHeight());
			}

			// If we are zero-sized, the canvas will remain null so check again
			if (mCanvas != null) {
				// Clear the background
				final Canvas bitmapCanvas = mCanvas;
				bitmapCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
				doDraw(bitmapCanvas);
				mRedrawScreen = false;
			}
		}

		// If we are zero-sized, the bitmap will be null so guard against this
		if (mBitmap != null) {
			canvas.drawBitmap(mBitmap, mBitmapRect, mBitmapRect, null);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (mSelectionMode == SELECTION_HIDDEN) {
			if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
					|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_UP
					|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
				// Display the selection box but don't move or select it
				// on this key press.
				mSelectionMode = SELECTION_SELECTED;
				mRedrawScreen = true;
				invalidate();
				return true;
			} else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
				// Display the selection box but don't select it
				// on this key press.
				mSelectionMode = SELECTION_PRESSED;
				mRedrawScreen = true;
				invalidate();
				return true;
			}
		}

		mSelectionMode = SELECTION_SELECTED;
		boolean redraw = false;
		Time other = null;

		switch (keyCode) {
		case KeyEvent.KEYCODE_ENTER:
			final long millis = getSelectedTimeInMillis();
			reviewSleepIfNecessary(millis);
			return true;
		case KeyEvent.KEYCODE_DPAD_UP:
			if (mCursor.up()) {
				other = mOtherViewCalendar;
				other.set(mViewCalendar);
				other.month -= 1;
				other.monthDay = mCursor.getSelectedDayOfMonth();

				// restore the calendar cursor for the animation
				mCursor.down();
			}
			redraw = true;
			break;

		case KeyEvent.KEYCODE_DPAD_DOWN:
			if (mCursor.down()) {
				other = mOtherViewCalendar;
				other.set(mViewCalendar);
				other.month += 1;
				other.monthDay = mCursor.getSelectedDayOfMonth();

				// restore the calendar cursor for the animation
				mCursor.up();
			}
			redraw = true;
			break;

		case KeyEvent.KEYCODE_DPAD_LEFT:
			if (mCursor.left()) {
				other = mOtherViewCalendar;
				other.set(mViewCalendar);
				other.month -= 1;
				other.monthDay = mCursor.getSelectedDayOfMonth();

				// restore the calendar cursor for the animation
				mCursor.right();
			}
			redraw = true;
			break;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if (mCursor.right()) {
				other = mOtherViewCalendar;
				other.set(mViewCalendar);
				other.month += 1;
				other.monthDay = mCursor.getSelectedDayOfMonth();

				// restore the calendar cursor for the animation
				mCursor.left();
			}
			redraw = true;
			break;
		}

		if (other != null) {
			other.normalize(true /* ignore DST */);
			// TODO
			// mNavigator.goTo(other, true);
		} else if (redraw) {
			mRedrawScreen = true;
			invalidate();
		}

		return redraw;
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		final long duration = event.getEventTime() - event.getDownTime();

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (mSelectionMode == SELECTION_HIDDEN) {
				// Don't do anything unless the selection is visible.
				break;
			}

			if (mSelectionMode == SELECTION_PRESSED) {
				// This was the first press when there was nothing selected.
				// Change the selection from the "pressed" state to the
				// the "selected" state. We treat short-press and
				// long-press the same here because nothing was selected.
				mSelectionMode = SELECTION_SELECTED;
				mRedrawScreen = true;
				invalidate();
				break;
			}

			// Check the duration to determine if this was a short press
			if (duration < ViewConfiguration.getLongPressTimeout()) {
				final long millis = getSelectedTimeInMillis();

				reviewSleepIfNecessary(millis);
			} else {
				mSelectionMode = SELECTION_LONGPRESS;
				mRedrawScreen = true;
				invalidate();
				performLongClick();
			}
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldw, int oldh) {
		drawingCalc(width, height);
		// If the size changed, then we should rebuild the bitmaps...
		clearBitmapCache();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event)) {
			return true;
		}

		return super.onTouchEvent(event);
	}

	private void recycleAndClearBitmapCache(SparseArray<Bitmap> bitmapCache) {
		final int size = bitmapCache.size();
		for (int i = 0; i < size; i++) {
			bitmapCache.valueAt(i).recycle();
		}
		bitmapCache.clear();

	}

	private void reviewSleepIfNecessary(final long millis) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				//final ArrayList<Long[]> applicableEvents = new ArrayList<Long[]>();
				final long ONE_DAY_IN_MS = 1000 * 60 * 60 * 24;

				int julianDay = Time.getJulianDay(millis, new Time().gmtoff);
				List<Long> applicableRowIds = new ArrayList<Long>();
				for (final Long[] session : mSessions) {
					if (julianDay >= session[2] && julianDay <= session[3]) {
						applicableRowIds.add(session[4]);
					}
					/*
					 * final long startTime = session[0] - thismillis; final
					 * long endTime = session[1] - thismillis; if ((endTime > 0)
					 * && ((startTime <= ONE_DAY_IN_MS && startTime > 0) ||
					 * startTime < 0)) {
					 *
					 * applicableEvents.add(session); }
					 */
				}

				if (applicableRowIds.size() == 1) {
					final Intent reviewSleepIntent = new Intent(getContext(),
							ReviewSleepActivity.class);
					final Uri data = Uri.withAppendedPath(SleepSession.CONTENT_URI,
							String.valueOf(applicableRowIds.get(0)));
					reviewSleepIntent.setData(data);
					getContext().startActivity(reviewSleepIntent);
				} else if (applicableRowIds.size() > 1) {

					getContext().startActivity(
							new Intent(getContext(), HistoryActivity.class).putExtra(
                                HistoryActivity.SEARCH_FOR, millis));
				}

				return null;
			}
		}.execute();

	}

	public void setTime(Time time) {
		mViewCalendar.set(time);
		mViewCalendar.monthDay = 1;
		final long millis = mViewCalendar.normalize(true /* ignore DST */);
		mFirstJulianDay = Time.getJulianDay(millis, mViewCalendar.gmtoff);
		mViewCalendar.set(time);

		mCursor = new DayOfMonthCursor(time.year, time.month, time.monthDay,
				mCursor.getWeekStartDay());

		this.mSelectionMode = MonthView.SELECTION_HIDDEN;

		mRedrawScreen = true;
		invalidate();
	}

	public void setSelectedTime(Time time) {
		// Save the selected time so that we can restore it later when we switch
		// views.
		mSavedTime.set(time);

		mViewCalendar.set(time);
		mViewCalendar.monthDay = 1;
		final long millis = mViewCalendar.normalize(true /* ignore DST */);
		mFirstJulianDay = Time.getJulianDay(millis, mViewCalendar.gmtoff);
		mViewCalendar.set(time);

		mCursor = new DayOfMonthCursor(time.year, time.month, time.monthDay,
				mCursor.getWeekStartDay());

		mRedrawScreen = true;
		invalidate();
	}

	public void setSelectionMode(int selectionMode) {
		mSelectionMode = selectionMode;
	}

	@Override
	public String toString() {
		return Utils.formatMonthYear(getContext(), mViewCalendar);
	}
}
