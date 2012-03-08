package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.Intent;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;

public class CheckForScreenBugFragment extends Calibrator {

	@Override
	public void onResume() {
		super.onResume();

		if (canBegin) {
			if (calibrationStateListener != null) {
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