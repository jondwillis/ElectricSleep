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

import android.graphics.Rect;

import com.androsz.electricsleepbeta.db.SleepSession;

public class EventGeometry {
	/* package */static final int MINUTES_PER_HOUR = 60;
	/* package */static final int MINUTES_PER_DAY = MINUTES_PER_HOUR * 24;
	// This is the space from the grid line to the event rectangle.
	private int mCellMargin = 0;

	private float mHourGap;

	private float mMinEventHeight;
	private float mMinuteHeight;

	// Computes the rectangle coordinates of the given event on the screen.
	// Returns true if the rectangle is visible on the screen.
	boolean computeEventRect(int date, int left, int top, int cellWidth,
			SleepSession event) {

		final float cellMinuteHeight = mMinuteHeight;
		final int startDay = event.getStartJulianDay();
		final int endDay = event.getEndJulianDay();

		if (startDay > date || endDay < date) {
			return false;
		}

		long startTime = event.getStartTimeOfDay();
		long endTime = event.getEndTimeOfDay();

		// If the event started on a previous day, then show it starting
		// at the beginning of this day.
		if (startDay < date) {
			startTime = 0;
		}

		// If the event ends on a future day, then show it extending to
		// the end of this day.
		if (endDay > date) {
			endTime = MINUTES_PER_DAY;
		}

		final int col = event.getColumn();
		final int maxCols = event.getMaxColumns();
		final int startHour = (int) (startTime / 60);
		int endHour = (int) (endTime / 60);

		// If the end point aligns on a cell boundary then count it as
		// ending in the previous cell so that we don't cross the border
		// between hours.
		if (endHour * 60 == endTime) {
			endHour -= 1;
		}

		event.top = top;
		event.top += (int) (startTime * cellMinuteHeight);
		event.top += startHour * mHourGap;

		event.bottom = top;
		event.bottom += (int) (endTime * cellMinuteHeight);
		event.bottom += endHour * mHourGap;

		// Make the rectangle be at least mMinEventHeight pixels high
		if (event.bottom < event.top + mMinEventHeight) {
			event.bottom = event.top + mMinEventHeight;
		}

		final float colWidth = (float) (cellWidth - 2 * mCellMargin)
				/ (float) maxCols;
		event.left = left + mCellMargin + col * colWidth;
		event.right = event.left + colWidth;
		return true;
	}

	/**
	 * Returns true if this event intersects the selection region.
	 */
	boolean eventIntersectsSelection(SleepSession event, Rect selection) {
		if (event.left < selection.right && event.right >= selection.left
				&& event.top < selection.bottom
				&& event.bottom >= selection.top) {
			return true;
		}
		return false;
	}

	/**
	 * Computes the distance from the given point to the given event.
	 */
	float pointToEvent(float x, float y, SleepSession event) {
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

	void setHourGap(float gap) {
		mHourGap = gap;
	}

	void setHourHeight(float height) {
		mMinuteHeight = height / 60.0f;
	}

	void setMinEventHeight(float height) {
		mMinEventHeight = height;
	}
}
