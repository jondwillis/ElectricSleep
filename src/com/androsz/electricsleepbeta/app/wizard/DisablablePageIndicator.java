package com.androsz.electricsleepbeta.app.wizard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import com.viewpagerindicator.TitlePageIndicator;

class DisablablePageIndicator extends TitlePageIndicator
{

	private boolean pagingEnabled = true;
	private Toast pagingDisabledToast;

	public DisablablePageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	

	public void setPagingEnabled(boolean enabled) {
		pagingEnabled = enabled;
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