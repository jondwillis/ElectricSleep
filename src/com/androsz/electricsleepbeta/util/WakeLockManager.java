package com.androsz.electricsleepbeta.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

/**
 * Manages and simplifies access to multiple WakeLocks that can be concurrently
 * held
 */
public class WakeLockManager {
	private static Map<String, PowerManager.WakeLock> locks = new ConcurrentHashMap<String, PowerManager.WakeLock>();

    private static final String TAG = WakeLockManager.class.getSimpleName();

    public static void acquire(Context context, String id, int flags) {
		acquire(context, id, flags, 0);
	}

	public static void acquire(Context context, String id, int flags, int releaseAfterMs) {
		final PowerManager mgr = (PowerManager) context.getApplicationContext().getSystemService(
				Context.POWER_SERVICE);

		// create the new wakelock and put it into the map
		final WakeLock newWakeLock = mgr.newWakeLock(flags, id.toString());

		// if this wakelock doesn't already exist, continue
		locks.put(id, newWakeLock);
		// only one at a time? TODO
		newWakeLock.setReferenceCounted(false);

		if (releaseAfterMs == 0) {
			newWakeLock.acquire();
		} else {
			newWakeLock.acquire(releaseAfterMs);
		}
	}

	public static void release(String id) {
		final WakeLock wakeLock = locks.get(id);
		// if there is was a wakelock, release it. (it has to be held)
		try {
			if (wakeLock != null) {
				wakeLock.release();
			}
		} catch (Exception ex) {
            // android's wakelocks are buggy?
            Log.d(TAG, "Exception while attempting to release wake lock.", ex);
		} finally {
			locks.remove(id);
		}
	}
}
