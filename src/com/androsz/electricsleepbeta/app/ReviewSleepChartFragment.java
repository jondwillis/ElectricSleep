package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class ReviewSleepChartFragment extends HostFragment {

	SleepChart sleepChart;

	SleepSession sleepRecord;

	@Override
	public void onClick(View v) {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_review_sleep_chart, container, false);
        sleepChart = (SleepChart) root.findViewById(R.id.sleep_movement_chart);
		if (sleepRecord != null) {
			setSleepRecord(sleepRecord);
		}

		return root;
	}

	public void setSleepRecord(SleepSession sleepRecord) {
		this.sleepRecord = sleepRecord;
		if (sleepChart != null) {
			sleepChart.sync(sleepRecord);
		}
	}

}
