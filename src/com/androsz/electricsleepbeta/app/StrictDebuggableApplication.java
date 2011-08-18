package com.androsz.electricsleepbeta.app;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.os.StrictMode;
import android.util.Log;

public class StrictDebuggableApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			// check if android:debuggable is set to true
			int applicationFlags = getApplicationInfo().flags;
			if ((applicationFlags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
				try {
					// api level 11+
					StrictMode
							.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
									.detectAll().penaltyFlashScreen()
									.penaltyLog().build());
				} catch (Throwable throwable) {
					// api level 10-
					StrictMode
							.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
									.detectAll().penaltyLog().penaltyDialog()
									.build());
				}
				StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
						.detectAll().penaltyLog().penaltyDeath().build());
			}
		} catch (Throwable throwable) {
		}
	}
}
