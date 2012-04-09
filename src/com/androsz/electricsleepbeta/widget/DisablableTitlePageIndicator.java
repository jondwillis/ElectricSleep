package com.androsz.electricsleepbeta.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.viewpagerindicator.TitlePageIndicator;

public class DisablableTitlePageIndicator extends TitlePageIndicator {

	private boolean enabled = true;
	
	public DisablableTitlePageIndicator(Context context) {
		this(context, null);
	}

	public DisablableTitlePageIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DisablableTitlePageIndicator(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (this.enabled) {
			return super.onTouchEvent(event);
		}

		return false;
	}

	public void setPagingEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
