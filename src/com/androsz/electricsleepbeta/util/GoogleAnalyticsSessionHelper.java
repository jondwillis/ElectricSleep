package com.androsz.electricsleepbeta.util;

import android.app.Application;

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

	public void incrementSession() {
		if (sessionCount == 0) {
			GoogleAnalyticsTracker.getInstance().startNewSession(key, appContext);
		}

		sessionCount++;
	}

	public void decrementSession() {
		if (sessionCount > 0) {
			sessionCount--;
		} else {
			GoogleAnalyticsTracker.getInstance().stopSession();
		}
	}
}
