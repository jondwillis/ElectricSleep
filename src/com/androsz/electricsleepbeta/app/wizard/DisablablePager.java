package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

public class DisablablePager extends ViewPager {

	private boolean pagingEnabled = true;
	private Toast pagingDisabledToast;

	public DisablablePager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setPagingEnabled(boolean enabled) {
		pagingEnabled = enabled;
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (pagingEnabled) {
			return super.onInterceptTouchEvent(ev);
		} else {
			return true;
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (pagingEnabled) {
			return super.onTouchEvent(ev);
		} else {
			return true;
		}
	}
}