package com.androsz.electricsleepbeta.app;

import android.app.Activity;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public abstract class AnalyticFragment extends Fragment {

	protected abstract int getContentAreaLayoutId();

	public abstract void onClick(View v);

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}
	
	@Override
	public void onStart()
	{
		super.onStart();
		// Need to do this for every activity that uses google analytics
		GoogleAnalyticsSessionHelper.getInstance(AnalyticActivity.KEY, getActivity().getApplication())
				.onStartSession();
		
		String versionName = "?";
		final Activity a = getActivity();
		try {
			versionName = a.getPackageManager().getPackageInfo(a.getPackageName(), 0).versionName;
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}

		GoogleAnalyticsTracker.getInstance().setProductVersion(a.getPackageName(), versionName);

		
		// Example of how to track a pageview event
		trackPageView(getClass().getSimpleName());
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		
		GoogleAnalyticsTracker.getInstance().dispatch();

		GoogleAnalyticsSessionHelper.getExistingInstance().onStopSession();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(getContentAreaLayoutId(), container, false);

		view.setBackgroundColor(Color.BLACK);
		//view.setBackgroundResource(R.drawable.gradient_background_vert);
		return view;
	}

	protected void trackEvent(final String label, final int value) {
		GoogleAnalyticsSessionHelper.trackEvent(label, value);
	}

	protected void trackPageView(final String pageUrl) {
		GoogleAnalyticsSessionHelper.trackPageView(pageUrl);
	}	
}
