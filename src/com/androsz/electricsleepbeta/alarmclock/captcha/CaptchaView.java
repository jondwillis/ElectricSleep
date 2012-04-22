package com.androsz.electricsleepbeta.alarmclock.captcha;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

public abstract class CaptchaView extends View {

	public CaptchaView(Context context) {
		this(context, null);
	}
	
	public CaptchaView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CaptchaView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}
