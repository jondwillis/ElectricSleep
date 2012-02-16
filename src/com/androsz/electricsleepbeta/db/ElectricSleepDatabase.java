/* @(#)ElectricSleepDatabase.java
 *
 *========================================================================
 * Copyright 2011 by Zeo Inc. All Rights Reserved
 *========================================================================
 *
 * Date: $Date$
 * Author: Jon Willis
 * Author: Brandon Edens <brandon.edens@myzeo.com>
 * Version: $Revision$
 */

package com.androsz.electricsleepbeta.db;

import com.androsz.electricsleepbeta.util.PointD;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.TimeZone;

/**
 * Interface to the electric sleep database.
 *
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class ElectricSleepDatabase extends SQLiteOpenHelper {

    private static final String TAG = ElectricSleepDatabase.class.getSimpleName();

    private static final String DB_NAME = "sleephistory";
    private static final int DB_VERSION = 7;

    public ElectricSleepDatabase(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createSleepSessionTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 5) {
            upgradeToVersion6(db);
            ++oldVersion;
        }

        if (oldVersion == 6) {
            upgradeToVersion7(db);
            ++oldVersion;
        }
    }

    private void createSleepSessionTable(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + SleepSession.PATH + " (" +
            SleepSession._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            SleepSession.CREATED_ON + " INTEGER NOT NULL," +
            SleepSession.UPDATED_ON + " INTEGER NOT NULL," +
            SleepSession.TIMEZONE + " TEXT NOT NULL," +
            SleepSession.START_TIMESTAMP + " INTEGER NOT NULL," +
            SleepSession.START_JULIAN_DAY + " INTEGER NOT NULL," +
            SleepSession.END_TIMESTAMP + " INTEGER NOT NULL," +
            SleepSession.DATA + " BLOB," +
            SleepSession.RATING + " INTEGER," +
            SleepSession.SPIKES + " INTEGER," +
            SleepSession.FELL_ASLEEP_TIMESTAMP + " INTEGER," +
            SleepSession.DURATION + " INTEGER," +
            SleepSession.CALIBRATION_LEVEL + " REAL," +
            SleepSession.MIN + " REAL," +
            SleepSession.NOTE + " TEXT" +
            ")");
    }

    private void upgradeToVersion6(SQLiteDatabase db) {
        // create the sleep session table
        createSleepSessionTable(db);

        final String DURATION = "KEY_SLEEP_DATA_DURATION";
        final String ALARM = "sleep_data_alarm";
        final String MIN = "sleep_data_min";
        final String NOTE = "KEY_SLEEP_DATA_NOTE";
        final String RATING = "sleep_data_rating";
        final String SPIKES = "KEY_SLEEP_DATA_SPIKES";
        final String DATA = "sleep_data";

        // copy over existing data
        Cursor cursor = db.query(
            "FTSsleephistory",
            new String[] {
                DATA, SPIKES, RATING, NOTE, MIN, DURATION, ALARM
            },
            null, null,
            null, null, null);
        if (cursor.moveToFirst()) {
            do {
                ContentValues values = new ContentValues(8);

                // populate the start and end timestamps
                byte[] bytes = cursor.getBlob(0);
                try {
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                    List<PointD> data = (List<PointD>) ois.readObject();
                    long startTimestamp = Math.round(data.get(0).x);
                    long endTimestamp = Math.round(data.get(data.size() - 1).x);
                    values.put(SleepSession.START_TIMESTAMP, startTimestamp);
                    values.put(SleepSession.END_TIMESTAMP, endTimestamp);
                    long duration = endTimestamp - startTimestamp;
                    Log.d(TAG, "Calculated timestamp is: " + duration);
                } catch (ClassNotFoundException e) {
                    Log.e(TAG, "List class does not exist. Something is terribly wrong.");
                } catch (IOException e) {
                    Log.w(TAG, "Failure to parse data to load start and end timestamp");
                }

                // copy over all the other values
                values.put(SleepSession.DATA, bytes);
                values.put(SleepSession.SPIKES, cursor.getInt(1));
                values.put(SleepSession.RATING, cursor.getInt(2));
                values.put(SleepSession.NOTE, cursor.getString(3));
                values.put(SleepSession.MIN, cursor.getDouble(4));
                values.put(SleepSession.DURATION, cursor.getLong(5));
                Log.d(TAG, "Original duration: " + cursor.getLong(5));
                values.put(SleepSession.CALIBRATION_LEVEL, cursor.getDouble(6));

                final long now = System.currentTimeMillis();
                values.put(SleepSession.CREATED_ON, now);
                values.put(SleepSession.UPDATED_ON, now);
                values.put(SleepSession.TIMEZONE, TimeZone.getDefault().getID());

                db.insert(SleepSession.PATH, null, values);
            } while (cursor.moveToNext());
        }
    }

    /**
     * Upgrade to version 7 includes adding start julian day.
     */
    private void upgradeToVersion7(SQLiteDatabase db) {
        db.execSQL("ALTER TABLE " + SleepSession.PATH + " ADD COLUMN " +
                   SleepSession.START_JULIAN_DAY + " INTEGER;");

        final Cursor cursor = db.query(
            SleepSession.PATH,
            new String[] {
                SleepSession._ID,
                SleepSession.START_TIMESTAMP,
            }, null, null, null, null, null);

        // Update all existing sleep sessions with a start julian day.
        cursor.moveToFirst();
        do {
            final ContentValues values = new ContentValues(1);
            final long id = cursor.getLong(0);
            values.put(SleepSession.START_JULIAN_DAY,
                       SleepSession.getZeoJulianDay(cursor.getLong(1)));
            db.update(SleepSession.PATH, values,
                      SleepSession._ID + " =? ", new String[] {Long.toString(id)});
        } while (cursor.moveToNext());
    }
}

