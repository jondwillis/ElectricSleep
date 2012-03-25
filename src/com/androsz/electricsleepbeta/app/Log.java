/* @(#)Log.java
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Modified By:
 *
 *========================================================================
 * Copyright 2011 by Zeo Inc. All Rights Reserved
 *========================================================================
 *
 * Date: $Date$
 * Author: Brandon Edens <brandon.edens@myzeo.com>
 * Version: $Revision$
 */

package com.androsz.electricsleepbeta.app;

import android.os.SystemClock;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generic Zeo logging subsystem.
 *
 * <h1>Description</h1>
 * <p>
 * File that wraps over standard Android logging normalizing the LOGTAG if one
 * is not provided.
 * </p>
 *
 * @author Brandon Edens
 * @version $Revision$
 */
public class Log {

    /** Set up logging to NOT include debug logs. */
    private static boolean sDebug = false;

    private final static String LOGTAG = "ElectricSleep";

    public static void d(String logMe) {
        if (sDebug) {
            android.util.Log.d(LOGTAG, SystemClock.uptimeMillis() + " " + logMe);
        }
    }

    public static void d(String TAG, String logMe) {
        if (sDebug) {
            android.util.Log.d(TAG, SystemClock.uptimeMillis() + " " + logMe);
        }
    }

    public static void d(String TAG, String logMe, Exception ex) {
        if (sDebug) {
            android.util.Log.d(TAG, SystemClock.uptimeMillis() + " " + logMe, ex);
        }
    }

    public static void v(String logMe) {
        if (sDebug) {
            android.util.Log.v(LOGTAG, /* SystemClock.uptimeMillis() + " " + */ logMe);
        }
    }

    public static void v(String TAG, String logMe) {
        if (sDebug) {
            android.util.Log.v(TAG, /* SystemClock.uptimeMillis() + " " + */ logMe);
        }
    }

    public static void v(String TAG, String logMe, Exception ex) {
        if (sDebug) {
            android.util.Log.v(TAG, logMe, ex);
        }
    }

    public static void i(String logMe) {
        android.util.Log.i(LOGTAG, logMe);
    }

    public static void i(String TAG, String logMe) {
        android.util.Log.i(TAG, logMe);
    }

    public static void e(String logMe) {
        android.util.Log.e(LOGTAG, logMe);
    }

    public static void e(String TAG, String logMe) {
        android.util.Log.e(TAG, logMe);
    }

    public static void e(String logMe, Exception ex) {
        android.util.Log.e(LOGTAG, logMe, ex);
    }

    public static void e(String TAG, String logMe, Exception ex) {
        android.util.Log.e(TAG, logMe, ex);
    }

    public static void w(String logMe) {
        android.util.Log.w(LOGTAG, logMe);
    }

    public static void w(String TAG, String logMe) {
        android.util.Log.w(TAG, logMe);
    }

    public static void w(String logMe, Exception ex) {
        android.util.Log.w(LOGTAG, logMe, ex);
    }

    public static void w(String TAG, String logMe, Exception ex) {
        android.util.Log.w(TAG, logMe, ex);
    }

    public static void wtf(String logMe) {
        android.util.Log.e(LOGTAG, logMe);
    }

    public static String formatTime(long millis) {
        return new SimpleDateFormat("HH:mm:ss.SSS aaa").format(new Date(millis));
    }

    /**
     * Enable / disable logging of any information less than warning level.
     */
    static void toggleDebug(boolean enable) {
        sDebug = enable;
    }
}

