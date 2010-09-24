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
			message = getString(R.string.message_not_calibrated);
		} else if (prefsVersion != getResources().getInteger(
				R.integer.prefs_version)) {
			message = getString(R.string.message_prefs_not_compatible);
		}

		if (message.length() > 0) {
			message += getString(R.string.message_recommend_calibration);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton("Calibrate",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									startActivity(new Intent(HomeActivity.this,
											CalibrationWizardActivity.class));
								}
							})
					.setNeutralButton("Manual",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									startActivity(new Intent(HomeActivity.this,
											SettingsActivity.class));
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
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

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PreferenceManager.setDefaultValues(this, R.xml.settings, false);

		showTitleButton1(R.drawable.ic_title_export);
		// showTitleButton2(R.drawable.ic_title_refresh);
		setHomeButtonAsLogo();

		enforceCalibrationBeforeStartingSleep(null, null);
	}

	public void onHistoryClick(final View v) {
		startActivity(new Intent(this, HistoryActivity.class));
	}

	@Override
	public void onHomeClick(final View v) {
		// do nothing b/c home is home!
	}

	public void onSleepClick(final View v) throws Exception {
		final SharedPreferences userPrefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		final int minSensitivity = userPrefs.getInt(
				getString(R.string.pref_minimum_sensitivity), -1);
		final int maxSensitivity = userPrefs.getInt(
				getString(R.string.pref_maximum_sensitivity), -1);
		final int alarmTriggerSensitivity = userPrefs.getInt(
				getString(R.string.pref_alarm_trigger_sensitivity), -1);

		final boolean useAlarm = userPrefs.getBoolean(
				getString(R.string.pref_use_alarm), false);
		final int alarmWindow = Integer.parseInt(userPrefs.getString(
				getString(R.string.pref_alarm_window), "-1"));

		if (maxSensitivity < 0 || minSensitivity < 0
				|| alarmTriggerSensitivity < 0 || useAlarm && alarmWindow < 0) {
			final AlertDialog.Builder dialog = new AlertDialog.Builder(
					HomeActivity.this)
					.setMessage(getString(R.string.invalid_settings))
					.setCancelable(false)
					.setPositiveButton("Calibrate",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									startActivity(new Intent(HomeActivity.this,
											CalibrationWizardActivity.class));
								}
							})
					.setNeutralButton("Manual",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									startActivity(new Intent(HomeActivity.this,
											SettingsActivity.class));
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										final DialogInterface dialog,
										final int id) {
									dialog.cancel();
								}
							});
			dialog.show();
			return;
		}

		final Intent serviceIntent = new Intent(HomeActivity.this,
				SleepAccelerometerService.class);
		serviceIntent.putExtra("min", minSensitivity);
		serviceIntent.putExtra("max", maxSensitivity);
		serviceIntent.putExtra("alarm", alarmTriggerSensitivity);
		serviceIntent.putExtra("useAlarm", useAlarm);
		serviceIntent.putExtra("alarmWindow", alarmWindow);
		enforceCalibrationBeforeStartingSleep(serviceIntent, new Intent(HomeActivity.this,
				SleepActivity.class));
	}

	public void onTitleButton1Click(final View v) {
		Toast.makeText(this, "ohhh", Toast.LENGTH_SHORT).show();
	}
}
