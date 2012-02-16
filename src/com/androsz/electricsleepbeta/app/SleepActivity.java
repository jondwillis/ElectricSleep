package com.androsz.electricsleepbeta.app;

import java.util.Calendar;
import java.util.List;

import org.achartengine.model.PointD;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.alarmclock.Alarm;
import com.androsz.electricsleepbeta.alarmclock.AlarmClock;
import com.androsz.electricsleepbeta.alarmclock.Alarms;
import com.androsz.electricsleepbeta.content.StartSleepReceiver;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class SleepActivity extends HostActivity {
    private class DimScreenTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(final Void... params) {
            // just wait without blocking the main thread!
            try {
                Thread.sleep(DIM_SCREEN_AFTER_MS);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(final Void results) {
            // after we have waited, dim the screen on the main thread!
            startActivity(new Intent(SleepActivity.this, DimSleepActivity.class));
        }

        @Override
        protected void onPreExecute() {
            // notify the user that we've received that they need a dimmed
            // screen
            Toast.makeText(SleepActivity.this, R.string.screen_will_dim,
                    Toast.LENGTH_LONG).show();
        }
    }

    private static final int DIM_SCREEN_AFTER_MS = 15000;

    private static final String SLEEP_CHART = "sleepChart";

    public static final String SYNC_CHART = "com.androsz.electricsleepbeta.SYNC_CHART";

    public static final String UPDATE_CHART = "com.androsz.electricsleepbeta.UPDATE_CHART";

    private final BroadcastReceiver batteryChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            pluggedIn = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) > 0;
            airplaneModeNagChanged();
        }
    };

    AsyncTask<Void, Void, Void> dimScreenTask;
    private SleepChart sleepChart;
    private final BroadcastReceiver sleepStoppedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            finish();
        }
    };
    private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(final Context context, final Intent intent) {

            List<PointD> points = (List<PointD>) intent
                    .getSerializableExtra(SleepMonitoringService.SLEEP_DATA);
            for (PointD point : points) {
                sleepChart.xySeriesMovement.add(point.x, point.y);
            }

            final double alarmTriggerSensitivity = intent.getDoubleExtra(
                    StartSleepReceiver.EXTRA_ALARM,
                    SettingsActivity.DEFAULT_ALARM_SENSITIVITY);
            sleepChart.setCalibrationLevel(alarmTriggerSensitivity);
            sleepChart.reconfigure();
            sleepChart.repaint();

            final boolean useAlarm = intent.getBooleanExtra(
                    StartSleepReceiver.EXTRA_USE_ALARM, false);
            final boolean forceScreenOn = intent.getBooleanExtra(
                    StartSleepReceiver.EXTRA_FORCE_SCREEN_ON, false);

            // Shows the bound to alarm toast if useAlarm is enabled
            if (useAlarm) {
                new AsyncTask<Void, Void, String[]>() {
                    @Override
                    protected String[] doInBackground(Void... params) {
                        String[] result = null;
                        final Alarm alarm = Alarms.calculateNextAlert(context);
                        // try {
                        if (alarm != null) {
                            final Calendar alarmTime = Calendar.getInstance();
                            alarmTime.setTimeInMillis(alarm.time);

                            java.text.DateFormat df = DateFormat
                                    .getDateFormat(context);
                            df = DateFormat.getTimeFormat(context);
                            final String dateTime = df.format(alarmTime
                                    .getTime());
                            final int alarmWindow = intent.getIntExtra(
                                    StartSleepReceiver.EXTRA_ALARM_WINDOW, 30);
                            alarmTime.add(Calendar.MINUTE, -1 * alarmWindow);
                            final String dateTimePre = df.format(alarmTime
                                    .getTime());
                            result = new String[] { dateTimePre, dateTime };
                        }
                        // } catch (final Exception e) {
                        // Log.d(TAG, "");
                        // }
                        return result;
                    }

                    @Override
                    protected void onPostExecute(String[] result) {
                        if (result != null) {
                            sleepChart.xyMultipleSeriesRenderer
                                    .setChartTitle(context.getString(
                                            R.string.you_will_be_awoken_before,
                                            result[0], result[1]));
                            buttonSleepNoAlarm.setVisibility(View.GONE);
                        } else {
                            sleepChart.xyMultipleSeriesRenderer
                                    .setChartTitle("");
                            buttonSleepNoAlarm.setVisibility(View.VISIBLE);
                        }
                        // dims the screen while in this activity and
                        // forceScreenOn is
                        // enabled
                        if (forceScreenOn) {
                            buttonSleepDim.setVisibility(View.VISIBLE);

                            cancelDimScreenTask();
                            dimScreenTask = new DimScreenTask();
                            dimScreenTask.execute();

                        } else {
                            buttonSleepDim.setVisibility(View.GONE);
                        }

                        if (sleepChart.makesSenseToDisplay()) {
                            sleepChart.setVisibility(View.VISIBLE);
                            waitForSleepData.setVisibility(View.GONE);
                        } else {
                            showWaitForSeriesDataIfNeeded();
                        }
                        showOrHideWarnings();
                    }
                }.execute();
            } else {
                sleepChart.xyMultipleSeriesRenderer.setChartTitle("");
                buttonSleepNoAlarm.setVisibility(View.VISIBLE);
                showOrHideWarnings();
            }
        }
    };

    private Button buttonSleepDim;

    private Button buttonSleepNoAlarm;

    private Button buttonSleepPluggedIn;

    private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            sleepChart.sync(intent.getDoubleExtra(
                    SleepMonitoringService.EXTRA_X, 0), intent.getDoubleExtra(
                    SleepMonitoringService.EXTRA_Y, 0), intent.getDoubleExtra(
                    StartSleepReceiver.EXTRA_ALARM,
                    SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

            if (sleepChart.makesSenseToDisplay()) {
                sleepChart.setVisibility(View.VISIBLE);
                waitForSleepData.setVisibility(View.GONE);
            } else {
                sleepChart.setVisibility(View.GONE);
                waitForSleepData.setVisibility(View.VISIBLE);
            }
        }
    };

    private ViewGroup waitForSleepData;

    @Override
    protected int getContentAreaLayoutId() {
        return R.layout.activity_sleep;
    }

    boolean pluggedIn = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.monitoring_sleep);
        airplaneModeOn = Settings.System.getInt(getContentResolver(),
                Settings.System.AIRPLANE_MODE_ON, 0) != 0;

        registerReceiver(sleepStoppedReceiver, new IntentFilter(
                SleepMonitoringService.SLEEP_STOPPED));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_monitoring_sleep, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(sleepStoppedReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_item_alarms:
            startActivity(new Intent(this, AlarmClock.class));
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.button_tracking_stop:
            sendBroadcast(new Intent(SleepMonitoringService.STOP_AND_SAVE_SLEEP));
            finish();
            break;
        }
    }

    @Override
    protected void onPause() {
        unregisterReceiver(airplaneModeChangedReceiver);
        unregisterReceiver(updateChartReceiver);
        unregisterReceiver(syncChartReceiver);
        unregisterReceiver(batteryChangedReceiver);
        cancelDimScreenTask();
        super.onPause();
    }

    private void cancelDimScreenTask() {
        // cancel the dim screen task if it hasn't completed
        if (dimScreenTask != null) {
            dimScreenTask.cancel(true);
            Toast.makeText(
                    this,
                    "Warning: dim sleep mode can only occur on the Sleep screen.",
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedState) {
        sleepChart = (SleepChart) savedState.getParcelable(SLEEP_CHART);
        super.onRestoreInstanceState(savedState);
    }

    boolean airplaneModeOn = false;
    BroadcastReceiver airplaneModeChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            airplaneModeOn = intent.getBooleanExtra("state", false);
            airplaneModeNagChanged();
        }

    };

    private void airplaneModeNagChanged() {
        final int visibility = (pluggedIn || airplaneModeOn ? View.GONE
                : View.VISIBLE);

        buttonSleepPluggedIn.setVisibility(visibility);

        showOrHideWarnings();
    }

    @Override
    protected void onResume() {
        sleepChart = (SleepChart) findViewById(R.id.sleep_movement_chart);
        waitForSleepData = (ViewGroup) findViewById(R.id.wait_for_sleep_data);
        buttonSleepNoAlarm = (Button) findViewById(R.id.text_sleep_no_alarm);
        buttonSleepDim = (Button) findViewById(R.id.text_sleep_dim);
        buttonSleepPluggedIn = (Button) findViewById(R.id.text_sleep_plugged_in);

        registerReceiver(airplaneModeChangedReceiver, new IntentFilter(
                Intent.ACTION_AIRPLANE_MODE_CHANGED));
        registerReceiver(updateChartReceiver, new IntentFilter(UPDATE_CHART));
        registerReceiver(syncChartReceiver, new IntentFilter(SYNC_CHART));
        registerReceiver(batteryChangedReceiver, new IntentFilter(
                Intent.ACTION_BATTERY_CHANGED));
        sendBroadcast(new Intent(SleepMonitoringService.POKE_SYNC_CHART));

        super.onResume();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SLEEP_CHART, sleepChart);
    }

    private void showOrHideWarnings() {
        // hide warnings panel if there's no warnings.
        final ScrollView landscapeWarnings = (ScrollView) findViewById(R.id.sleep_landscape_warnings);
        // make sure we're in landscape. portrait doesn't have this problem.
        if (landscapeWarnings != null) {
            int visibility = buttonSleepPluggedIn.getVisibility()
                    + buttonSleepDim.getVisibility()
                    + buttonSleepNoAlarm.getVisibility();

            // if all are gone...
            visibility = (visibility == (View.GONE * 3)) ? View.GONE
                    : View.VISIBLE;
            landscapeWarnings.setVisibility(visibility);
        }
    }

    private void showWaitForSeriesDataIfNeeded() {
        if (sleepChart == null || !sleepChart.makesSenseToDisplay()) {
            sleepChart.setVisibility(View.GONE);
            waitForSleepData.setVisibility(View.VISIBLE);
        }
    }
}
