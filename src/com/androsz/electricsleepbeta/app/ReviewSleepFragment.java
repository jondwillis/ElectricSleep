/* @(#)ReviewSleepFragment.java
 *
 *========================================================================
 * Copyright 2011 by Zeo Inc. All Rights Reserved
 *========================================================================
 *
 * Date: $Date$
 * Author: Jon Willis
 * Author: Brandon Edens <brandon.edens@myzeo.com>
 * Version: $Revision$
 */

package com.androsz.electricsleepbeta.app;

import static com.androsz.electricsleepbeta.util.IntentUtil.shareSleep;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.SleepChart;

/**
 * Fragment used to review a sleep record.
 *
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class ReviewSleepFragment extends AnalyticFragment {

	private static final String TAG = ReviewSleepFragment.class.getSimpleName();

	/** Row ID of the sleep record to review. */
	public static final String KEY_SLEEP_ID = "key_sleep_id";

	private SleepSession mSleepRecord;

	private SleepChart mSleepChart;
	private RatingBar mMorningFeel;
	private TextView mSleepEfficiency;
	private TextView mTotalRecordingTime;
	private TextView mTimesDisrupted;
	private TextView mTimeToFallAsleep;
	private TextView mNotes;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_review_sleep,
				container, false);
		mSleepChart = (SleepChart) root.findViewById(R.id.sleep_movement_chart);

		mMorningFeel = (RatingBar) root.findViewById(R.id.morning_feel);
		mSleepEfficiency = (TextView) root.findViewById(R.id.sleep_efficiency);
		mTotalRecordingTime = (TextView) root
				.findViewById(R.id.total_recording_time);
		mTimesDisrupted = (TextView) root.findViewById(R.id.times_disrupted);
		mTimeToFallAsleep = (TextView) root
				.findViewById(R.id.time_to_fall_asleep);
		mNotes = (TextView) root.findViewById(R.id.notes);

		if (mSleepRecord != null) {
			setSleepRecord(mSleepRecord);
		}

		return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_review_sleep, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_item_share_sleep_record:
			shareSleep(mSleepRecord, getActivity());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void setSleepRecord(SleepSession sleepRecord) {
		if (sleepRecord == null) {
			return;
		}

		Log.d(TAG, "Setting sleep record.");
		mSleepChart.sync(sleepRecord);
		mMorningFeel.setRating(sleepRecord.getRating());
		mSleepEfficiency.setText(sleepRecord.getEfficiency());
		mTotalRecordingTime.setText(sleepRecord
				.getTotalRecordTime(getResources()));
		mTimesDisrupted.setText(sleepRecord.getTimesDisrupted());
		mTimeToFallAsleep.setText(sleepRecord
				.getTimeToFallAsleepText(getResources()));
		mNotes.setText(sleepRecord.getNotes());

		mSleepRecord = sleepRecord;
	}
}
