package com.androsz.electricsleepbeta.util;

import android.content.Context;

/**
 * Math utility functions.
 */
public final class MathUtils {
	public static int calculatePxFromDip(Context context, int sizeInDip) {
		final int dip = (int) (sizeInDip
				* context.getResources().getDisplayMetrics().density + 0.5f);
		return dip;
	}
	/**
	 * Equivalent to Math.max(low, Math.min(high, amount));
	 */
	public static float constrain(final float amount, final float low,
			final float high) {
		return amount < low ? low : amount > high ? high : amount;
	}

	/**
	 * Equivalent to Math.max(low, Math.min(high, amount));
	 */
	public static int constrain(final int amount, final int low, final int high) {
		return amount < low ? low : amount > high ? high : amount;
	}

	private MathUtils() {
	}
}