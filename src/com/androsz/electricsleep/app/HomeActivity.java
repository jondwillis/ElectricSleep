package com.androsz.electricsleep.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.service.SleepAccelerometerService;
import com.androsz.electricsleep.view.SleepChartView;

/**
 * Front-door {@link Activity} that displays high-level features the application
 * offers to users.
 */
public class HomeActivity extends CustomTitlebarActivity {

	private SleepChartView sleepChartView;

	private void addChartView() {
		findViewById(R.id.home_container);
		sleepChartView = (SleepChartView) findViewById(R.id.home_sleep_chart);

		final Cursor cursor = managedQuery(SleepContentProvider.CONTENT_URI,
				null, null, new String[] { getString(R.string.to) },
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME + " DESC");

		final TextView reviewTitleText = (TextView) findViewById(R.id.home_review_title_text);
		if (cursor == null) {
			sleepChartView.setVisibility(View.GONE);
			reviewTitleText
					.setText(getString(R.string.home_review_title_text_empty));
		} else {
			cursor.moveToLast();
			sleepChartView.setVisibility(View.VISIBLE);
			sleepChartView.syncWithCursor(cursor);
			reviewTitleText.setText(getString(R.string.home_review_title_text));
		}
	}

	private void enforceCalibrationBeforeStartingSleep(final Intent service,
			final Intent activity) {

		new Thread(new Runnable() {

			@Override
			public void run() {
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
					final AlertDialog.Builder dialog = new AlertDialog.Builder(
							HomeActivity.this)
							.setMessage(message)
							.setCancelable(false)
							.setPositiveButton("Calibrate",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												final DialogInterface dialog,
												final int id) {
											startActivity(new Intent(
													HomeActivity.this,
													CalibrationWizardActivity.class));
										}
									})
							.setNeutralButton("Manual",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												final DialogInterface dialog,
												final int id) {
											startActivity(new Intent(
													HomeActivity.this,
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
		}).run();
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

		final SharedPreferences userPrefs = getSharedPreferences(
				getString(R.string.prefs_version), Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(
				getString(R.string.prefs_version), 0);
		if (prefsVersion == 0) {
			// todo
		}

	}

	public void onHistoryClick(final View v) {
		startActivity(new Intent(this, HistoryActivity.class));
	}

	@Override
	public void onHomeClick(final View v) {
		// do nothing b/c home is home!
	}

	@Override
	protected void onPause() {
		super.onPause();
		// removeChartView();
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		try {
			super.onRestoreInstanceState(savedState);
			sleepChartView = (SleepChartView) savedState
					.getSerializable("sleepChartView");
		} catch (final RuntimeException re) {

		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		addChartView();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable("sleepChartView", sleepChartView);
	}

	public void onSleepClick(final View v) throws Exception {
		new Thread(new Runnable() {

			@Override
			public void run() {
				final SharedPreferences userPrefs = PreferenceManager
						.getDefaultSharedPreferences(HomeActivity.this);
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
						|| alarmTriggerSensitivity < 0 || useAlarm
						&& alarmWindow < 0) {
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
											startActivity(new Intent(
													HomeActivity.this,
													CalibrationWizardActivity.class));
										}
									})
							.setNeutralButton("Manual",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												final DialogInterface dialog,
												final int id) {
											startActivity(new Intent(
													HomeActivity.this,
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
				enforceCalibrationBeforeStartingSleep(serviceIntent,
						new Intent(HomeActivity.this, SleepActivity.class));
			}
		}).run();
	}

	public void onTitleButton1Click(final View v) {
		Toast.makeText(this, "ohhh", Toast.LENGTH_SHORT).show();
	}
}
