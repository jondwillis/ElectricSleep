package com.androsz.electricsleepbeta.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.view.View;
import android.widget.Button;

import com.androsz.electricsleepbeta.R;

public class WelcomeTutorialWizardActivity extends CustomTitlebarWizardActivity {

	public static boolean enforceCalibrationBeforeStartingSleep(
			final Activity context) {

		final SharedPreferences userPrefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(
				SettingsActivity.PREFERENCES_ENVIRONMENT, 0);
		String message = "";
		if (prefsVersion == 0) {
			message = context.getString(R.string.message_not_calibrated);
		} else if (prefsVersion != context.getResources().getInteger(
				R.integer.prefs_version)) {
			message = context.getString(R.string.message_prefs_not_compatible);
		}

		if (message.length() > 0) {
			message += context
					.getString(R.string.message_recommend_calibration);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(context)
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton(context.getString(R.string.calibrate),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									context.startActivity(new Intent(context,
											CalibrationWizardActivity.class));
									context.finish();
								}
							})
					.setNeutralButton(context.getString(R.string.manual),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									context.startActivity(new Intent(context,
											SettingsActivity.class));
									context.finish();
								}
							})
					.setNegativeButton(context.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			dialog.show();
			return false;
		} else {
			return true;
		}
	}

	private boolean required = false;

	@Override
	protected int getWizardLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.wizard_welcome;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		required = getIntent().hasExtra("required");
		if (required) {
			final ActionBar bar = getSupportActionBar();
			bar.setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (required) {
			return false;
		} else {
			return super.onCreateOptionsMenu(menu);
		}
	}

	@Override
	protected void onFinishWizardActivity() {
		if (enforceCalibrationBeforeStartingSleep(this)) {
			finish();
		}
	}

	@Override
	public void onLeftButtonClick(final View v) {
		if (viewFlipper.getDisplayedChild() == 0) {
			onFinishWizardActivity();
		} else {
			super.onLeftButtonClick(v);
		}
	}

	@Override
	protected void onPrepareLastSlide() {
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		required = savedState.getBoolean("required");
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("required", required);
	}

	@Override
	protected boolean onWizardActivity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void setupNavigationButtons() {
		super.setupNavigationButtons();
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		if (viewFlipper.getDisplayedChild() == 0) {
			leftButton.setText(R.string.skip_tutorial);
		}
	}
}
