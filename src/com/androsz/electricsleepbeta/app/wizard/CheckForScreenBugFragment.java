package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.widget.SafeViewFlipper;

public class CheckForScreenBugFragment extends Calibrator {

    private static final int FLIPPER_INSTRUCTIONS = 0;
    private static final int FLIPPER_RESULTS = 1;

    /** The saved state of the flipper. */
    private static final String FLIPPER_STATE = "flipper_state";

    /** The saved state of the results message. */
    private static final String RESULTS_TXT = "results_text";

    private TextView mResults;
    private SafeViewFlipper mFlipper;

    // Ugly static-ness required because of custom lockscreens.
    public static Integer SCREEN_BUG_STATE = null;
    public final static int SCREEN_BUG_PRESENT = 0;
    public final static int SCREEN_BUG_NOT_PRESENT = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mResults = (TextView) root.findViewById(R.id.standby_test_results);
        mFlipper = (SafeViewFlipper) root.findViewById(R.id.content_flipper);
        if (savedInstanceState != null) {
            mFlipper.setDisplayedChild(savedInstanceState.getInt(FLIPPER_STATE,
                    FLIPPER_INSTRUCTIONS));
            mResults.setText(savedInstanceState.getString(RESULTS_TXT,
                    getString(R.string.completed_standby_test)));
        }

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        // we cannot use a BroadcastReceiver because of custom
        // lockscreens.

        if (calibrationStateListener != null && SCREEN_BUG_STATE != null) {
            // update displayed language
            if (SCREEN_BUG_STATE == SCREEN_BUG_PRESENT) {
                mResults.setText(getString(R.string.completed_standby_test)
                        + " "
                        + getString(R.string.identified_that_android_device_must_be_on));
            } else if (SCREEN_BUG_STATE == SCREEN_BUG_NOT_PRESENT) {
                mResults.setText(getString(R.string.completed_standby_test)
                        + " "
                        + getString(R.string.identified_screen_can_be_turned_off));
            } else {
                throw new IllegalStateException("SCREEN_BUG_STATE");
            }
            /*
             * if (ACTION_BUG_PRESENT.equals(action)) { mResults.setText(
             * getString(R.string.completed_standby_test) + " " + getString(R
             * .string.identified_that_android_device_must_be_on)); } else if
             * (ACTION_BUG_NOT_PRESENT.equals(action)) { mResults.setText(
             * getString(R.string.completed_standby_test) + " " +
             * getString(R.string.identified_screen_can_be_turned_off)); }
             */
            mFlipper.setDisplayedChild(FLIPPER_RESULTS);
            calibrationStateListener.onCalibrationComplete(true);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FLIPPER_STATE, mFlipper.getDisplayedChild());
        outState.putString(RESULTS_TXT, (String) mResults.getText());
    }

    /*
     * @Override public void onPause() { super.onPause();
     * 
     * final Intent i = new Intent(getActivity(),
     * CheckForScreenBugAccelerometerService.class);
     * 
     * // this replaces the need for broadcast receivers. // the service updates
     * BUG_PRESENT_INTENT, THEN our activity is // alerted. if
     * (BUG_PRESENT_INTENT != null) { stopService(i); BUG_PRESENT_INTENT = null;
     * } }
     */

    @Override
    public int getLayoutResourceId() {
        return R.layout.wizard_calibration_screenbug;
    }

    @Override
    public void startCalibration(Activity context) {
        if (SCREEN_BUG_STATE == null) {
            final Intent i = new Intent(context,
                    CheckForScreenBugAccelerometerService.class);

            context.startService(i);
            ((TextView) context.findViewById(R.id.status_text))
                    .setText(R.string.notification_screenbug_ticker);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void stopCalibration(Activity context) {
        // reset state
        SCREEN_BUG_STATE = null;

        final Intent i = new Intent(context,
                CheckForScreenBugAccelerometerService.class);

        if (context.stopService(i)) {
            ((TextView) context.findViewById(R.id.status_text))
                    .setText("Test Complete.");
        }
    }
}