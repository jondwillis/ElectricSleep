package com.androsz.electricsleep.receiver;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.content.BroadcastReceiver;

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
			final long result = shdb
					.addSleep(context, intent.getStringExtra("name"),
							(ArrayList<Double>) intent
									.getSerializableExtra("currentSeriesX"),
							(ArrayList<Double>) intent
									.getSerializableExtra("currentSeriesY"),
							min, intent.getIntExtra("max",
									SettingsActivity.DEFAULT_MAX_SENSITIVITY),
							intent.getIntExtra("alarm",
									SettingsActivity.DEFAULT_ALARM_SENSITIVITY));

			final Intent reviewSleepIntent = new Intent(context,
					ReviewSleepActivity.class);

			final Uri data = Uri.withAppendedPath(
					SleepContentProvider.CONTENT_URI, String.valueOf(result));
			reviewSleepIntent.setData(data);

			reviewSleepIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

			context.startActivity(reviewSleepIntent);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
