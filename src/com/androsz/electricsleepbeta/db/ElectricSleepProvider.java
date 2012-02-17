/* @(#)ElectricSleepProvider.java
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

import com.google.android.apps.iosched.util.SelectionBuilder;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.util.TimeZone;

/**
 * Provider for electric sleep data.
 * 
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class ElectricSleepProvider extends ContentProvider {

    public static final String CONTENT_AUTHORITY = "com.androsz.electricsleepbeta.db.electric_sleep_provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    public interface TimestampColumns {
        String CREATED_ON = "created_on";
        String UPDATED_ON = "updated_on";
    }

    private static final int SLEEP_SESSIONS = 100;
    private static final int SLEEP_SESSIONS_ID = 101;

    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private ElectricSleepDatabase mOpenHelper;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri, "");
        final int count = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return count;
    }

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }

    @Override
    public String getType(Uri uri) {
        final int match = URI_MATCHER.match(uri);
        switch (match) {
        case SLEEP_SESSIONS:
            return SleepSession.CONTENT_TYPE;
        case SLEEP_SESSIONS_ID:
            return SleepSession.CONTENT_ITEM_TYPE;
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (values == null) {
            return null;
        }

        final long currentTimestamp = System.currentTimeMillis();
        values.put(TimestampColumns.CREATED_ON, currentTimestamp);
        values.put(TimestampColumns.UPDATED_ON, currentTimestamp);

        final ContentResolver resolver = getContext().getContentResolver();
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        switch (match) {
        case SLEEP_SESSIONS:
            values.put(SleepSession.TIMEZONE, TimeZone.getDefault().getID());
            final long id = db.insert(SleepSession.PATH, null, values);
            if (id == -1) {
                return null;
            }
            resolver.notifyChange(uri, null);
            return ContentUris.withAppendedId(SleepSession.CONTENT_URI, id);
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new ElectricSleepDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = URI_MATCHER.match(uri);

        final SelectionBuilder builder = buildSimpleSelection(uri,
                " as query_tmp");

        switch (match) {
        case SLEEP_SESSIONS:
        case SLEEP_SESSIONS_ID:
            if (projection == null) {
                projection = SleepSession.PROJECTION;
            }
            if (sortOrder == null) {
                sortOrder = SleepSession.SORT_ORDER;
            }
            Cursor c = builder.where(selection, selectionArgs).query(db,
                    projection, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        }
        return null;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        final ContentResolver resolver = getContext().getContentResolver();

        final long updateTimestamp = System.currentTimeMillis();
        values.put(TimestampColumns.UPDATED_ON, updateTimestamp);

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri, "");
        final int count = builder.where(selection, selectionArgs).update(db,
                values);

        final int match = URI_MATCHER.match(uri);
        switch (match) {
        case SLEEP_SESSIONS:
        case SLEEP_SESSIONS_ID:
            resolver.notifyChange(uri, null);
        }
        return count;
    }

    private SelectionBuilder buildSimpleSelection(Uri uri, String as) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = URI_MATCHER.match(uri);

        String table = null;
        switch (match) {
        case SLEEP_SESSIONS:
            return builder.table(SleepSession.PATH);
        case SLEEP_SESSIONS_ID:
            return builder.table(SleepSession.PATH).where(
                    SleepSession._ID + "=?",
                    Long.toString(ContentUris.parseId(uri)));
        }
        throw new UnsupportedOperationException("Unkown uri: " + uri);
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = CONTENT_AUTHORITY;

        matcher.addURI(authority, SleepSession.PATH, SLEEP_SESSIONS);
        matcher.addURI(authority, SleepSession.PATH + "/#", SLEEP_SESSIONS_ID);
        return matcher;
    }
}
