/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
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
package com.androsz.electricsleep.achartengine.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * A series for the category charts like the bar and pie ones.
 */
public class CategorySeries implements Serializable {
	/** The series title. */
	private final String mTitle;
	/** The series categories. */
	private final List<String> mCategories = new ArrayList<String>();
	/** The series values. */
	private final List<Double> mValues = new ArrayList<Double>();

	/**
	 * Builds a new category series.
	 * 
	 * @param title
	 *            the series title
	 */
	public CategorySeries(String title) {
		mTitle = title;
	}

	/**
	 * Adds a new value to the series
	 * 
	 * @param value
	 *            the new value
	 */
	public void add(double value) {
		add(mCategories.size() + "", value);
	}

	/**
	 * Adds a new value to the series.
	 * 
	 * @param category
	 *            the category
	 * @param value
	 *            the new value
	 */
	public void add(String category, double value) {
		mCategories.add(category);
		mValues.add(value);
	}

	/**
	 * Removes all the existing values from the series.
	 */
	public void clear() {
		mCategories.clear();
		mValues.clear();
	}

	/**
	 * Returns the category name at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the category name at the index
	 */
	public String getCategory(int index) {
		return mCategories.get(index);
	}

	/**
	 * Returns the series item count.
	 * 
	 * @return the series item count
	 */
	public int getItemCount() {
		return mCategories.size();
	}

	/**
	 * Returns the series title.
	 * 
	 * @return the series title
	 */
	public String getTitle() {
		return mTitle;
	}

	/**
	 * Returns the value at the specified index.
	 * 
	 * @param index
	 *            the index
	 * @return the value at the index
	 */
	public double getValue(int index) {
		return mValues.get(index);
	}

	/**
	 * Removes an existing value from the series.
	 * 
	 * @param index
	 *            the index in the series of the value to remove
	 */
	public void remove(int index) {
		mCategories.remove(index);
		mValues.remove(index);
	}

	/**
	 * Transforms the category series to an XY series.
	 * 
	 * @return the XY series
	 */
	public XYSeries toXYSeries() {
		final XYSeries xySeries = new XYSeries(mTitle);
		int k = 0;
		for (final double value : mValues) {
			xySeries.add(++k, value);
		}
		return xySeries;
	}
}
