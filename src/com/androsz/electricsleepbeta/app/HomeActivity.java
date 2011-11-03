package com.androsz.electricsleepbeta.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;
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

	/*
	 * private class LoadLastSleepChartTask extends AsyncTask<String, Void,
	 * Cursor> {
	 * 
	 * @Override protected Cursor doInBackground(String... params) { // TODO:
	 * CursorLoader return SleepSessions.getSleepMatches(HomeActivity.this,
	 * params[0], new String[] { SleepSessions.MainTable.KEY_TITLE });//
	 * managedQuery(SleepSessions.MainTable.CONTENT_URI, // null, // null, //
	 * new String[] { params[0] }, SleepSessions.MainTable.KEY_TITLE); }
	 * 
	 * @Override protected void onPostExecute(final Cursor cursor) { final
	 * TextView lastSleepTitleText = (TextView)
	 * findViewById(R.id.home_last_sleep_title_text); final TextView
	 * reviewTitleText = (TextView) findViewById(R.id.home_review_title_text);
	 * final ViewGroup container = (ViewGroup)
	 * findViewById(R.id.home_stats_container); if (cursor == null) {
	 * container.setVisibility(View.GONE); reviewTitleText
	 * .setText(getString(R.string.home_review_title_text_empty));
	 * lastSleepTitleText
	 * .setText(getString(R.string.home_last_sleep_title_text_empty)); } else {
	 * 
	 * final TextView avgScoreText = (TextView)
	 * findViewById(R.id.value_score_text); final TextView avgDurationText =
	 * (TextView) findViewById(R.id.value_duration_text); final TextView
	 * avgSpikesText = (TextView) findViewById(R.id.value_spikes_text); final
	 * TextView avgFellAsleepText = (TextView)
	 * findViewById(R.id.value_fell_asleep_text); cursor.moveToLast(); try {
	 * sleepChart.sync(cursor); } catch (final StreamCorruptedException e) {
	 * e.printStackTrace(); } catch (final IllegalArgumentException e) {
	 * e.printStackTrace(); } catch (final IOException e) { e.printStackTrace();
	 * } catch (final ClassNotFoundException e) { e.printStackTrace(); }
	 * sleepChart.setMinimumHeight(MathUtils
	 * .getAbsoluteScreenHeightPx(HomeActivity.this) / 3); lastSleepTitleText
	 * .setText(getString(R.string.home_last_sleep_title_text));
	 * 
	 * cursor.moveToFirst(); int avgSleepScore = 0; long avgDuration = 0; int
	 * avgSpikes = 0; long avgFellAsleep = 0; int count = 0; do { count++; final
	 * SleepSession sleepRecord = new SleepSession(cursor); avgSleepScore +=
	 * sleepRecord.getSleepScore(); avgDuration += sleepRecord.duration;
	 * avgSpikes += sleepRecord.spikes; avgFellAsleep +=
	 * sleepRecord.getTimeToFallAsleep(); } while (cursor.moveToNext());
	 * 
	 * final float invCount = 1.0f / count; avgSleepScore *= invCount;
	 * avgDuration *= invCount; avgSpikes *= invCount; avgFellAsleep *=
	 * invCount;
	 * 
	 * avgScoreText.setText(avgSleepScore + "%");
	 * avgDurationText.setText(SleepSession.getTimespanText( avgDuration,
	 * getResources())); avgSpikesText.setText(avgSpikes + "");
	 * avgFellAsleepText.setText(SleepSession.getTimespanText( avgFellAsleep,
	 * getResources()));
	 * 
	 * reviewTitleText .setText(getString(R.string.home_review_title_text));
	 * container.setVisibility(View.VISIBLE);
	 * sleepChart.setVisibility(View.VISIBLE); } }
	 * 
	 * @Override protected void onPreExecute() { sleepChart = (SleepChart)
	 * findViewById(R.id.home_sleep_chart); }
	 * 
	 * }
	 */

	// LoadLastSleepChartTask loadLastSleepChartTask;

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
				SleepSessions.MainTable.ALL_COLUMNS_PROJECTION, null, null, null);
	}

	/*
	 * Used for overriding default HostActivity behavior..
	 * @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final boolean result = super.onCreateOptionsMenu(menu);
		menu.findItem(R.id.menu_item_donate).setShowAsAction(
				MenuItem.SHOW_AS_ACTION_WITH_TEXT | MenuItem.SHOW_AS_ACTION_IF_ROOM);
		menu.findItem(R.id.menu_item_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		return result;
	}*/

	public void onHistoryClick(final View v) {
		startActivity(new Intent(this, HistoryMonthActivity.class));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {

	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
		final TextView lastSleepTitleText = (TextView) findViewById(R.id.home_last_sleep_title_text);
		final TextView reviewTitleText = (TextView) findViewById(R.id.home_review_title_text);
		final ViewGroup container = (ViewGroup) findViewById(R.id.home_stats_container);
		if (cursor == null || cursor.getCount() == 0) {
			container.setVisibility(View.GONE);
			reviewTitleText.setText(getString(R.string.home_review_title_text_empty));
			lastSleepTitleText.setText(getString(R.string.home_last_sleep_title_text_empty));
		} else {

			final TextView avgScoreText = (TextView) findViewById(R.id.value_score_text);
			final TextView avgDurationText = (TextView) findViewById(R.id.value_duration_text);
			final TextView avgSpikesText = (TextView) findViewById(R.id.value_spikes_text);
			final TextView avgFellAsleepText = (TextView) findViewById(R.id.value_fell_asleep_text);
			cursor.moveToLast();

			try {
				sleepChart.sync(cursor);
			} catch (final Exception e) {
			}
			sleepChart.setMinimumHeight(MathUtils.getAbsoluteScreenHeightPx(HomeActivity.this) / 3);
			lastSleepTitleText.setText(getString(R.string.home_last_sleep_title_text));

			cursor.moveToFirst();
			int avgSleepScore = 0;
			long avgDuration = 0;
			int avgSpikes = 0;
			long avgFellAsleep = 0;
			int count = 0;
			do {
				count++;
				SleepSession sleepRecord = null;
				try {
					sleepRecord = new SleepSession(cursor);
				} catch (final CursorIndexOutOfBoundsException cioobe) {
					// there are no records!
					return;
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

			avgScoreText.setText(avgSleepScore + "%");
			avgDurationText.setText(SleepSession.getTimespanText(avgDuration, getResources()));
			avgSpikesText.setText(avgSpikes + "");
			avgFellAsleepText.setText(SleepSession.getTimespanText(avgFellAsleep, getResources()));

			reviewTitleText.setText(getString(R.string.home_review_title_text));
			container.setVisibility(View.VISIBLE);
			sleepChart.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		// if (loadLastSleepChartTask != null) {
		// loadLastSleepChartTask.cancel(true);
		// }
	}

	@Override
	protected void onResume() {
		super.onResume();
		// if (loadLastSleepChartTask != null) {
		// loadLastSleepChartTask.cancel(true);
		// }
		// loadLastSleepChartTask = new LoadLastSleepChartTask();
		// loadLastSleepChartTask.execute(getString(R.string.to));
	}

	public void onSleepClick(final View v) throws Exception {
		sendBroadcast(new Intent(StartSleepReceiver.START_SLEEP));
	}
}
