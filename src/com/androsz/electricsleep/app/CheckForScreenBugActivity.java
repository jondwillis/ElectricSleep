package com.androsz.electricsleep.app;

import android.content.Intent;
import android.os.Bundle;

import com.androsz.electricsleep.R;

public class CheckForScreenBugActivity extends CalibrateForResultActivity {

	// hack-ish but necessary because lockscreens can differ
	public static Intent BUG_PRESENT_INTENT = null;

	/*
	 * private final BroadcastReceiver bugNotPresentReceiver = new
	 * BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(final Context context, final Intent
	 * intent) { CheckForScreenBugActivity.this .setResult(
	 * CALIBRATION_SUCCEEDED, new Intent(
	 * CheckForScreenBugAccelerometerService.BUG_NOT_PRESENT));
	 * unregisterReceiver(bugPresentReceiver);
	 * unregisterReceiver(bugNotPresentReceiver); finish(); } };
	 * 
	 * private final BroadcastReceiver bugPresentReceiver = new
	 * BroadcastReceiver() {
	 * 
	 * @Override public void onReceive(final Context context, final Intent
	 * intent) { CheckForScreenBugActivity.this.setResult(CALIBRATION_SUCCEEDED,
	 * new Intent( CheckForScreenBugAccelerometerService.BUG_PRESENT));
	 * unregisterReceiver(bugPresentReceiver);
	 * unregisterReceiver(bugNotPresentReceiver); finish(); } };
	 */

	@Override
	protected Intent getAssociatedServiceIntent() {
		return new Intent(this, CheckForScreenBugAccelerometerService.class);
	}

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_check_for_screen_bug);
	}

	@Override
	public void onResume() {
		super.onResume();

		final Intent i = new Intent(this,
				CheckForScreenBugAccelerometerService.class);

		// this replaces the need for broadcast receivers.
		// the service updates BUG_PRESENT_INTENT, THEN our activity is alerted.
		if (BUG_PRESENT_INTENT != null) {
			stopService(i);
			CheckForScreenBugActivity.this.setResult(CALIBRATION_SUCCEEDED,
					new Intent(BUG_PRESENT_INTENT));
			BUG_PRESENT_INTENT = null;
			finish();
		} else {
			startService(i);
		}
	}
}
