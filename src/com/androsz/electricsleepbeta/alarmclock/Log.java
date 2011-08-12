/*
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
 */

/**
 * package-level logging flag
 */

package com.androsz.electricsleepbeta.alarmclock;

import android.util.Config;

class Log {
	public final static String LOGTAG = "com.androsz.electricsleepbeta.AlarmClock";

	static final boolean LOGV = AlarmClock.DEBUG ? Config.LOGD : Config.LOGV;

	static void e(final String logMe) {
		android.util.Log.e(LOGTAG, logMe);
	}

	static void e(final String logMe, final Exception ex) {
		android.util.Log.e(LOGTAG, logMe, ex);
	}

	static void i(final String logMe) {
		android.util.Log.i(LOGTAG, logMe);
	}

	static void v(final String logMe) {
		android.util.Log.v(LOGTAG, /* SystemClock.uptimeMillis() + " " + */
		logMe);
	}
}
