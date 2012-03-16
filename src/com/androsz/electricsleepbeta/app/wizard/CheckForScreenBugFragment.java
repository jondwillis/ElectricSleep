package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;
import com.androsz.electricsleepbeta.app.Log;
import com.androsz.electricsleepbeta.widget.SafeViewFlipper;

import static com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService.ACTION_BUG_PRESENT;
import static com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService.ACTION_BUG_NOT_PRESENT;

public class CheckForScreenBugFragment extends Calibrator {

    private static final int FLIPPER_INSTRUCTIONS = 0;
    private static final int FLIPPER_RESULTS = 1;
    private static final String FLIPPER_STATE = "flipper_state";

    private TextView mResults;
    private SafeViewFlipper mFlipper;

    private final BroadcastReceiver updateFlipperResults = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            Log.d("MBE", "Received broadcast.");
            final String action = intent.getAction();
            if (ACTION_BUG_PRESENT.equals(action)) {
                mResults.setText(
                    getString(R.string.completed_standby_test) +
                    " " +
                    getString(R.string.identified_that_android_device_must_be_on));
            } else if (ACTION_BUG_NOT_PRESENT.equals(action)) {
                mResults.setText(
                    getString(R.string.completed_standby_test) +
                    " " +
                    getString(R.string.identified_screen_can_be_turned_off));
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mResults = (TextView) root.findViewById(R.id.standby_test_results);
        mFlipper = (SafeViewFlipper) root.findViewById(R.id.content_flipper);
        if (savedInstanceState != null) {
            mFlipper.setDisplayedChild(
                savedInstanceState.getInt(FLIPPER_STATE, FLIPPER_INSTRUCTIONS));
        }

        IntentFilter filter = new IntentFilter(ACTION_BUG_PRESENT);
        filter.addAction(ACTION_BUG_NOT_PRESENT);
        getActivity().registerReceiver(updateFlipperResults, filter);
        return root;
    }

    @Override
	public void onResume() {
		super.onResume();

        if (canBegin) {
			if (calibrationStateListener != null) {
                mFlipper.setDisplayedChild(FLIPPER_RESULTS);
                calibrationStateListener.onCalibrationComplete(true);
			}
		}
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FLIPPER_STATE, mFlipper.getDisplayedChild());
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

	private boolean canBegin = false;

	@Override
	public int getLayoutResourceId() {
		// TODO Auto-generated method stub
		return R.layout.wizard_calibration_screenbug;
	}

	@Override
	public void startCalibration(Activity context) {
		if (!canBegin) {
            canBegin = true;

			final Intent i = new Intent(context,
					CheckForScreenBugAccelerometerService.class);

			context.startService(i);
			((TextView) context.findViewById(R.id.status_text))
					.setText(R.string.notification_screenbug_ticker);
		}
	}

	@Override
	public void stopCalibration(Activity context) {
        if (canBegin) {
			canBegin = false;
			final Intent i = new Intent(context,
					CheckForScreenBugAccelerometerService.class);

			if (context.stopService(i)) {
				((TextView) context.findViewById(R.id.status_text))
						.setText("Test Complete.");
			}
		}
	}
}