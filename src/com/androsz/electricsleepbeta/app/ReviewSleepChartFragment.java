package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepRecord;
import com.androsz.electricsleepbeta.widget.SleepChart;

public class ReviewSleepChartFragment extends AnalyticFragment {

	SleepRecord sleepRecord;

	public void setSleepRecord(SleepRecord sleepRecord) {
		this.sleepRecord = sleepRecord;
		if (sleepChart != null) {
			sleepChart.sync(sleepRecord);
		}
	}

	SleepChart sleepChart;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_review_sleep_chart,
				container, false);

		sleepChart = (SleepChart) view.findViewById(R.id.sleep_movement_chart);
		if (sleepRecord != null) {
			setSleepRecord(sleepRecord);
		}

		return view;
	}

	@Override
	public void onClick(View v) {
	}

}
