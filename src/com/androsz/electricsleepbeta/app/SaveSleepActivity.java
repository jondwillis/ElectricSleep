package com.androsz.electricsleepbeta.app;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.Toast;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.alarmclock.Alarms;
import com.androsz.electricsleepbeta.content.SaveSleepReceiver;

public class SaveSleepActivity extends HostActivity implements OnRatingBarChangeListener {

	public static final String SAVE_SLEEP = "com.androsz.electricsleepbeta.SAVE_SLEEP";

	EditText noteEdit;

	ProgressDialog progress;

	private float rating = Float.NaN;

	private final BroadcastReceiver saveCompletedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {

			new AsyncTask<Void, Void, String>() {

				@Override
				protected String doInBackground(Void... params) {
					// makes sure the next alert is set (fixes a bug with
					// snoozing
					// disabling repeated alarms)
					Alarms.setNextAlert(context);
					final Intent reviewSleepIntent = new Intent(context, ReviewSleepActivity.class);
					if (!intent.getBooleanExtra(SaveSleepReceiver.EXTRA_SUCCESS, false)) {
						String why = getString(R.string.could_not_save_sleep) + " ";
						final String ioException = intent
								.getStringExtra(SaveSleepReceiver.EXTRA_IO_EXCEPTION);
						if (ioException != null) {
							why += ioException;
						} else {
							why += getString(R.string.sleep_too_brief_to_analyze);
						}
						return why;
					}
					final Uri uri = Uri.parse(intent.getStringExtra(SaveSleepReceiver.EXTRA_URI));
					if (uri != null) {
						reviewSleepIntent.setData(uri);
					}

					reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
							| Intent.FLAG_ACTIVITY_NEW_TASK);

					startActivity(reviewSleepIntent);
					return null;
				}

				@Override
				protected void onPostExecute(String result) {
					if (result != null) {
						trackEvent(result, 0);
						Toast.makeText(context, result, Toast.LENGTH_LONG).show();
					}
					progress.dismiss();
					finish();
				}
			}.execute();

		}
	};

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_save_sleep;
	}

	@Override
	// @SuppressWarnings("unchecked")
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		((RatingBar) findViewById(R.id.save_sleep_rating_bar)).setOnRatingBarChangeListener(this);
		noteEdit = (EditText) findViewById(R.id.save_sleep_note_edit);
		getSupportActionBar().setDisplayHomeAsUpEnabled(false);
	}

	public void onDiscardClick(final View v) {
		final AlertDialog.Builder dialog = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.delete_sleep_record))
				.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog, final int id) {
						final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
						notificationManager.cancel(getIntent().getExtras().getInt(
								SleepMonitoringService.EXTRA_ID));
						// new Thread(new Runnable(){

						// @Override
						// public void run() {
						// deleteFile(SleepMonitoringService.SLEEP_DATA);
						// }}){}.run();
						finish();
					}
				})
				.setNegativeButton(getString(R.string.cancel),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(final DialogInterface dialog, final int id) {
								dialog.cancel();
							}
						});
		dialog.show();
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(saveCompletedReceiver);
	}

	@Override
	public void onRatingChanged(final RatingBar ratingBar, final float rating,
			final boolean fromUser) {
		if (fromUser) {
			this.rating = rating;
		}
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		rating = savedState.getFloat(SaveSleepReceiver.EXTRA_RATING);
	}

	@Override
	public void onResume() {
		super.onResume();
		registerReceiver(saveCompletedReceiver, new IntentFilter(
				SaveSleepReceiver.SAVE_SLEEP_COMPLETED));
	}

	public void onSaveClick(final View v) {

		if (Float.isNaN(rating)) {
			Toast.makeText(this, R.string.error_not_rated, Toast.LENGTH_SHORT).show();
			return;
		}

		final Intent saveIntent = new Intent(SaveSleepActivity.SAVE_SLEEP);
		saveIntent.putExtra(SaveSleepReceiver.EXTRA_NOTE, noteEdit.getText().toString());
		saveIntent.putExtra(SaveSleepReceiver.EXTRA_RATING, (int) rating);
		saveIntent.putExtras(getIntent().getExtras()); // add the sleep history
														// data

		v.setEnabled(false);
		progress = new ProgressDialog(this);
		progress.setMessage(getString(R.string.saving_sleep));
		progress.show();
		sendBroadcast(saveIntent);
		final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(getIntent().getExtras().getInt(SleepMonitoringService.EXTRA_ID));
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putFloat(SaveSleepReceiver.EXTRA_RATING, rating);
	}
}
