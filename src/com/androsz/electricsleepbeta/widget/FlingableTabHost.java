package com.androsz.electricsleepbeta.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.util.MathUtils;

public class FlingableTabHost extends TabHost {
	GestureDetector mGestureDetector;

	Animation mRightInAnimation;
	Animation mRightOutAnimation;
	Animation mLeftInAnimation;
	Animation mLeftOutAnimation;

	public FlingableTabHost(final Context context, final AttributeSet attrs) {
		super(context, attrs);

		this.setBackgroundColor(android.R.color.transparent);

		mRightInAnimation = AnimationUtils.loadAnimation(context,
				R.anim.slide_right_in);
		mRightOutAnimation = AnimationUtils.loadAnimation(context,
				R.anim.slide_right_out);
		mLeftInAnimation = AnimationUtils.loadAnimation(context,
				R.anim.slide_left_in);
		mLeftOutAnimation = AnimationUtils.loadAnimation(context,
				R.anim.slide_left_out);

		final int minScaledFlingVelocity = ViewConfiguration.get(context)
				.getScaledMinimumFlingVelocity(); // 5 = fudge by
														// experimentation

		mGestureDetector = new GestureDetector(
				new GestureDetector.SimpleOnGestureListener() {
					@Override
					public boolean onFling(final MotionEvent e1,
							final MotionEvent e2, final float velocityX,
							final float velocityY) {

						final int tabCount = getTabWidget().getTabCount();
						final int currentTab = getCurrentTab();
						if (Math.abs(velocityX) > minScaledFlingVelocity
								&& Math.abs(velocityY) < minScaledFlingVelocity*2) {

							final boolean right = velocityX < 0;
							final int newTab = MathUtils.constrain(currentTab
									+ (right ? 1 : -1), 0, tabCount - 1);
							if (newTab != currentTab) {
								// Somewhat hacky, depends on current
								// implementation of TabHost:
								// http://android.git.kernel.org/?p=platform/frameworks/base.git;a=blob;
								// f=core/java/android/widget/TabHost.java
								final View currentView = getCurrentView();
								setCurrentTab(newTab);
								final View newView = getCurrentView();

								newView.startAnimation(right ? mRightInAnimation
										: mLeftInAnimation);
								currentView
										.startAnimation(right ? mRightOutAnimation
												: mLeftOutAnimation);
							}
						}
						return super.onFling(e1, e2, velocityX, velocityY);
					}
				});
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent ev) {
		if (mGestureDetector.onTouchEvent(ev))
			return true;
		return super.onInterceptTouchEvent(ev);
	}
}
