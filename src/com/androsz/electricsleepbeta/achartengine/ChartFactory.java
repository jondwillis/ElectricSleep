package com.androsz.electricsleepbeta.achartengine;

///**
// * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
// *  
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *  
// *      http://www.apache.org/licenses/LICENSE-2.0
// *  
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.androsz.electricsleepbeta.achartengine;
//
//import android.content.Context;
//import android.content.Intent;
//
//import com.androsz.electricsleepbeta.achartengine.chart.BarChart;
//import com.androsz.electricsleepbeta.achartengine.chart.BarChart.Type;
//import com.androsz.electricsleepbeta.achartengine.chart.BubbleChart;
//import com.androsz.electricsleepbeta.achartengine.chart.DoughnutChart;
//import com.androsz.electricsleepbeta.achartengine.chart.LineChart;
//import com.androsz.electricsleepbeta.achartengine.chart.PieChart;
//import com.androsz.electricsleepbeta.achartengine.chart.RangeBarChart;
//import com.androsz.electricsleepbeta.achartengine.chart.ScatterChart;
//import com.androsz.electricsleepbeta.achartengine.chart.TimeChart;
//import com.androsz.electricsleepbeta.achartengine.chart.XYChart;
//import com.androsz.electricsleepbeta.achartengine.model.CategorySeries;
//import com.androsz.electricsleepbeta.achartengine.model.MultipleCategorySeries;
//import com.androsz.electricsleepbeta.achartengine.model.XYMultipleSeriesDataset;
//import com.androsz.electricsleepbeta.achartengine.renderer.DefaultRenderer;
//import com.androsz.electricsleepbeta.achartengine.renderer.XYMultipleSeriesRenderer;
//import com.androsz.electricsleepbeta.widget.SleepChart;
//
///**
// * Utility methods for creating chart views or intents.
// */
//public class ChartFactory {
//	/** The key for the chart data. */
//	public static final String CHART = "chart";
//
//	/** The key for the chart graphical activity title. */
//	public static final String TITLE = "title";
//
//	private static boolean checkMultipleSeriesItems(
//			final MultipleCategorySeries dataset, final int value) {
//		final int count = dataset.getCategoriesCount();
//		boolean equal = true;
//		for (int k = 0; k < count && equal; k++) {
//			equal = dataset.getValues(k).length == dataset.getTitles(k).length;
//		}
//		return equal;
//	}
//
//	/**
//	 * Checks the validity of the dataset and renderer parameters.
//	 * 
//	 * @param dataset
//	 *            the category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	private static void checkParameters(final CategorySeries dataset,
//			final DefaultRenderer renderer) {
//		if (dataset == null || renderer == null
//				|| dataset.getItemCount() != renderer.getSeriesRendererCount()) {
//			throw new IllegalArgumentException(
//					"Dataset and renderer should be not null and the dataset number of items should be equal to the number of series renderers");
//		}
//	}
//
//	/**
//	 * Checks the validity of the dataset and renderer parameters.
//	 * 
//	 * @param dataset
//	 *            the category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	private static void checkParameters(final MultipleCategorySeries dataset,
//			final DefaultRenderer renderer) {
//		if (dataset == null
//				|| renderer == null
//				|| !checkMultipleSeriesItems(dataset,
//						renderer.getSeriesRendererCount())) {
//			throw new IllegalArgumentException(
//					"Titles and values should be not null and the dataset number of items should be equal to the number of series renderers");
//		}
//	}
//
//	/**
//	 * Checks the validity of the dataset and renderer parameters.
//	 * 
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	private static void checkParameters(final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		if (dataset == null
//				|| renderer == null
//				|| dataset.getSeriesCount() != renderer
//						.getSeriesRendererCount()) {
//			throw new IllegalArgumentException(
//					"Dataset and renderer should be not null and should have the same number of series");
//		}
//	}
//
//	/**
//	 * Creates a bar chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param type
//	 *            the bar chart type
//	 * @return a bar chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getBarChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final Type type) {
//		return getBarChartIntent(context, dataset, renderer, type, "");
//	}
//
//	/**
//	 * Creates a bar chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param type
//	 *            the bar chart type
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a bar chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getBarChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final Type type,
//			final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final BarChart chart = new BarChart(dataset, renderer, type);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a bar chart view.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param type
//	 *            the bar chart type
//	 * @return a bar chart graphical view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final ChartView getBarChartView(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final Type type) {
//		checkParameters(dataset, renderer);
//		final XYChart chart = new BarChart(dataset, renderer, type);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * Creates a bubble chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @return a scatter chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getBubbleChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		return getBubbleChartIntent(context, dataset, renderer, "");
//	}
//
//	/**
//	 * Creates a bubble chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a scatter chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getBubbleChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final XYChart chart = new BubbleChart(dataset, renderer);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a bubble chart view.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @return a scatter chart graphical view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final ChartView getBubbleChartView(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		checkParameters(dataset, renderer);
//		final XYChart chart = new BubbleChart(dataset, renderer);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * Creates a doughnut chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a pie chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	public static final Intent getDoughnutChartIntent(final Context context,
//			final MultipleCategorySeries dataset,
//			final DefaultRenderer renderer, final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final DoughnutChart chart = new DoughnutChart(dataset, renderer);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a doughnut chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @return a pie chart view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	public static final ChartView getDoughnutChartView(
//			final Context context, final MultipleCategorySeries dataset,
//			final DefaultRenderer renderer) {
//		checkParameters(dataset, renderer);
//		final DoughnutChart chart = new DoughnutChart(dataset, renderer);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * 
//	 * Creates a line chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @return a line chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getLineChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		return getLineChartIntent(context, dataset, renderer, "");
//	}
//
//	/**
//	 * Creates a line chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param activityTitle
//	 *            the graphical chart activity title. If this is null, then the
//	 *            title bar will be hidden. If a blank title is passed in, then
//	 *            the title bar will be the default. Pass in any other string to
//	 *            set a custom title.
//	 * @return a line chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getLineChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final XYChart chart = new LineChart(dataset, renderer);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a line chart view.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @return a line chart graphical view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final ChartView getLineChartView(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		checkParameters(dataset, renderer);
//		final XYChart chart = new LineChart(dataset, renderer);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * Creates a pie chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @return a pie chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	public static final Intent getPieChartIntent(final Context context,
//			final CategorySeries dataset, final DefaultRenderer renderer) {
//		return getPieChartIntent(context, dataset, renderer, "");
//	}
//
//	/**
//	 * Creates a pie chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a pie chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	public static final Intent getPieChartIntent(final Context context,
//			final CategorySeries dataset, final DefaultRenderer renderer,
//			final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final PieChart chart = new PieChart(dataset, renderer);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a pie chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the category series dataset (cannot be null)
//	 * @param renderer
//	 *            the series renderer (cannot be null)
//	 * @return a pie chart view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset
//	 *             number of items is different than the number of series
//	 *             renderers
//	 */
//	public static final ChartView getPieChartView(final Context context,
//			final CategorySeries dataset, final DefaultRenderer renderer) {
//		checkParameters(dataset, renderer);
//		final PieChart chart = new PieChart(dataset, renderer);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * Creates a range bar chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param type
//	 *            the range bar chart type
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a range bar chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getRangeBarChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final Type type,
//			final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final RangeBarChart chart = new RangeBarChart(dataset, renderer, type);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a range bar chart view.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param type
//	 *            the range bar chart type
//	 * @return a bar chart graphical view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final ChartView getRangeBarChartView(
//			final Context context, final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final Type type) {
//		checkParameters(dataset, renderer);
//		final XYChart chart = new RangeBarChart(dataset, renderer, type);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * Creates a scatter chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @return a scatter chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getScatterChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		return getScatterChartIntent(context, dataset, renderer, "");
//	}
//
//	/**
//	 * Creates a scatter chart intent that can be used to start the graphical
//	 * view activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a scatter chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getScatterChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final XYChart chart = new ScatterChart(dataset, renderer);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a scatter chart view.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @return a scatter chart graphical view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final ChartView getScatterChartView(
//			final Context context, final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer) {
//		checkParameters(dataset, renderer);
//		final XYChart chart = new ScatterChart(dataset, renderer);
//		return new ChartView(context, chart);
//	}
//
//	/**
//	 * Creates a time chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param format
//	 *            the date format pattern to be used for displaying the X axis
//	 *            date labels. If null, a default appropriate format will be
//	 *            used.
//	 * @return a time chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getTimeChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final String format) {
//		return getTimeChartIntent(context, dataset, renderer, format, "");
//	}
//
//	/**
//	 * Creates a time chart intent that can be used to start the graphical view
//	 * activity.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param format
//	 *            the date format pattern to be used for displaying the X axis
//	 *            date labels. If null, a default appropriate format will be
//	 *            used
//	 * @param activityTitle
//	 *            the graphical chart activity title
//	 * @return a time chart intent
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final Intent getTimeChartIntent(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final String format,
//			final String activityTitle) {
//		checkParameters(dataset, renderer);
//		final Intent intent = new Intent(context, GraphicalActivity.class);
//		final TimeChart chart = new TimeChart(dataset, renderer);
//		chart.setDateFormat(format);
//		intent.putExtra(CHART, chart);
//		intent.putExtra(TITLE, activityTitle);
//		return intent;
//	}
//
//	/**
//	 * Creates a time chart view.
//	 * 
//	 * @param context
//	 *            the context
//	 * @param dataset
//	 *            the multiple series dataset (cannot be null)
//	 * @param renderer
//	 *            the multiple series renderer (cannot be null)
//	 * @param format
//	 *            the date format pattern to be used for displaying the X axis
//	 *            date labels. If null, a default appropriate format will be
//	 *            used.
//	 * @return a time chart graphical view
//	 * @throws IllegalArgumentException
//	 *             if dataset is null or renderer is null or if the dataset and
//	 *             the renderer don't include the same number of series
//	 */
//	public static final ChartView getTimeChartView(final Context context,
//			final XYMultipleSeriesDataset dataset,
//			final XYMultipleSeriesRenderer renderer, final String format) {
//		checkParameters(dataset, renderer);
//		final TimeChart chart = new TimeChart(dataset, renderer);
//		chart.setDateFormat(format);
//		return new ChartView(context, chart);
//	}
//
//	private ChartFactory() {
//		// empty for now
//	}
//
// }
