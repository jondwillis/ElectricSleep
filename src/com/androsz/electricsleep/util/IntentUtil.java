package com.androsz.electricsleep.util;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class IntentUtil {
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 * 
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 * 
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean doesIntentHaveReceivers(final Context context,
			final String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		final List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent, 0);
		return list.size() > 0;
	}
}
