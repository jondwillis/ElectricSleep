package com.androsz.electricsleep.ui;

import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.achartengine.ChartView;
import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;
import com.androsz.electricsleep.service.SleepAccelerometerService;
import com.androsz.electricsleep.ui.view.SleepChartView;

public class SleepActivity extends CustomTitlebarActivity {

	private class WaitForSeriesDataProgressDialog extends ProgressDialog {
		public WaitForSeriesDataProgressDialog(final Context context) {
			super(context);
		}

		public WaitForSeriesDataProgressDialog(final Context context,
				final int theme) {
			// super(context);
			super(context, theme);
		}

		@Override
		public void onBackPressed() {
			SleepActivity.this.onBackPressed();
		}
	}

	public static final String UPDATE_CHART = "com.androsz.electricsleep.UPDATE_CHART";

	public static final String SYNC_CHART = "com.androsz.electricsleep.SYNC_CHART";

	private SleepChartView sleepChartView;

	private ProgressDialog waitForSeriesData;

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {

			sleepChartView.syncByAdding(intent.getDoubleExtra("x", 0), intent
					.getDoubleExtra("y", 0), intent.getIntExtra("min",
					SettingsActivity.DEFAULT_MIN_SENSITIVITY), intent
					.getIntExtra("max",
							SettingsActivity.DEFAULT_MAX_SENSITIVITY), intent
					.getIntExtra("alarm",
							SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

			if (sleepChartView.makesSense() && waitForSeriesData != null) {
				waitForSeriesData.dismiss();
				waitForSeriesData = null;
			}
		}
	};

	private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(final Context context, final Intent intent) {

			sleepChartView
					.syncByCopying((List<Double>) intent
							.getSerializableExtra("currentSeriesX"),
							(List<Double>) intent
									.getSerializableExtra("currentSeriesY"),
							intent.getIntExtra("min",
									SettingsActivity.DEFAULT_MIN_SENSITIVITY),
							intent.getIntExtra("max",
									SettingsActivity.DEFAULT_MAX_SENSITIVITY),
							intent.getIntExtra("alarm",
									SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

			if (sleepChartView.makesSense() && waitForSeriesData != null) {
				waitForSeriesData.dismiss();
				waitForSeriesData = null;
			}
		}
	};

	private void addChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (layout.getChildCount() == 0) {
			if (sleepChartView == null) {
				sleepChartView = new SleepChartView(this);
			}
			layout.addView(sleepChartView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	private void removeChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (sleepChartView.getParent() == layout) {
			layout.removeView(sleepChartView);
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		this.setTitle(R.string.monitoring_sleep);
		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_export);
		showTitleButton2(R.drawable.ic_title_refresh);
		// TODO having this removed might cause problems
		// addChartView();
	}

	@Override
	protected void onPause() {
		if (waitForSeriesData != null && waitForSeriesData.isShowing()) {
			waitForSeriesData.dismiss();
		}
		unregisterReceiver(updateChartReceiver);
		unregisterReceiver(syncChartReceiver);
		super.onPause();
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		sleepChartView = (SleepChartView) savedState
				.getSerializable("sleepChartView");
		/*
		 * sleepChartGraphicalView.xyMultipleSeriesDataset =
		 * (XYMultipleSeriesDataset) savedState .getSerializable("dataset");
		 * sleepChartGraphicalView.xyMultipleSeriesRenderer =
		 * (XYMultipleSeriesRenderer) savedState .getSerializable("renderer");
		 * 
		 * sleepChartGraphicalView.xySeriesMovement = (XYSeries) savedState
		 * .getSerializable("seriesMovement");
		 * sleepChartGraphicalView.xySeriesMovementRenderer = (XYSeriesRenderer)
		 * savedState .getSerializable("rendererMovement");
		 * 
		 * sleepChartGraphicalView.xySeriesAlarmTrigger = (XYSeries) savedState
		 * .getSerializable("seriesAlarmTrigger");
		 * sleepChartGraphicalView.xySeriesAlarmTriggerRenderer =
		 * (XYSeriesRenderer) savedState
		 * .getSerializable("rendererAlarmTrigger");
		 */
	}

	@Override
	protected void onResume() {
		super.onResume();
		addChartView();
		showWaitForSeriesDataIfNeeded();
		registerReceiver(updateChartReceiver, new IntentFilter(UPDATE_CHART));
		registerReceiver(syncChartReceiver, new IntentFilter(SYNC_CHART));
		sendBroadcast(new Intent(SleepAccelerometerService.POKE_SYNC_CHART));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		removeChartView();
		outState.putSerializable("sleepChartView", sleepChartView);
		/*
		 * outState.putSerializable("dataset",
		 * sleepChartGraphicalView.xyMultipleSeriesDataset);
		 * outState.putSerializable("renderer",
		 * sleepChartGraphicalView.xyMultipleSeriesRenderer);
		 * 
		 * outState.putSerializable("seriesMovement",
		 * sleepChartGraphicalView.xySeriesMovement);
		 * outState.putSerializable("rendererMovement",
		 * sleepChartGraphicalView.xySeriesMovementRenderer);
		 * 
		 * outState.putSerializable("seriesAlarmTrigger",
		 * sleepChartGraphicalView.xySeriesAlarmTrigger);
		 * outState.putSerializable("rendererAlarmTrigger",
		 * sleepChartGraphicalView.xySeriesAlarmTriggerRenderer);
		 */
	}

	public void onTitleButton1Click(final View v) {
		final Intent saveActivityIntent = new Intent(this,
				SaveSleepActivity.class);
		saveActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(saveActivityIntent);
	}

	public void onTitleButton2Click(final View v) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
				.setMessage("will show settings")
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int id) {
						dialog.cancel();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog,
									final int id) {
								dialog.cancel();
							}
						});
		dialog.show();
	}

	private void showWaitForSeriesDataIfNeeded() {
		if (!sleepChartView.makesSense()) {
			if (waitForSeriesData == null || !waitForSeriesData.isShowing()) {
				waitForSeriesData = new WaitForSeriesDataProgressDialog(this);
				waitForSeriesData
						.setMessage(getText(R.string.dialog_wait_for_sleep_data_message));
				// waitForSeriesData.setContentView(R.layout.dialog_wait_for_data);
				waitForSeriesData.setButton(DialogInterface.BUTTON_NEGATIVE,
						getString(R.string.stop),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface arg0,
									final int arg1) {

								stopService(new Intent(SleepActivity.this,
										SleepAccelerometerService.class));
								SleepActivity.this.finish();
							}
						});
				waitForSeriesData.show();
			}
		}
	}
}
