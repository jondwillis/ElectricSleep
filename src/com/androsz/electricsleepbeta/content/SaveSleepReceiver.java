package com.androsz.electricsleepbeta.content;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.SleepMonitoringService;
import com.androsz.electricsleepbeta.db.SleepHistoryDatabase;
import com.androsz.electricsleepbeta.db.SleepRecord;
import com.androsz.electricsleepbeta.util.PointD;

public class SaveSleepReceiver extends BroadcastReceiver {

	public static final String EXTRA_IO_EXCEPTION = "IOException";
	public static final String EXTRA_ROW_ID = "rowId";
	public static final String EXTRA_SUCCESS = "success";
	public static final String EXTRA_NOTE = "note";
	public static final String EXTRA_RATING = "rating";
	public static String SAVE_SLEEP_COMPLETED = "com.androsz.electricsleep.SAVE_SLEEP_COMPLETED";

	@Override
	public void onReceive(final Context context, final Intent intent) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				final double alarm = intent.getDoubleExtra(
						StartSleepReceiver.EXTRA_ALARM,
						SettingsActivity.DEFAULT_ALARM_SENSITIVITY);

				final String name = intent
						.getStringExtra(SleepMonitoringService.EXTRA_NAME);
				final int rating = intent.getIntExtra(EXTRA_RATING, 5);
				final String note = intent.getStringExtra(EXTRA_NOTE);

				// FileInputStream fis;
				RandomAccessFile raFile;
				List<PointD> originalData = null;
				try {
					// fis = context
					// .openFileInput(SleepMonitoringService.SLEEP_DATA);
					synchronized (SleepMonitoringService.sDataLock) {
						final File dataFile = context
								.getFileStreamPath(SleepMonitoringService.SLEEP_DATA);
						raFile = new RandomAccessFile(dataFile, "r");
						final long length = dataFile.length();
						final int chunkSize = 16;
						originalData = new ArrayList<PointD>((int) (length
								/ chunkSize / 2));
						if (length >= chunkSize) {
							// just do one giant IO operation to load whole file
							// into memory
							final byte[] buffer = new byte[(int) length];
							raFile.read(buffer);
							raFile.close();
							final byte[] chunk = new byte[chunkSize];
							for (int i = 0; i < buffer.length; i += chunkSize) {
								System.arraycopy(buffer, i, chunk, 0, chunkSize);
								originalData.add(PointD.fromByteArray(chunk));
							}
						}
					}
				} catch (final FileNotFoundException e) {
					context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED)
							.putExtra(EXTRA_IO_EXCEPTION, e.getMessage()));
					return;
				} catch (final IOException e) {
					context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED)
							.putExtra(EXTRA_IO_EXCEPTION, e.getMessage()));
					return;
				}

				context.deleteFile(SleepMonitoringService.SLEEP_DATA);

				final int numberOfPointsOriginal = originalData.size();

				// List<Double> mX = (List<Double>) intent
				// .getSerializableExtra("currentSeriesX");
				// List<Double> mY = (List<Double>) intent
				// .getSerializableExtra("currentSeriesY");

				if (numberOfPointsOriginal == 0) {
					context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED));
					return;
				}

				final SleepHistoryDatabase shdb = new SleepHistoryDatabase(
						context);

				final int numberOfDesiredGroupedPoints = SleepMonitoringService.MAX_POINTS_IN_A_GRAPH;
				// numberOfDesiredGroupedPoints = numberOfPointsOriginal >
				// numberOfDesiredGroupedPoints ? numberOfDesiredGroupedPoints
				// : numberOfPointsOriginal;

				if (numberOfDesiredGroupedPoints <= numberOfPointsOriginal) {
					final int pointsPerGroup = numberOfPointsOriginal
							/ numberOfDesiredGroupedPoints + 1;
					final List<PointD> lessDetailedData = new ArrayList<PointD>(
							numberOfDesiredGroupedPoints);
					int numberOfPointsInThisGroup = pointsPerGroup;
					double maxYForThisGroup;
					double totalForThisGroup;
					int numberOfSpikes = 0;
					int numberOfConsecutiveNonSpikes = 0;
					long timeOfFirstSleep = 0;
					for (int i = 0; i < numberOfDesiredGroupedPoints; i++) {
						maxYForThisGroup = 0;
						totalForThisGroup = 0;
						final int startIndexForThisGroup = i * pointsPerGroup;
						for (int j = 0; j < pointsPerGroup; j++) {
							try {
								final double currentY = originalData
										.get(startIndexForThisGroup + j).y;
								if (currentY > maxYForThisGroup) {
									maxYForThisGroup = currentY;
								}
								totalForThisGroup += currentY;
							} catch (final IndexOutOfBoundsException ioobe) {
								// lower the number of points
								// (and thereby signify that we are done)
								numberOfPointsInThisGroup = j - 1;
								break;
							}
						}
						final double averageForThisGroup = totalForThisGroup
								/ numberOfPointsInThisGroup;
						if (numberOfPointsInThisGroup < pointsPerGroup) {
							// we are done
							final int lastIndex = numberOfPointsOriginal - 1;
							lessDetailedData.add(originalData.get(lastIndex));
							break;
						} else {
							if (maxYForThisGroup < alarm) {
								maxYForThisGroup = averageForThisGroup;
								if (timeOfFirstSleep == 0
										&& ++numberOfConsecutiveNonSpikes > 4) {
									final int lastIndex = lessDetailedData
											.size() - 1;

									timeOfFirstSleep = Math
											.round(lessDetailedData
													.get(lastIndex).x);
								}
							} else {
								numberOfConsecutiveNonSpikes = 0;
								numberOfSpikes++;
							}
							lessDetailedData.add(new PointD(originalData
									.get(startIndexForThisGroup).x,
									maxYForThisGroup));
						}
					}

					final long endTime = Math.round(lessDetailedData
							.get(lessDetailedData.size() - 1).x);
					final long startTime = Math.round(lessDetailedData.get(0).x);

					try {
						shdb.addSleep(context, new SleepRecord(name,
								lessDetailedData,
								SettingsActivity.DEFAULT_MIN_SENSITIVITY,
								alarm, rating, endTime - startTime,
								numberOfSpikes, timeOfFirstSleep, note));
					} catch (final IOException e) {
						shdb.close();
						context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED)
								.putExtra(EXTRA_IO_EXCEPTION, e.getMessage()));
						return;
					}
				} else {

					final long endTime = Math.round(originalData
							.get(numberOfPointsOriginal - 1).x);
					final long startTime = Math.round(originalData.get(0).x);

					int numberOfSpikes = 0;
					int numberOfConsecutiveNonSpikes = 0;
					long timeOfFirstSleep = endTime;
					for (int i = 0; i < numberOfPointsOriginal; i++) {
						final double currentY = originalData.get(i).y;
						if (currentY < alarm) {
							if (timeOfFirstSleep == endTime
									&& ++numberOfConsecutiveNonSpikes > 4) {
								final int lastIndex = originalData.size() - 1;

								timeOfFirstSleep = Math.round(originalData
										.get(lastIndex).x);
							}
						} else {
							numberOfConsecutiveNonSpikes = 0;
							numberOfSpikes++;
						}
					}
					try {
						shdb.addSleep(context, new SleepRecord(name,
								originalData,
								SettingsActivity.DEFAULT_MIN_SENSITIVITY,
								alarm, rating, endTime - startTime,
								numberOfSpikes, timeOfFirstSleep, note));
					} catch (final IOException e) {
						shdb.close();
						context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED)
								.putExtra(EXTRA_IO_EXCEPTION, e.getMessage()));
						return;
					}
				}

				final Cursor c = shdb.getSleepMatches(name, new String[] {
						BaseColumns._ID, SleepRecord.KEY_TITLE });

				if (c == null) {
					/*
					 * Toast.makeText( context,
					 * "Could not find the recently saved sleep in the sleep database- report this!"
					 * , Toast.LENGTH_LONG).show();
					 */
					shdb.close();
					context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED));
					return;
				} else if (!c.moveToFirst()) {
					/*
					 * Toast.makeText( context,
					 * "Could not move to the recently saved sleep in the sleep database- report this!"
					 * , Toast.LENGTH_LONG).show();
					 */
					shdb.close();
					c.close();
					context.sendBroadcast(new Intent(SAVE_SLEEP_COMPLETED));
					return;
				}
				final long rowId = c.getLong(0);
				c.close();

				// context.startActivity(reviewSleepIntent);
				shdb.close();

				final Intent saveSleepCompletedIntent = new Intent(
						SAVE_SLEEP_COMPLETED);
				saveSleepCompletedIntent.putExtra(EXTRA_SUCCESS, true);
				saveSleepCompletedIntent.putExtra(EXTRA_ROW_ID,
						String.valueOf(rowId));
				context.sendBroadcast(saveSleepCompletedIntent);
			}
		}).start();
	}
}
