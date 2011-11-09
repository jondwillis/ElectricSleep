package com.androsz.electricsleepbeta.app;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public abstract class AnalyticActivity extends FragmentActivity {

	public static final String KEY = "UA-19363335-1";

	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalyticsSessionHelper.getInstance(KEY, getApplication()).onStartSession();

		String versionName = "?";
		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}

		GoogleAnalyticsTracker.getInstance().setProductVersion(getPackageName(), versionName);

		// I have no idea...
		GoogleAnalyticsTracker.getInstance().setCustomVar(1, Integer.toString(VERSION.SDK_INT),
				Build.MODEL);
		GoogleAnalyticsTracker.getInstance().setCustomVar(2, versionName,
				Build.MODEL + "-" + Integer.toString(VERSION.SDK_INT));
		
		// Example of how to track a pageview event
		trackPageView(getClass().getSimpleName());
	}
	
	@Override
	protected void onStop() {
		super.onStop();

		GoogleAnalyticsTracker.getInstance().dispatch();

		GoogleAnalyticsSessionHelper.getExistingInstance().onStopSession();
	}

	protected void trackEvent(final String label, final int value) {
		GoogleAnalyticsSessionHelper.trackEvent(label, value);
	}

	protected void trackPageView(final String pageUrl) {
		GoogleAnalyticsSessionHelper.trackPageView(pageUrl);
	}
}
