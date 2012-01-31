package com.androsz.electricsleepbeta.app;

import android.support.v4.app.FragmentActivity;

import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

public abstract class AnalyticActivity extends FragmentActivity implements GoogleAnalyticsTrackerHelper {

	public static final String KEY = "UA-19363335-1";

	@Override
	protected void onStart()
	{
		super.onStart();

		GoogleAnalyticsSessionHelper.getInstance(KEY, getApplication()).onStartSession();
        trackPageView(getClass().getSimpleName());
	}

	@Override
	protected void onStop() {
		super.onStop();
		GoogleAnalyticsSessionHelper.getExistingInstance().onStopSession();
	}

	public void trackEvent(final String label, final int value) {
		GoogleAnalyticsSessionHelper.trackEvent(label, value);
	}

	public void trackPageView(final String pageUrl) {
		GoogleAnalyticsSessionHelper.trackPageView(pageUrl);
	}
}
