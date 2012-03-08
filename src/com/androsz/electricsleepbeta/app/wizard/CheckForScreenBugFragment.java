package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.Intent;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;
import com.androsz.electricsleepbeta.app.LayoutFragment;

public class CheckForScreenBugFragment extends LayoutFragment implements
		Calibrator {

	@Override
	public void onResume() {
		super.onResume();

		if (canBegin) {

			final Intent i = new Intent(getActivity(),
					CheckForScreenBugAccelerometerService.class);
			// this replaces the need for broadcast receivers.
			// the service updates BUG_PRESENT_INTENT, THEN our activity is
			// alerted.
			if (CalibrationWizardActivity.BUG_PRESENT_INTENT != null) {
				getActivity().stopService(i);
				CalibrationWizardActivity.BUG_PRESENT_INTENT = null;
				onRightButtonClicked(null);
			}
		}
	}

	private void onRightButtonClicked(Object object) {
		// TODO Auto-generated method stub

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
		canBegin = true;

		final Intent i = new Intent(context,
				CheckForScreenBugAccelerometerService.class);
		// this replaces the need for broadcast receivers.
		// the service updates BUG_PRESENT_INTENT, THEN our activity is
		// alerted.
		if (CalibrationWizardActivity.BUG_PRESENT_INTENT != null) {
			context.stopService(i);
			CalibrationWizardActivity.BUG_PRESENT_INTENT = null;
		} else {
			context.startService(i);
		}

	}

	@Override
	public void stopCalibration(Activity context) {
		canBegin = false;
		final Intent i = new Intent(context,
				CheckForScreenBugAccelerometerService.class);

		context.stopService(i);
		CalibrationWizardActivity.BUG_PRESENT_INTENT = null;
	}
}