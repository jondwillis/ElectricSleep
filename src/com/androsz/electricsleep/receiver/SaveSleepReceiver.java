package com.androsz.electricsleep.receiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;
import com.androsz.electricsleep.ui.ReviewSleepActivity;
import com.androsz.electricsleep.ui.SettingsActivity;

public class SaveSleepReceiver extends BroadcastReceiver {

	@SuppressWarnings("unchecked")
	@Override
	public void onReceive(final Context context, final Intent intent) {
		// shdb.addSleep(
		// sdf.format(dateStarted) + " to " + sdf2.format(now),
		// currentSeriesX, currentSeriesY, minSensitivity,
		// maxSensitivity, alarmTriggerSensitivity);
		final SleepHistoryDatabase shdb = new SleepHistoryDatabase(context);

		try {
			final int min = intent.getIntExtra("min",
					SettingsActivity.DEFAULT_MIN_SENSITIVITY);
			int max = intent.getIntExtra("max",
					SettingsActivity.DEFAULT_MAX_SENSITIVITY);
			int alarm = intent
			.getIntExtra("alarm",
					SettingsActivity.DEFAULT_ALARM_SENSITIVITY);
			
			final String name = intent.getStringExtra("name");
			shdb.addSleep(context, name, (List<Double>) intent
					.getSerializableExtra("currentSeriesX"),
					(List<Double>) intent
							.getSerializableExtra("currentSeriesY"), min, max, alarm);

			long rowId = -1;

			Cursor c = shdb
					.getSleepMatches(name, new String[] { BaseColumns._ID,
							SleepHistoryDatabase.KEY_SLEEP_DATE_TIME });
			c.moveToFirst();
			rowId = c.getLong(0);

			final Intent reviewSleepIntent = new Intent(context,
					ReviewSleepActivity.class);
			final Uri uri = Uri.withAppendedPath(
					SleepContentProvider.CONTENT_URI, String.valueOf(rowId));
			reviewSleepIntent.setData(uri);
			// reviewSleepIntent.putExtra("position", position);

			reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(reviewSleepIntent);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			shdb.close();
		}
	}
}
