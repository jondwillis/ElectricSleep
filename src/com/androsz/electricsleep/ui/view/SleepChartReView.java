package com.androsz.electricsleep.ui.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

public class SleepChartReView extends SleepChartView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5283117631549734626L;

	public SleepChartReView(final Context context) {
		super(context);
	}

	@Override
	protected void redraw(final int min, final int max, final int alarm) {
		if (makesSense()) {
			final int count = xySeriesMovement.mY.size();
			int numberOfDesiredGroupedPoints = 100;
			numberOfDesiredGroupedPoints = count > numberOfDesiredGroupedPoints ? numberOfDesiredGroupedPoints
					: count;

			if (numberOfDesiredGroupedPoints != count) {
				final int pointsPerGroup = count / numberOfDesiredGroupedPoints
						+ 1;
				final List<Double> lessDetailedX = new ArrayList<Double>(
						numberOfDesiredGroupedPoints);
				final List<Double> lessDetailedY = new ArrayList<Double>(
						numberOfDesiredGroupedPoints);
				int numberOfPointsInThisGroup = pointsPerGroup;
				double averageYForThisGroup = 0;
				for (int i = 0; i < numberOfDesiredGroupedPoints; i++) {
					averageYForThisGroup = 0;
					final int startIndexForThisGroup = i * pointsPerGroup;
					for (int j = 0; j < pointsPerGroup; j++) {
						try {
							averageYForThisGroup += xySeriesMovement.mY
									.get(startIndexForThisGroup + j);
						} catch (final IndexOutOfBoundsException ioobe) {
							// lower the number of points
							// (and signify that we are done)
							numberOfPointsInThisGroup = j - 1;
							break;
						}
					}
					// averageXForThisGroup /= numberOfPointsInThisGroup;
					averageYForThisGroup /= numberOfPointsInThisGroup;
					if (numberOfPointsInThisGroup < pointsPerGroup) {
						// we are done
						final int lastIndex = xySeriesMovement.mX.size() - 1;
						lessDetailedX.add(xySeriesMovement.mX.get(lastIndex));
						lessDetailedY.add(xySeriesMovement.mY.get(lastIndex));
						xySeriesMovement.mX = lessDetailedX;
						xySeriesMovement.mY = lessDetailedY;
						break;
					} else {
						lessDetailedX.add(xySeriesMovement.mX
								.get(startIndexForThisGroup));
						lessDetailedY.add(averageYForThisGroup);
					}
				}
			}
		}
		super.redraw(min, max, alarm);
	}
}
