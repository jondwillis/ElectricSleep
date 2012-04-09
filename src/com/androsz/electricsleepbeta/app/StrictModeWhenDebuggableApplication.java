package com.androsz.electricsleepbeta.app;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.StrictMode;

public class StrictModeWhenDebuggableApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        GoogleAnalyticsTracker.getInstance().setAnonymizeIp(true);

		final int applicationFlags = getApplicationInfo().flags;
		// is android:debuggable is set to true?
		if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
			//YES
            Log.toggleDebug(true);

            //Send GA stuff to the android log, not the server.
			GoogleAnalyticsTracker.getInstance().setDryRun(true);
			//Who really cares that much about exact location anyway?

			// we can only use StrictMode in Gingerbread and beyond.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {

				// add the default thread policies
				StrictMode.ThreadPolicy.Builder builder = new StrictMode.ThreadPolicy.Builder()
						.detectAll().penaltyLog();

				// add Flash Screen indicator if we are on Honeycomb and beyond.
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
					builder.penaltyFlashScreen();
				}

				StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().detectAll().penaltyLog()
						.build());

				StrictMode.setThreadPolicy(builder.build());
			}
		}
	}
}
