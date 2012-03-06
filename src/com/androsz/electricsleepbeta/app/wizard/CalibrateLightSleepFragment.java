package com.androsz.electricsleepbeta.app.wizard;

import java.util.List;

import org.achartengine.model.PointD;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.LayoutFragment;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.widget.DecimalSeekBar;
import com.androsz.electricsleepbeta.widget.SleepChart;
import com.androsz.electricsleepbeta.widget.VerticalSeekBar;

public class CalibrateLightSleepFragment extends LayoutFragment
    implements Calibrator {

    private float mAlarmTrigger;

    private SharedPreferences mPrefs;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mPrefs = getActivity().getSharedPreferences(SettingsActivity.PREFERENCES, 0);
        mAlarmTrigger =
            mPrefs.getFloat(getActivity().getString(R.string.pref_alarm_trigger_sensitivity),
                            SettingsActivity.DEFAULT_ALARM_SENSITIVITY);

        sleepChart = (SleepChart) getActivity().findViewById(
                R.id.calibration_sleep_chart);

        final VerticalSeekBar seekBar = (VerticalSeekBar) getActivity()
                .findViewById(R.id.calibration_level_seekbar);
        seekBar.setMax((int) SettingsActivity.MAX_ALARM_SENSITIVITY);
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(final SeekBar seekBar,
                    final int progress, final boolean fromUser) {
                if (fromUser) {
                    sleepChart.setCalibrationLevel(progress / DecimalSeekBar.PRECISION);
                }
            }

            @Override
            public void onStartTrackingTouch(final SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
            }
        });

        sleepChart.clear();
        sleepChart.setVisibility(View.INVISIBLE);
        getActivity().findViewById(R.id.calibration_level_seekbar)
                .setVisibility(View.INVISIBLE);
        getActivity().findViewById(R.id.warming_up_text).setVisibility(
                View.VISIBLE);
        sleepChart.setCalibrationLevel(mAlarmTrigger);

        getActivity().getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private static final String SLEEP_CHART = "sleepChart";

    SleepChart sleepChart;

    private final BroadcastReceiver syncChartReceiver = new BroadcastReceiver() {
        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(final Context context, final Intent intent) {

            // throw new Exception("UHHHH");
            sleepChart = (SleepChart) getActivity().findViewById(
                    R.id.calibration_sleep_chart);

            List<PointD> points = (List<PointD>) intent
                    .getSerializableExtra(SleepMonitoringService.SLEEP_DATA);
            for (PointD point : points) {
                sleepChart.addPoint(point.x, point.y);
            }

            sleepChart.reconfigure();
            sleepChart.repaint();
        }
    };

    private final BroadcastReceiver updateChartReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            /*
             * CalibrateAlarmActivity.this .setResult( CALIBRATION_SUCCEEDED,
             * new Intent().putExtra("y", sleepChart.getCalibrationLevel()));
             */
            getActivity().findViewById(R.id.calibration_sleep_chart)
                    .setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.calibration_level_seekbar)
                    .setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.warming_up_text).setVisibility(
                    View.GONE);
            if (sleepChart != null) {
                final VerticalSeekBar seekBar = (VerticalSeekBar) getActivity()
                        .findViewById(R.id.calibration_level_seekbar);
                if (sleepChart.hasCalibrationLevel()) {
                    seekBar.setProgress((float) sleepChart.getCalibrationLevel());
                    sleepChart.sync(intent.getDoubleExtra(
                                        SleepMonitoringService.EXTRA_X, 0), intent
                                    .getDoubleExtra(SleepMonitoringService.EXTRA_Y, 0),
                                    sleepChart.getCalibrationLevel());
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(updateChartReceiver,
                new IntentFilter(SleepActivity.UPDATE_CHART));
        getActivity().registerReceiver(syncChartReceiver,
                new IntentFilter(SleepActivity.SYNC_CHART));

        mAlarmTrigger =
            mPrefs.getFloat(getActivity().getString(R.string.pref_alarm_trigger_sensitivity),
                            SettingsActivity.DEFAULT_ALARM_SENSITIVITY);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopCalibration(getActivity());
        getActivity().unregisterReceiver(updateChartReceiver);
        getActivity().unregisterReceiver(syncChartReceiver);

        // Save the trigger sensitivity
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putFloat(getActivity().getString(R.string.pref_alarm_trigger_sensitivity),
                        mAlarmTrigger);
        editor.commit();
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.wizard_calibration_lightsleep;
    }

    @Override
    public void startCalibration(Context context) {
        final Intent i = new Intent(context, SleepMonitoringService.class);
        context.stopService(i);
        i.putExtra("testModeRate",
                CalibrationWizardActivity.LIGHT_SLEEP_CALIBRATION_INTERVAL);
        i.putExtra("alarm", SettingsActivity.MAX_ALARM_SENSITIVITY);
        context.startService(i);
    }

    @Override
    public void stopCalibration(Context context) {
        context.stopService(new Intent(context, SleepMonitoringService.class));
    }
}