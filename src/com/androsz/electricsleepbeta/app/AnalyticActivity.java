package com.androsz.electricsleepbeta.app;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

public abstract class AnalyticActivity extends SherlockFragmentActivity implements GoogleAnalyticsTrackerHelper {

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
