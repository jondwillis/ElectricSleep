package com.androsz.electricsleep.ui;

import java.io.IOException;
import java.io.StreamCorruptedException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.service.SleepAccelerometerService;
import com.androsz.electricsleep.ui.widget.SleepChartView;

/**
 * Front-door {@link Activity} that displays high-level features the application
 * offers to users.
 */
public class HomeActivity extends CustomTitlebarActivity {

	private SleepChartView sleepChartView;

	private void addChartView() {
		final RelativeLayout layout = (RelativeLayout) findViewById(R.id.home_container);
		// if (layout.getChildCount() == 2) {
		sleepChartView = (SleepChartView) findViewById(R.id.home_sleep_chart);
		/*
		 * / / layout . addView ( sleepChartView , new LayoutParams ( / /
		 * LayoutParams . WRAP_CONTENT , LayoutParams . WRAP_CONTENT ) ) ;
		 * 
		 * final Button sleepBtn = ( Button ) findViewById ( R . id .
		 * home_btn_sleep ) ; sleepBtn . bringToFront ( ) ; final Button
		 * historyBtn = ( Button ) findViewById ( R . id . home_btn_history ) ;
		 * historyBtn . bringToFront ( ) ; }
		 */
		final Cursor cursor = managedQuery(SleepContentProvider.CONTENT_URI,
				null, null, new String[] { getString(R.string.to) },
				SleepHistoryDatabase.KEY_SLEEP_DATE_TIME + " DESC");

		if (cursor == null) {
			sleepChartView.setVisibility(View.GONE);
		} else {
			cursor.moveToLast();
			sleepChartView.syncWithCursor(cursor);
			TextView reviewTitleText = (TextView)findViewById(R.id.home_review_title_text);
			reviewTitleText.setText(getString(R.string.home_review_title_text));
		}
	}

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

		final SharedPreferences userPrefs = getSharedPreferences(
				getString(R.string.prefs_version), Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(
				getString(R.string.prefs_version), 0);
		if (prefsVersion == 0) {
			//todo
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
		} catch (RuntimeException re) {

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
		final SharedPreferences userPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);
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
		enforceCalibrationBeforeStartingSleep(serviceIntent, new Intent(
				HomeActivity.this, SleepActivity.class));
	}

	public void onTitleButton1Click(final View v) {
		Toast.makeText(this, "ohhh", Toast.LENGTH_SHORT).show();
	}

	private void removeChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (sleepChartView.getParent() == layout) {
			layout.removeView(sleepChartView);
		}
	}
}
