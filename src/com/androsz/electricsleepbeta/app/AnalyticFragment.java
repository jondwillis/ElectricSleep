package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

public abstract class AnalyticFragment extends Fragment implements GoogleAnalyticsTrackerHelper {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
    }

	@Override
	public void onStart() {
		super.onStart();
        GoogleAnalyticsSessionHelper.getInstance(AnalyticActivity.KEY, getActivity().getApplication())
				.onStartSession();
        trackPageView(getClass().getSimpleName());
	}

	@Override
	public void onStop() {
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
