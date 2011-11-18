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

package com.androsz.electricsleepbeta.widget.calendar;

import java.util.ArrayList;
import java.util.Collection;

import android.graphics.Rect;

public class SessionGeometry {
	/* package */static final int MINUTES_PER_HOUR = 60;
	/* package */static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * 24;

	/**
	 * Computes a position for each event. Each event is displayed as a
	 * non-overlapping rectangle. For normal events, these rectangles are
	 * displayed in separate columns in the week view and day view. For all-day
	 * events, these rectangles are displayed in separate rows along the top. In
	 * both cases, each event is assigned two numbers: N, and Max, that specify
	 * that this event is the Nth event of Max number of events that are
	 * displayed in a group. The width and position of each rectangle depend on
	 * the maximum number of rectangles that occur at the same time.
	 * 
	 * @param eventsList
	 *            the list of events, sorted into increasing time order
	 */
	public static void computePositions(Collection<SessionGeometry> eventsList) {
		if (eventsList == null) {
			return;
		}

		doComputePositions(eventsList);
	}

	private static void doComputePositions(Collection<SessionGeometry> eventsList) {
		final Collection<SessionGeometry> activeList = new ArrayList<SessionGeometry>();
		final Collection<SessionGeometry> groupList = new ArrayList<SessionGeometry>();

		long colMask = 0;
		int maxCols = 0;
		for (final SessionGeometry record : eventsList) {

			// long start = record.getStartTime();

			// Remove the inactive events. An event on the active list
			// becomes inactive when its end time is less than or equal to
			// the current event's start time.
			/*
			 * Iterator<SleepRecord> iter = activeList.iterator(); while
			 * (iter.hasNext()) { SleepRecord active = iter.next(); if
			 * (active.getEndTime() <= start) { colMask &= ~(1L <<
			 * active.getColumn()); iter.remove(); } }
			 */

			// If the active list is empty, then reset the max columns, clear
			// the column bit mask, and empty the groupList.
			if (activeList.isEmpty()) {
				for (final SessionGeometry ev : groupList) {
					ev.setMaxColumns(maxCols);
				}
				maxCols = 0;
				colMask = 0;
				groupList.clear();
			}

			// Find the first empty column. Empty columns are represented by
			// zero bits in the column mask "colMask".
			int col = findFirstZeroBit(colMask);
			if (col == 64) {
				col = 63;
			}
			colMask |= (1L << col);
			record.setColumn(col);
			activeList.add(record);
			groupList.add(record);
			final int len = activeList.size();
			if (maxCols < len) {
				maxCols = len;
			}
		}
		for (final SessionGeometry ev : groupList) {
			ev.setMaxColumns(maxCols);
		}
	}

	public static int findFirstZeroBit(long val) {
		for (int ii = 0; ii < 64; ++ii) {
			if ((val & (1L << ii)) == 0) {
				return ii;
			}
		}
		return 64;
	}

	// This is the space from the grid line to the event rectangle.
	private int mCellMargin = 0;

	private float mHourGap;
	private float mMinEventHeight;

	private float mMinuteHeight;
	private int mColumn;
	private int mMaxColumns;
	// The coordinates of the event rectangle drawn on the screen.
	public float left;

	public float right;
	public float top;

	public float bottom;
	Long startDay;

	Long endDay;

	long startTime;

	long endTime;

	public SessionGeometry(Long[] sessionBounds) {
		startTime = sessionBounds[0];
		endTime = sessionBounds[1];

		startDay = sessionBounds[2];
		endDay = sessionBounds[3];
	}

	 // Computes the rectangle coordinates of the given event on the screen.
    // Returns true if the rectangle is visible on the screen.
    boolean computeEventRect(int date, int left, int top, int cellWidth) {

            final float cellMinuteHeight = mMinuteHeight;

            if (this.startDay > date || this.endDay < date) {
                    return false;
            }


            // If the event started on a previous day, then show it starting
            // at the beginning of this day.
            if (this.startDay < date) {
                    this.startTime = 0;
            }

            // If the event ends on a future day, then show it extending to
            // the end of this day.
            if (this.endDay > date) {
                    this.endTime = MINUTES_PER_DAY;
            }

            final int col = this.getColumn();
            final int maxCols = this.getMaxColumns();
            final int startHour = (int) (this.startTime / 60);
            int endHour = (int) (this.endTime / 60);

            // If the end point aligns on a cell boundary then count it as
            // ending in the previous cell so that we don't cross the border
            // between hours.
            if (endHour * 60 == this.endTime) {
                    endHour -= 1;
            }

            this.top = top;
            this.top += (int) (this.startTime * cellMinuteHeight);
            this.top += startHour * mHourGap;

            this.bottom = top;
            this.bottom += (int) (this.endTime * cellMinuteHeight);
            this.bottom += endHour * this.mHourGap;

            // Make the rectangle be at least mMinEventHeight pixels high
            if (this.bottom < this.top + mMinEventHeight) {
            	this.bottom = this.top + mMinEventHeight;
            }

            final float colWidth = (float) (cellWidth - 2 * mCellMargin)
                            / (float) maxCols;
            this.left = left + mCellMargin + col * colWidth;
            this.right = this.left + colWidth;
            return true;
    }
    
	/**
	 * Returns true if this event intersects the selection region.
	 */
	boolean eventIntersectsSelection(SessionGeometry event, Rect selection) {
		if (event.left < selection.right && event.right >= selection.left
				&& event.top < selection.bottom && event.bottom >= selection.top) {
			return true;
		}
		return false;
	}

	public int getColumn() {
		return mColumn;
	}

	public int getMaxColumns() {
		return mMaxColumns;
	}

	/**
	 * Computes the distance from the given point to the given event.
	 */
	float pointToEvent(float x, float y, SessionGeometry event) {
		final float left = event.left;
		final float right = event.right;
		final float top = event.top;
		final float bottom = event.bottom;

		if (x >= left) {
			if (x <= right) {
				if (y >= top) {
					if (y <= bottom) {
						// x,y is inside the event rectangle
						return 0f;
					}
					// x,y is below the event rectangle
					return y - bottom;
				}
				// x,y is above the event rectangle
				return top - y;
			}

			// x > right
			final float dx = x - right;
			if (y < top) {
				// the upper right corner
				final float dy = top - y;
				return (float) Math.sqrt(dx * dx + dy * dy);
			}
			if (y > bottom) {
				// the lower right corner
				final float dy = y - bottom;
				return (float) Math.sqrt(dx * dx + dy * dy);
			}
			// x,y is to the right of the event rectangle
			return dx;
		}
		// x < left
		final float dx = left - x;
		if (y < top) {
			// the upper left corner
			final float dy = top - y;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}
		if (y > bottom) {
			// the lower left corner
			final float dy = y - bottom;
			return (float) Math.sqrt(dx * dx + dy * dy);
		}
		// x,y is to the left of the event rectangle
		return dx;
	}

	void setCellMargin(int cellMargin) {
		mCellMargin = cellMargin;
	}

	public void setColumn(int column) {
		mColumn = column;
	}

	void setHourGap(float gap) {
		mHourGap = gap;
	}

	void setHourHeight(float height) {
		mMinuteHeight = height / 60.0f;
	}

	public void setMaxColumns(int maxColumns) {
		mMaxColumns = maxColumns;
	}

	void setMinEventHeight(float height) {
		mMinEventHeight = height;
	}

}
