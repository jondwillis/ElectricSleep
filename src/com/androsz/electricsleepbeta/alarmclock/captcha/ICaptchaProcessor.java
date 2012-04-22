package com.androsz.electricsleepbeta.alarmclock.captcha;

import android.content.Context;

/**
 * 
 * An interface that should be implemented by things that need to create and check CAPTCHAs
 * 
 * @author jon
 *
 */
public interface ICaptchaProcessor {
	/**
	 * @param context
	 * @return 
	 */
	public void onGenerate(Context context);
	public boolean onCheck();
}
