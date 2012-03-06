package com.androsz.electricsleepbeta.app;

import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class LayoutFragment extends Fragment implements GoogleAnalyticsTrackerHelper {

	public void trackEvent(final String label, final int value) {
		GoogleAnalyticsSessionHelper.trackEvent(label, value);
	}

	public void trackPageView(final String pageUrl) {
		GoogleAnalyticsSessionHelper.trackPageView(pageUrl);
	}
	
	public static LayoutFragment newInstance(final int layoutId) {
		return new LayoutFragment() {
			@Override
			public int getLayoutResourceId() {
				return layoutId;
			}
		};
	}
	
	public abstract int getLayoutResourceId();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(getLayoutResourceId(), container, false);
	}
}