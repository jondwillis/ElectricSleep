package com.androsz.electricsleepbeta.app;

import com.androsz.electricsleepbeta.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Build.VERSION;
import android.view.View;

public abstract class AnalyticFragment extends Fragment {
	protected GoogleAnalyticsTracker analytics;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		final Activity a = getActivity();
		analytics = GoogleAnalyticsTracker.getInstance();
		analytics.start(getString(R.string.analytics_ua_number), a);
		analytics.trackPageView("/" + this.getClass().getCanonicalName());
		
		final boolean DEVELOPER_MODE = (getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) == 0;
		// TODO
		if (DEVELOPER_MODE) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectAll().penaltyFlashScreen().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectAll().penaltyLog().penaltyDeath().build());
		}
	}

	public abstract void onClick(View v);

	@Override
	public void onDestroy() {
		super.onDestroy();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				Activity a = getActivity();
				// final SharedPreferences userPrefs =
				// a.getPreferences(Context.MODE_PRIVATE);
				// TODO
				boolean analyticsEnabled = true;
				// userPrefs.getBoolean(getString(R.string.key_pref_analytics),
				// true);
				if (analyticsEnabled) {
					analytics.dispatch();
				}
				analytics.stop();
				return null;
			}
		}.execute();
	}

	protected void trackEvent(final String label, final int value) {
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String version = "?";
				Activity a = getActivity();
				try {
					version = a.getPackageManager().getPackageInfo(
							a.getPackageName(), 0).versionName;
				} catch (final NameNotFoundException e) {
					e.printStackTrace();
				}
				final String modelAndApiLevel = Build.MODEL + "-"
						+ VERSION.SDK_INT;
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
