/*
 * Copyright 2010 Google Inc.
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

package com.androsz.electricsleep.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.service.SleepAccelerometerService;

/**
 * Front-door {@link Activity} that displays high-level features the application
 * offers to users.
 */
public class HomeActivity extends CustomTitlebarActivity {

	private void enforceCalibrationBeforeStartingSleep(final Intent service,
			final Intent activity) {
		final SharedPreferences userPrefs = getSharedPreferences(
				getString(R.string.prefs_version), Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(
				getString(R.string.prefs_version), 0);
		String message = "";
		if (prefsVersion == 0) {
			message = "You have not yet calibrated ElectricSleep to work on your device.";
		} else if (prefsVersion != getResources().getInteger(
				R.integer.prefs_version)) {
			message = "Your preferences are not compatible with this version of ElectricSleep.";
		}

		if (message.length() > 0) {
			message += "\n\nIt is recommended that you run the Calibration Wizard or manually configure your Settings now.";
			final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
					.setMessage(message).setCancelable(false)
					.setPositiveButton("Calibrate",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onCalibrateClick(null);
								}
							}).setNeutralButton("Manual",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onSettingsClick(null);
								}
							}).setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog.show();
		} else if (service != null && activity != null) {
			startService(service);
			startActivity(activity);
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_home;
	}

	public void onAlarmsClick(View v) {
		startActivity(new Intent(this, AlarmsActivity.class));
	}

	public void onCalibrateClick(View v) {
		startActivity(new Intent(this, CalibrationWizardActivity.class));
	}

	public void onCloudClick(View v) {
		startActivity(new Intent(this, CloudActivity.class));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// change title in titlebar without changing the app's name in the
		// launcher
		this.setTitle(R.string.title_home);

		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_export);
		showTitleButton2(R.drawable.ic_title_refresh);

		enforceCalibrationBeforeStartingSleep(null, null);
	}

	public void onHistoryClick(View v) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	@Override
	public void onHomeClick(View v) {
		// do nothing b/c home is home!
	}

	public void onSettingsClick(View v) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onSleepClick(View v) throws Exception {

		final SharedPreferences userPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		final int maxSensitivity = userPrefs.getInt(
				getString(R.string.pref_maximum_sensitivity), -1);
		final int minSensitivity = userPrefs.getInt(
				getString(R.string.pref_minimum_sensitivity), -1);
		final int alarmTriggerSensitivity = userPrefs.getInt(
				getString(R.string.pref_alarm_trigger_sensitivity), -1);

		if (maxSensitivity < 0 || minSensitivity < 0
				|| alarmTriggerSensitivity < 0) {
			final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
					.setMessage(
							"Your calibration settings are invalid. Please manually configure settings or run the Calibration Wizard.")
					.setCancelable(false).setPositiveButton("Calibrate",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onCalibrateClick(null);
								}
							}).setNeutralButton("Manual",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									onSettingsClick(null);
								}
							}).setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
			dialog.show();
			return;
		}

		final Intent i = new Intent(this, SleepAccelerometerService.class);
		i.putExtra("min", minSensitivity);
		i.putExtra("max", maxSensitivity);
		i.putExtra("alarm", alarmTriggerSensitivity);
		enforceCalibrationBeforeStartingSleep(i, new Intent(this,
				SleepActivity.class));
	}

	public void onTitleButton1Click(View v) {
		Toast.makeText(this, "ohhh", Toast.LENGTH_SHORT);
	}
}
