package com.androsz.electricsleep.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Toast;

import com.androsz.electricsleep.R;

public class SaveSleepActivity extends CustomTitlebarActivity implements
		OnRatingBarChangeListener {

	public static final String SAVE_SLEEP = "com.androsz.electricsleep.SAVE_SLEEP";

	private float rating = Float.NaN;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_save_sleep;
	}

	@Override
	// @SuppressWarnings("unchecked")
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((RatingBar) findViewById(R.id.save_sleep_rating_bar))
				.setOnRatingBarChangeListener(this);
	}

	@Override
	public void onRatingChanged(final RatingBar ratingBar, final float rating,
			final boolean fromUser) {
		if (fromUser) {
			this.rating = rating;
		}
	}

	public void onSaveClick(final View v) {

		if (Float.isNaN(rating)) {
			Toast.makeText(this, R.string.error_not_rated, Toast.LENGTH_SHORT)
					.show();
			return;
		}

		getIntent().putExtra("rating", rating);

		final Intent saveIntent = new Intent(SaveSleepActivity.SAVE_SLEEP);
		saveIntent.putExtras(getIntent().getExtras());
		sendBroadcast(saveIntent);
		finish();
	}
	
	public void onDiscardClick(final View v) {
		finish();
	}
}
