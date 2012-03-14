package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;
import com.androsz.electricsleepbeta.widget.SafeViewFlipper;

public class CheckForScreenBugFragment extends Calibrator {

    private static final int FLIPPER_INSTRUCTIONS = 0;
    private static final int FLIPPER_RESULTS = 1;

    private SafeViewFlipper mFlipper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = super.onCreateView(inflater, container, savedInstanceState);
        mFlipper = (SafeViewFlipper) root.findViewById(R.id.content_flipper);
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