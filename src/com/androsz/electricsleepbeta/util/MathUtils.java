package com.androsz.electricsleepbeta.util;

import android.content.Context;

/**
 * Utility functions that, well, have mathematic purposes.
 */
public final class MathUtils {

	public static int calculatePxFromDp(Context context, int sizeInDp) {
		final float px = (int) (sizeInDp * context.getResources().getDisplayMetrics().density + 0.5f);
		return Math.round(px);
	}

	// For fonts!
	public static int calculatePxFromSp(Context context, int sizeInSp) {
		final float px = (int) (sizeInSp * context.getResources().getDisplayMetrics().scaledDensity + 0.5f);
		return Math.round(px);
	}

	/**
	 * Equivalent to Math.max(low, Math.min(high, amount));
	 */
	public static float constrain(final float amount, final float low, final float high) {
		return amount < low ? low : amount > high ? high : amount;
	}

	/**
	 * Equivalent to Math.max(low, Math.min(high, amount));
	 */
	public static int constrain(final int amount, final int low, final int high) {
		return amount < low ? low : amount > high ? high : amount;
	}

	public static int getAbsoluteScreenHeightPx(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	// Cannot instantiate
	private MathUtils() {
	}
}