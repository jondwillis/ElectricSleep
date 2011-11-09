package com.androsz.electricsleepbeta.util;

import android.app.Application;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

/**
 * @author Jon
 * 
 *         Singleton that helps keep GA sessions open only as long as they are
 *         needed
 */
public class GoogleAnalyticsSessionHelper {

	private static GoogleAnalyticsSessionHelper INSTANCE;

	private final String key;
	private final Application appContext;
	private int sessionCount;

	private GoogleAnalyticsSessionHelper(String key, Application appContext) {
		this.key = key;
		this.appContext = appContext;
		this.sessionCount = 0;
	}

	public static GoogleAnalyticsSessionHelper getInstance(String key, Application appContext) {
		// Create a new instance if it is not cached. Otherwise, use the
		// cached one.
		if (INSTANCE == null) {
			INSTANCE = new GoogleAnalyticsSessionHelper(key, appContext);
		}
		return INSTANCE;
	}

	public static GoogleAnalyticsSessionHelper getExistingInstance() {
		return INSTANCE;
	}

	public void onStartSession() {
		if (sessionCount == 0) {
			GoogleAnalyticsTracker.getInstance().startNewSession(key, appContext);
		}

		sessionCount++;
	}

	public void onStopSession() {
		sessionCount--;
		if(sessionCount < 1)
		{
			sessionCount = 0;
			GoogleAnalyticsTracker.getInstance().stopSession();
		}
	}

	public static void trackEvent(final String label, final int value) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					GoogleAnalyticsTracker.getInstance().trackEvent(
							Integer.toString(VERSION.SDK_INT), Build.MODEL, label, value);
				} catch (final Exception whocares) {
				}
				return null;
			}
		}.execute();
	}
	
	public static void trackPageView(final String pageUrl) {
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				try {
					GoogleAnalyticsTracker.getInstance().trackPageView(pageUrl);
				} catch (final Exception whocares) {
				}
				return null;
			}
		}.execute();
	}
}
