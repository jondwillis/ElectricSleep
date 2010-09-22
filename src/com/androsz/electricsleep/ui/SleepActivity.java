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
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.achartengine.ChartFactory;
import com.androsz.electricsleep.achartengine.GraphicalView;
import com.androsz.electricsleep.achartengine.model.XYMultipleSeriesDataset;
import com.androsz.electricsleep.achartengine.model.XYSeries;
import com.androsz.electricsleep.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleep.achartengine.renderer.XYSeriesRenderer;
import com.androsz.electricsleep.service.SleepAccelerometerService;

public class SleepActivity extends CustomTitlebarActivity {

	private class WaitForSeriesDataProgressDialog extends ProgressDialog {
		public WaitForSeriesDataProgressDialog(Context context) {
			super(context);
		}

		public WaitForSeriesDataProgressDialog(Context context, int theme) {
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

	private XYMultipleSeriesDataset xyMultipleSeriesDataset;

	private XYMultipleSeriesRenderer xyMultipleSeriesRenderer;

	private XYSeries xySeriesMovement;

	private XYSeriesRenderer xySeriesMovementRenderer;

	private XYSeries xySeriesAlarmTrigger;

	private XYSeriesRenderer xySeriesAlarmTriggerRenderer;

	private GraphicalView chartGraphicalView;

	private ProgressDialog waitForSeriesData;

	private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			xySeriesMovement.mX.add(intent.getDoubleExtra("x", 0));
			xySeriesMovement.mY.add(intent.getDoubleExtra("y", 0));

			redrawChart(intent.getIntExtra("min", 0),
					intent.getIntExtra("max", 100),
					intent.getIntExtra("alarm", -1));
		}
	};

	private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			xySeriesMovement.mX = (List<Double>) intent
					.getSerializableExtra("currentSeriesX");

			xySeriesMovement.mY = (List<Double>) intent
					.getSerializableExtra("currentSeriesY");

			redrawChart(intent.getIntExtra("min", 0),
					intent.getIntExtra("max", 100),
					intent.getIntExtra("alarm", -1));
		}
	};

	private void addChartView() {
		final LinearLayout layout = (LinearLayout) findViewById(R.id.sleepMovementChart);
		if (layout.getChildCount() == 0) {
			chartGraphicalView = ChartFactory
					.getTimeChartView(this, xyMultipleSeriesDataset,
							xyMultipleSeriesRenderer, "h:mm a");
			layout.addView(chartGraphicalView, new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		}
	}

	private void buildChart() {
		if (xySeriesMovement == null) {

			// set up sleep movement series/renderer
			xySeriesMovement = new XYSeries("sleep");
			xySeriesMovementRenderer = new XYSeriesRenderer();
			xySeriesMovementRenderer.setFillBelowLine(true);
			xySeriesMovementRenderer.setFillBelowLineColor(getResources()
					.getColor(R.color.primary1));
			xySeriesMovementRenderer.setColor(Color.TRANSPARENT);

			// set up alarm trigger series/renderer
			xySeriesAlarmTrigger = new XYSeries("alarmTrigger");
			xySeriesAlarmTriggerRenderer = new XYSeriesRenderer();
			xySeriesAlarmTriggerRenderer.setFillBelowLine(true);
			xySeriesAlarmTriggerRenderer.setFillBelowLineColor(getResources()
					.getColor(R.color.background_transparent_lighten));
			xySeriesAlarmTriggerRenderer.setColor(Color.TRANSPARENT);

			// add series to the dataset
			xyMultipleSeriesDataset = new XYMultipleSeriesDataset();
			xyMultipleSeriesDataset.addSeries(xySeriesMovement);
			xyMultipleSeriesDataset.addSeries(xySeriesAlarmTrigger);

			// set up the dataset renderer
			xyMultipleSeriesRenderer = new XYMultipleSeriesRenderer();
			xyMultipleSeriesRenderer
					.addSeriesRenderer(xySeriesMovementRenderer);
			xyMultipleSeriesRenderer
					.addSeriesRenderer(xySeriesAlarmTriggerRenderer);
			xyMultipleSeriesRenderer.setShowLegend(false);
			xyMultipleSeriesRenderer.setAxisTitleTextSize(22);
			xyMultipleSeriesRenderer.setLabelsTextSize(17);
			xyMultipleSeriesRenderer.setAntialiasing(true);
			final Display defaultDisplay = getWindowManager()
					.getDefaultDisplay();
			if (defaultDisplay.getWidth() > defaultDisplay.getHeight()) {
				// landscape
				xyMultipleSeriesRenderer.setXLabels(10);
			} else {
				// portrait
				xyMultipleSeriesRenderer.setXLabels(5);
			}
			xyMultipleSeriesRenderer.setYLabels(0);
			xyMultipleSeriesRenderer
					.setYTitle(getString(R.string.movement_level_during_sleep));
			xyMultipleSeriesRenderer.setShowGrid(true);
			xyMultipleSeriesRenderer.setAxesColor(getResources().getColor(
					R.color.text));
			xyMultipleSeriesRenderer.setLabelsColor(xyMultipleSeriesRenderer
					.getAxesColor());
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_sleep;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		this.setTitle(R.string.monitoring_sleep);
		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_export);
		showTitleButton2(R.drawable.ic_title_refresh);

		buildChart();
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
	protected void onRestoreInstanceState(Bundle savedState) {
		super.onRestoreInstanceState(savedState);

		xyMultipleSeriesDataset = (XYMultipleSeriesDataset) savedState
				.getSerializable("dataset");
		xyMultipleSeriesRenderer = (XYMultipleSeriesRenderer) savedState
				.getSerializable("renderer");

		xySeriesMovement = (XYSeries) savedState
				.getSerializable("seriesMovement");
		xySeriesMovementRenderer = (XYSeriesRenderer) savedState
				.getSerializable("rendererMovement");

		xySeriesAlarmTrigger = (XYSeries) savedState
				.getSerializable("seriesAlarmTrigger");
		xySeriesAlarmTriggerRenderer = (XYSeriesRenderer) savedState
				.getSerializable("rendererAlarmTrigger");
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
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putSerializable("dataset", xyMultipleSeriesDataset);
		outState.putSerializable("renderer", xyMultipleSeriesRenderer);

		outState.putSerializable("seriesMovement", xySeriesMovement);
		outState.putSerializable("rendererMovement", xySeriesMovementRenderer);

		outState.putSerializable("seriesAlarmTrigger", xySeriesAlarmTrigger);
		outState.putSerializable("rendererAlarmTrigger",
				xySeriesAlarmTriggerRenderer);
	}

	public void onTitleButton1Click(View v) {
		Intent saveActivityIntent = new Intent(this, SaveSleepActivity.class);
		saveActivityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
				| Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(saveActivityIntent);
	}

	public void onTitleButton2Click(View v) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
				.setMessage("will show settings")
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		dialog.show();
	}

	private void redrawChart(int min, int max, int alarmTriggerSensitivity) {
		if (xySeriesMovement.mX.size() > 1 && xySeriesMovement.mY.size() > 1) {
			if (waitForSeriesData != null) {
				waitForSeriesData.dismiss();
				waitForSeriesData = null;
			}

			final double firstX = xySeriesMovement.mX.get(0);
			final double lastX = xySeriesMovement.mX.get(xySeriesMovement.mX
					.size() - 1);
			xyMultipleSeriesRenderer.setXAxisMin(firstX);
			xyMultipleSeriesRenderer.setXAxisMax(lastX);

			xyMultipleSeriesRenderer.setYAxisMin(min);
			xyMultipleSeriesRenderer.setYAxisMax(max);

			// reconfigure the alarm trigger line..
			xySeriesAlarmTrigger.clear();

			xySeriesAlarmTrigger.add(firstX, alarmTriggerSensitivity);
			xySeriesAlarmTrigger.add(lastX, alarmTriggerSensitivity);

			chartGraphicalView.repaint();
		}
	}

	private void showWaitForSeriesDataIfNeeded() {
		if (xySeriesMovement.getItemCount() < 2) {
			if (waitForSeriesData == null || !waitForSeriesData.isShowing()) {
				waitForSeriesData = new WaitForSeriesDataProgressDialog(this);
				waitForSeriesData
						.setMessage(getText(R.string.dialog_wait_for_sleep_data_message));
				// waitForSeriesData.setContentView(R.layout.dialog_wait_for_data);
				waitForSeriesData.setButton(DialogInterface.BUTTON_NEGATIVE,
						getString(R.string.stop),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {

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
