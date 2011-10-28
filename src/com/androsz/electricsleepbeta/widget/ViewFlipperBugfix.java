package com.androsz.electricsleepbeta.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class ViewFlipperBugfix extends ViewFlipper {

	public ViewFlipperBugfix(final Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public ViewFlipperBugfix(final Context context, final AttributeSet as) {
		super(context, as);
		// TODO Auto-generated constructor stub
	}

	// hack to prevent a crash on android 2.1 - 2.3.x
	// http://code.google.com/p/android/issues/detail?id=6191#c8
	@Override
	public void onDetachedFromWindow() {
		try {
			super.onDetachedFromWindow();
		} catch (final IllegalArgumentException e) {
			stopFlipping();
		}
	}

}
