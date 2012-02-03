/* @(#)SafeViewFlipper.java
 *
 *========================================================================
 * Copyright 2011 by Zeo Inc. All Rights Reserved
 *========================================================================
 *
 * Date: $Date$
 * Author: Christopher Souvey <christopher.souvey@myzeo.com>
 * Version: $Revision$
 */

package com.androsz.electricsleepbeta.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ViewFlipper;

/**
 * View flipper that protects against against crashes during screen rotations.
 *
 * @author Christopher Souvey
 * @version $Revision$
 */
public class SafeViewFlipper extends ViewFlipper {

    private static final String TAG = SafeViewFlipper.class.getSimpleName();

    public SafeViewFlipper(Context context) {
        super(context);
    }

    public SafeViewFlipper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Workaround for Android Bug 6191:
     * http://code.google.com/p/android/issues/detail?id=6191
     * <p/>
     * ViewFlipper occasionally throws an IllegalArgumentException after screen
     * rotations.
     */
    @Override
    protected void onDetachedFromWindow() {
        try {
            super.onDetachedFromWindow();
        } catch (IllegalArgumentException e) {
            Log.d(TAG, "SafeViewFlipper ignoring IllegalArgumentException");
            // Call stopFlipping() in order to kick off updateRunning()
            stopFlipping();
        }
    }
}