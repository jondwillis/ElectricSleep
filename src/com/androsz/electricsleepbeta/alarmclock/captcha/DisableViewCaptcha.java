package com.androsz.electricsleepbeta.alarmclock.captcha;

import android.view.View;

public abstract class DisableViewCaptcha implements ICaptchaProcessor {

	public View mDisabledView;
	
	public DisableViewCaptcha(View viewToDisable)
	{
		mDisabledView = viewToDisable;
		mDisabledView.setEnabled(false);
	}
}
