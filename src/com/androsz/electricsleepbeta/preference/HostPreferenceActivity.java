package com.androsz.electricsleepbeta.preference;

import java.util.List;

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.SherlockPreferenceActivity;
import android.support.v4.view.MenuItem;
import android.widget.ListView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.AnalyticActivity;
import com.androsz.electricsleepbeta.app.HostActivity;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

public abstract class HostPreferenceActivity extends SherlockPreferenceActivity
		implements GoogleAnalyticsTrackerHelper {

	protected abstract int getContentAreaLayoutId();

	protected abstract int getHeadersResourceId();

	protected static final int NO_HEADERS = 0;

	/**
	 * Populate the activity with the top-level headers.
	 */
	@Override
	public void onBuildHeaders(List<Header> target) {
		if (getHeadersResourceId() != NO_HEADERS) {
			loadHeadersFromResource(getHeadersResourceId(), target);
		}
	}

	protected boolean getNeedToLoadOldStylePreferences() {
		return Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB
				|| getHeadersResourceId() == NO_HEADERS;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ListView lvw = getListView();
		this.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.background_dark)));
		lvw.setBackgroundColor(getResources().getColor(R.color.background_dark));

		//if pre-honeycomb, don't try to use fragments and just load the old-style prefs
		if (getNeedToLoadOldStylePreferences()) {
			addPreferencesFromResource(getContentAreaLayoutId());
			//TODO is this needed anymore? it is inconsistent between API levels
			//if (getPreferencesName() != null) {
			//	getPreferenceManager().setSharedPreferencesName(getPreferencesName());
			//}
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
	protected void onStart() {
		super.onStart();
		GoogleAnalyticsSessionHelper.getInstance(AnalyticActivity.KEY,
				getApplication()).onStartSession();

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
