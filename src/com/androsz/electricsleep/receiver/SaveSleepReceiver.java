package com.androsz.electricsleep.receiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.androsz.electricsleep.app.ReviewSleepActivity;
import com.androsz.electricsleep.app.SettingsActivity;
import com.androsz.electricsleep.db.SleepContentProvider;
import com.androsz.electricsleep.db.SleepHistoryDatabase;

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
			final int max = intent.getIntExtra("max",
					SettingsActivity.DEFAULT_MAX_SENSITIVITY);
			final int alarm = intent.getIntExtra("alarm",
					SettingsActivity.DEFAULT_ALARM_SENSITIVITY);

			final String name = intent.getStringExtra("name");

			List<Double> mX = (List<Double>) intent
					.getSerializableExtra("currentSeriesX");
			List<Double> mY = (List<Double>) intent
					.getSerializableExtra("currentSeriesY");
			final int count = mY.size();

			int numberOfDesiredGroupedPoints = 200;
			numberOfDesiredGroupedPoints = count > numberOfDesiredGroupedPoints ? count
					/ ((int) count / numberOfDesiredGroupedPoints)
					: count;
			//
			if (numberOfDesiredGroupedPoints != count) {
				final int pointsPerGroup = count / numberOfDesiredGroupedPoints
						+ 1;
				final List<Double> lessDetailedX = new ArrayList<Double>(
						numberOfDesiredGroupedPoints);
				final List<Double> lessDetailedY = new ArrayList<Double>(
						numberOfDesiredGroupedPoints);
				int numberOfPointsInThisGroup = pointsPerGroup;
				double averageYForThisGroup = 0;
				for (int i = 0; i < numberOfDesiredGroupedPoints; i++) {
					averageYForThisGroup = 0;
					final int startIndexForThisGroup = i * pointsPerGroup;
					for (int j = 0; j < pointsPerGroup; j++) {
						try {
							averageYForThisGroup += mY
									.get(startIndexForThisGroup + j);
						} catch (final IndexOutOfBoundsException ioobe) {
							// lower the number of points
							// (and signify that we are done)
							numberOfPointsInThisGroup = j - 1;
							break;
						}
					}
					averageYForThisGroup /= numberOfPointsInThisGroup;
					if (numberOfPointsInThisGroup < pointsPerGroup) {
						// we are done
						final int lastIndex = mX.size() - 1;
						lessDetailedX.add(mX.get(lastIndex));
						lessDetailedY.add(mY.get(lastIndex));
						mX = lessDetailedX;
						mY = lessDetailedY;
						break;
					} else {
						lessDetailedX.add(mX.get(startIndexForThisGroup));
						lessDetailedY.add(averageYForThisGroup);
					}
				}
				shdb.addSleep(context, name, lessDetailedX, lessDetailedY, min,
						max, alarm);
			} else {
				shdb.addSleep(context, name, mX, mY, min, max, alarm);
			}

			long rowId = -1;

			final Cursor c = shdb
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
