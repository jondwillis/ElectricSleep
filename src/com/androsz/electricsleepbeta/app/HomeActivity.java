package com.androsz.electricsleepbeta.app;

import java.io.IOException;
import java.io.StreamCorruptedException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.alarmclock.AlarmClock;
import com.androsz.electricsleepbeta.content.StartSleepReceiver;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.db.SleepSessions;
import com.androsz.electricsleepbeta.util.MathUtils;
import com.androsz.electricsleepbeta.widget.SleepChart;

/**
 * Front-door {@link Activity} that displays high-level features the application
 * offers to users.
 */
public class HomeActivity extends HostActivity implements LoaderManager.LoaderCallbacks<Cursor> {

	private SleepChart sleepChart;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_home;
	}

	public void onAlarmsClick(final View v) {
		startActivity(new Intent(this, AlarmClock.class));
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		final ActionBar bar = getSupportActionBar();
		bar.setDisplayHomeAsUpEnabled(false);
		new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				PreferenceManager.setDefaultValues(HomeActivity.this, R.xml.settings, false);
				final SharedPreferences userPrefs = getSharedPreferences(
						SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE);
				final int prefsVersion = userPrefs.getInt(SettingsActivity.PREFERENCES_ENVIRONMENT,
						0);
				if (prefsVersion == 0) {
					startActivity(new Intent(HomeActivity.this, WelcomeTutorialWizardActivity.class)
							.putExtra("required", true));
				} else {

					if (WelcomeTutorialWizardActivity
							.enforceCalibrationBeforeStartingSleep(HomeActivity.this)) {
					}
				}
				return null;
			}
		}.execute();

		sleepChart = (SleepChart) findViewById(R.id.home_sleep_chart);

		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(this, SleepSessions.MainTable.CONTENT_URI,
				SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, null, null, SleepSessions.MainTable.KEY_ROW_ID + " DESC");
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		// cancel home as up
		if (item.getItemId() == android.R.id.home) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onHistoryClick(final View v) {
		startActivity(new Intent(this, HistoryMonthActivity.class));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, final Cursor cursor) {
		final TextView lastSleepTitleText = (TextView) findViewById(R.id.home_last_sleep_title_text);
		final TextView reviewTitleText = (TextView) findViewById(R.id.home_review_title_text);
		//final ViewGroup container = (ViewGroup) findViewById(R.id.home_stats_container);
		final ViewGroup statsContainer = (ViewGroup) findViewById(R.id.home_statistics_dashboard);
		if (cursor == null || cursor.getCount() == 0) {
			statsContainer.setVisibility(View.GONE);
			sleepChart.setVisibility(View.GONE);
			reviewTitleText.setText(getString(R.string.home_review_title_text_empty));
			lastSleepTitleText.setVisibility(View.GONE);
			lastSleepTitleText.setText(getString(R.string.home_last_sleep_title_text_empty));
		} else {

			final TextView avgScoreText = (TextView) findViewById(R.id.value_score_text);
			final TextView avgDurationText = (TextView) findViewById(R.id.value_duration_text);
			final TextView avgSpikesText = (TextView) findViewById(R.id.value_spikes_text);
			final TextView avgFellAsleepText = (TextView) findViewById(R.id.value_fell_asleep_text);
			cursor.moveToFirst();

			try {
				sleepChart.sync(cursor);
			} catch (StreamCorruptedException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			final long sleepChartRowId = cursor.getLong(0);
			sleepChart.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					final Intent reviewSleepIntent = new Intent(HomeActivity.this,
							ReviewSleepActivity.class);

					final Uri data = Uri.withAppendedPath(SleepSessions.MainTable.CONTENT_ID_URI_BASE,
							String.valueOf(sleepChartRowId));
					reviewSleepIntent.setData(data);
					startActivity(reviewSleepIntent);
				}
			});

			sleepChart.setMinimumHeight(MathUtils.getAbsoluteScreenHeightPx(HomeActivity.this) / 2);
			lastSleepTitleText.setText(getString(R.string.home_last_sleep_title_text));
			sleepChart.setVisibility(View.VISIBLE);
			lastSleepTitleText.setVisibility(View.VISIBLE);

			new AsyncTask<Void, Void, Void>() {
				int avgSleepScore = 0;
				long avgDuration = 0;
				int avgSpikes = 0;
				long avgFellAsleep = 0;

				@Override
				protected Void doInBackground(Void... params) {
					int count = 0;
					do {
						count++;
						SleepSession sleepRecord = null;
						try {
							sleepRecord = new SleepSession(cursor);
						} catch (final CursorIndexOutOfBoundsException cioobe) {
							// there are no records!
							return null;
						}
						avgSleepScore += sleepRecord.getSleepScore();
						avgDuration += sleepRecord.duration;
						avgSpikes += sleepRecord.spikes;
						avgFellAsleep += sleepRecord.getTimeToFallAsleep();

					} while (cursor.moveToNext());

					final float invCount = 1.0f / count;
					avgSleepScore *= invCount;
					avgDuration *= invCount;
					avgSpikes *= invCount;
					avgFellAsleep *= invCount;
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					avgScoreText.setText(avgSleepScore + "%");
					avgDurationText.setText(SleepSession.getTimespanText(avgDuration,
							getResources()));
					avgSpikesText.setText(avgSpikes + "");
					avgFellAsleepText.setText(SleepSession.getTimespanText(avgFellAsleep,
							getResources()));
					reviewTitleText.setText(getString(R.string.home_review_title_text));
					statsContainer.setVisibility(View.VISIBLE);
					super.onPostExecute(result);
				}
			}.execute();

		}
	}

	public void onSleepClick(final View v) throws Exception {
		sendBroadcast(new Intent(StartSleepReceiver.START_SLEEP));
	}
}
