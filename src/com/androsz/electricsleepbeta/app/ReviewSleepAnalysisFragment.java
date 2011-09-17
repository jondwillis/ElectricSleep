package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;

public class ReviewSleepAnalysisFragment extends HostFragment {

	RatingBar ratingRB;

	TextView scoreTV, durationTV, spikesTV, fellAsleepTV, noteTV;

	SleepSession sleepRecord;

	@Override
	public void onClick(View v) {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = super.onCreateView(inflater, container, savedInstanceState);
		scoreTV = (TextView) view.findViewById(R.id.value_score_text);
		durationTV = (TextView) view.findViewById(R.id.value_duration_text);
		spikesTV = (TextView) view.findViewById(R.id.value_spikes_text);
		fellAsleepTV = (TextView) view
				.findViewById(R.id.value_fell_asleep_text);
		noteTV = (TextView) view.findViewById(R.id.value_note_text);

		ratingRB = (RatingBar) view.findViewById(R.id.value_rating_bar);

		if (sleepRecord != null) {
			setSleepRecord(sleepRecord);
		}

		return view;
	}

	public void setSleepRecord(SleepSession sleepRecord) {
		this.sleepRecord = sleepRecord;
		if (scoreTV != null) {
			scoreTV.setText(sleepRecord.getSleepScore() + "%");
			durationTV.setText(sleepRecord.getDurationText(getResources()));
			spikesTV.setText(Integer.toString(sleepRecord.spikes));
			fellAsleepTV.setText(sleepRecord.getFellAsleepText(getResources()));
			noteTV.setText(sleepRecord.note);

			ratingRB.setRating(sleepRecord.rating);
		}
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.fragment_review_sleep_analysis;
	}
}
