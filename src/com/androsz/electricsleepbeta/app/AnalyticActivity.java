package com.androsz.electricsleepbeta.app;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.MenuItem;

import com.androsz.electricsleepbeta.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public abstract class AnalyticActivity extends FragmentActivity {
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		CharSequence thistit = getTitle();
		CharSequence thattit = item.getTitle();
		int bleh = this.getSupportActionBar().getDisplayOptions()
				& ActionBar.DISPLAY_HOME_AS_UP;
		if (thistit == thattit)
			if (bleh == ActionBar.DISPLAY_HOME_AS_UP)
				finish();
			else if (bleh == 0) {
				//TODO
				//final Intent intent = new Intent(this, ImportActivity.class);
				//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				//startActivity(intent);
			}
		return super.onOptionsItemSelected(item);
	}
	
	private GoogleAnalyticsTracker analytics;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				analytics = GoogleAnalyticsTracker.getInstance();
				analytics.start(getString(R.string.analytics_ua_number), AnalyticActivity.this);
				analytics.trackPageView("/" + getLocalClassName());
				return null;
			}}.execute();
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				final SharedPreferences userPrefs = getPreferences(MODE_PRIVATE);
				//TODO
				boolean analyticsEnabled = userPrefs.getBoolean(getString(R.string.pref_analytics), true);
				if (analyticsEnabled) {
					analytics.dispatch();
				}
				return null;
			}

			protected void onPostExecute(Void analyticsEnabled) {
				analytics.stop();
			}
		}.execute();
	}

	protected void trackEvent(final String label, final int value) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String version = "?";
				try {
					version = getPackageManager().getPackageInfo(getPackageName(),
							0).versionName;
				} catch (final NameNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				final String modelAndApiLevel = Build.MODEL + "-" + VERSION.SDK_INT;
				analytics.trackEvent(version, modelAndApiLevel, label, value);
				return null;
			}
		}.execute();
	}

	protected void trackPageView(final String pageUrl) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				analytics.trackPageView(pageUrl);
				return null;
			}
		}.execute();
	}
}
