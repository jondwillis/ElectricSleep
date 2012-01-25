package com.androsz.electricsleepbeta.app;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

public abstract class AnalyticFragment extends Fragment implements GoogleAnalyticsTrackerHelper {

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
		
		GoogleAnalyticsSessionHelper.getInstance(AnalyticActivity.KEY, getActivity().getApplication())
				.onStartSession();
		
		trackPageView(getClass().getSimpleName());
	}
	
	@Override
	public void onStop()
	{
		super.onStop();

		GoogleAnalyticsSessionHelper.getExistingInstance().onStopSession();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(getContentAreaLayoutId(), container, false);

		view.setBackgroundColor(getResources().getColor(R.color.background));
		//view.setBackgroundResource(R.drawable.gradient_background_vert);
		return view;
	}

	public void trackEvent(final String label, final int value) {
		GoogleAnalyticsSessionHelper.trackEvent(label, value);
	}

	public void trackPageView(final String pageUrl) {
		GoogleAnalyticsSessionHelper.trackPageView(pageUrl);
	}	
}
