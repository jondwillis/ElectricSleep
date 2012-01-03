package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.content.Intent;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.CheckForScreenBugAccelerometerService;

public class CheckForScreenBugFragment extends LayoutFragment {

	@Override
	public void onResume() {
		super.onResume();

		if (canBegin) {

			final Intent i = new Intent(getActivity(), CheckForScreenBugAccelerometerService.class);
			// this replaces the need for broadcast receivers.
			// the service updates BUG_PRESENT_INTENT, THEN our activity is
			// alerted.
			if (CalibrationWizardActivity.BUG_PRESENT_INTENT != null) {
				getActivity().stopService(i);
				CalibrationWizardActivity.BUG_PRESENT_INTENT = null;
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

	public void begin() {
		canBegin = true;

			final Intent i = new Intent(getActivity(), CheckForScreenBugAccelerometerService.class);
			// this replaces the need for broadcast receivers.
			// the service updates BUG_PRESENT_INTENT, THEN our activity is
			// alerted.
			if (CalibrationWizardActivity.BUG_PRESENT_INTENT != null) {
				getActivity().stopService(i);
				CalibrationWizardActivity.BUG_PRESENT_INTENT = null;
			} else {
				getActivity().startService(i);
			}
	}

	public void end() {
		canBegin = false;
		// if (this.isAdded()) {
		Activity a = getActivity();
		if (a != null) {
			final Intent i = new Intent(a, CheckForScreenBugAccelerometerService.class);

			a.stopService(i);
			CalibrationWizardActivity.BUG_PRESENT_INTENT = null;
		}
	}

	@Override
	public int getLayoutResourceId() {
		// TODO Auto-generated method stub
		return R.layout.wizard_calibration_screenbug;
	}
}