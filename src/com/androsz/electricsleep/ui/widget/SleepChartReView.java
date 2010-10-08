package com.androsz.electricsleep.ui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.content.Context;
import android.util.AttributeSet;

public class SleepChartReView extends SleepChartView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5283117631549734626L;

	public SleepChartReView(final Context context) {
		super(context);
	}


	public SleepChartReView(final Context context, AttributeSet as) {
		super(context, as);
	}

	private void getMovingAverage() {
		int N = 10;
		double[] a = new double[N];
		double sum = 0.0;
		Scanner s = new Scanner(System.in);
		for (int i = 1; s.hasNext(); i++) {
			sum -= a[i % N];
			a[i % N] = s.nextDouble();
			sum += a[i % N];
			if (i >= N)
				System.out.print(sum / N + " ");
		}
	}
	
	protected void redraw(final int min, final int max, final int alarm) {
		if (makesSenseToDisplay()) {
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
			}
			super.redraw(min, max, alarm);
		}
	}

	// @Override
	// protected void redraw(final int min, final int max, final int alarm) {
	// if (makesSenseToDisplay()) {
	// final int count = xySeriesMovement.mY.size();
	// int numberOfDesiredGroupedPoints = 25;
	// numberOfDesiredGroupedPoints = count > numberOfDesiredGroupedPoints ?
	// numberOfDesiredGroupedPoints
	// : count;
	//
	// if (numberOfDesiredGroupedPoints != count) {
	// final int pointsPerGroup = count / numberOfDesiredGroupedPoints
	// + 1;
	// final List<Double> lessDetailedX = new ArrayList<Double>(
	// numberOfDesiredGroupedPoints);
	// final List<Double> lessDetailedY = new ArrayList<Double>(
	// numberOfDesiredGroupedPoints);
	// int numberOfPointsInThisGroup = pointsPerGroup;
	// double averageYForThisGroup = 0;
	// for (int i = 0; i < numberOfDesiredGroupedPoints; i++) {
	// averageYForThisGroup = 0;
	// final int startIndexForThisGroup = i * pointsPerGroup;
	// for (int j = 0; j < pointsPerGroup; j++) {
	// try {
	// averageYForThisGroup += xySeriesMovement.mY
	// .get(startIndexForThisGroup + j);
	// } catch (final IndexOutOfBoundsException ioobe) {
	// // lower the number of points
	// // (and signify that we are done)
	// numberOfPointsInThisGroup = j - 1;
	// break;
	// }
	// }
	//
	// averageYForThisGroup /= numberOfPointsInThisGroup;
	// if (numberOfPointsInThisGroup < pointsPerGroup) {
	// // we are done
	// final int lastIndex = xySeriesMovement.mX.size() - 1;
	// lessDetailedX.add(xySeriesMovement.mX.get(lastIndex));
	// lessDetailedY.add(xySeriesMovement.mY.get(lastIndex));
	// xySeriesMovement.mX = lessDetailedX;
	// xySeriesMovement.mY = lessDetailedY;
	// break;
	// } else {
	// lessDetailedX.add(xySeriesMovement.mX
	// .get(startIndexForThisGroup));
	// lessDetailedY.add(averageYForThisGroup);
	// }
	// }
	// }
	// }
	// super.redraw(min, max, alarm);
	// }
}
