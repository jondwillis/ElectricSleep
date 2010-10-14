package com.androsz.electricsleep.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;

import com.androsz.electricsleep.R;

public class SaveSleepActivity extends CustomTitlebarActivity implements
		OnRatingBarChangeListener {

	public static final String SAVE_SLEEP = "com.androsz.electricsleep.SAVE_SLEEP";

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_save_sleep;
	}

	@Override
	// @SuppressWarnings("unchecked")
	protected void onCreate(final Bundle savedInstanceState) {

		// registerReceiver(saveSleepReceiver, new IntentFilter(SAVE_SLEEP));
		super.onCreate(savedInstanceState);
	}

	public void onSaveClick(final View v) {
		final Intent saveIntent = new Intent(SaveSleepActivity.SAVE_SLEEP);
		((RatingBar) findViewById(R.id.save_sleep_rating_bar))
				.setOnRatingBarChangeListener(this);

		getIntent().putExtra("rating", rating);
		saveIntent.putExtras(getIntent().getExtras());
		sendBroadcast(saveIntent);
		finish();
	}

	private float rating = Float.NaN;

	@Override
	public void onRatingChanged(RatingBar ratingBar, float rating,
			boolean fromUser) {
		this.rating = rating;

	}
}
