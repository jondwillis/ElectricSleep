package com.androsz.electricsleepbeta.preference;

import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Build.VERSION;
import android.support.v4.app.SherlockPreferenceActivity;
import android.support.v4.view.MenuItem;
import android.widget.ListView;

import com.androsz.electricsleepbeta.app.AnalyticActivity;
import com.androsz.electricsleepbeta.app.HostActivity;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public abstract class HostPreferenceActivity extends SherlockPreferenceActivity implements GoogleAnalyticsTrackerHelper {

	protected static final int NO_CONTENT = 0;

	protected abstract int getContentAreaLayoutId();

	protected abstract String getPreferencesName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ListView lvw = getListView();
		lvw.setBackgroundColor(Color.BLACK);

		//if (getPreferencesName() != null) {
		//	getPreferenceManager().setSharedPreferencesName(getPreferencesName());
		//}

		if (getContentAreaLayoutId() != NO_CONTENT) {
			addPreferencesFromResource(getContentAreaLayoutId());
		}

		HostActivity.prepareActionBar(this);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return (true);
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		GoogleAnalyticsSessionHelper.getInstance(AnalyticActivity.KEY, getApplication()).onStartSession();
		
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
