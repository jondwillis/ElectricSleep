/* @(#)ReviewSleepFragment.java
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

package com.androsz.electricsleepbeta.app;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.SleepChart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Fragment used to review a sleep record.
 *
 * @author Jon Willis
 * @author Brandon Edens
 * @version $Revision$
 */
public class ReviewSleepFragment extends Fragment {

    private static final String TAG = ReviewSleepFragment.class.getSimpleName();

    /** Row ID of the sleep record to review. */
    public static final String KEY_SLEEP_ID = "key_sleep_id";

    private SleepSession mSleepRecord;

    private SleepChart mSleepChart;
    private RatingBar mMorningFeel;
    private TextView mSleepEfficiency;
    private TextView mTotalRecordingTime;
    private TextView mTimesDisrupted;
    private TextView mTimeToFallAsleep;
    private TextView mNotes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_review_sleep, container, false);
        mSleepChart = (SleepChart) root.findViewById(R.id.sleep_movement_chart);

        mMorningFeel = (RatingBar) root.findViewById(R.id.morning_feel);
        mSleepEfficiency = (TextView) root.findViewById(R.id.sleep_efficiency);
        mTotalRecordingTime = (TextView) root.findViewById(R.id.total_recording_time);
        mTimesDisrupted = (TextView) root.findViewById(R.id.times_disrupted);
        mTimeToFallAsleep = (TextView) root.findViewById(R.id.time_to_fall_asleep);
        mNotes = (TextView) root.findViewById(R.id.notes);

        if (mSleepRecord != null) {
            setSleepRecord(mSleepRecord);
        }

        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_review_sleep, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
        case R.id.menu_item_share_sleep_record:
            if (mSleepRecord == null) {
                // Emit error if user attempts to share when no record is loaded.
                Toast.makeText(getActivity(),
                               R.string.unfortunately_no_sleep_record_was_available_for_sharing,
                               Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent intent = new Intent(Intent.ACTION_SEND);
            final String dateString =
                DateUtils.formatDateTime(getActivity(),
                                         mSleepRecord.getStartTimestamp(),
                                         DateUtils.FORMAT_NO_YEAR |
                                         DateUtils.FORMAT_SHOW_DATE |
                                         DateUtils.FORMAT_ABBREV_ALL);
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.see_how_i_slept_on) +
                            " " + dateString);
            StringBuilder builder = new StringBuilder();
            builder.append(getString(R.string.see_how_i_slept_on) + " " + dateString + ".\n");
            builder.append(getString(R.string.try_out_zeo));
            intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
            intent.setType("text/plain");
            try {
                final String filename = "zeo_actigraphy_detail.png";
                File screenshotFile;
                String storageState = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(storageState) &&
                    !Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState)) {
                    // We only attempt to use external storage if its mounted and NOT read only as
                    // we must write our screenshots there.
                    File screenshotDirectory =
                        new File(Environment.getExternalStorageDirectory(),
                                 "/Android/data/com.androsz.electricsleepbeta/tmp/");
                    screenshotDirectory.mkdirs();
                    // Attempt to store the night detail screenshot on the SD card if possible.
                    screenshotFile = new File(screenshotDirectory, filename);
                    FileOutputStream os = new FileOutputStream(screenshotFile);
                    // Inflate the layout used for sharing night details with others.
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final View view = inflater.inflate(R.layout.share_sleep, null);
                    ((TextView) view.findViewById(R.id.share_date))
                        .setText(mSleepRecord.getDayText(getActivity()));
                    ((SleepChart) view.findViewById(R.id.sleep_movement_chart))
                        .sync(mSleepRecord);
                    ((RatingBar) view.findViewById(R.id.morning_feel))
                        .setRating(mSleepRecord.getRating());
                    ((TextView) view.findViewById(R.id.sleep_efficiency))
                        .setText(mSleepRecord.getEfficiency());
                    ((TextView) view.findViewById(R.id.total_recording_time))
                        .setText(mSleepRecord.getTotalRecordTime(getActivity().getResources()));
                    ((TextView) view.findViewById(R.id.times_disrupted))
                        .setText(mSleepRecord.getTimesDisrupted());
                    ((TextView) view.findViewById(R.id.time_to_fall_asleep))
                        .setText(mSleepRecord.getTimeToFallAsleepText(
                                     getActivity().getResources()));

                    view.setLayoutParams(new LinearLayout.LayoutParams(800, 800));

                    // Begin process of drawing night sharing to bitmap.
                    view.measure(
                        View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY));
                    view.layout(0, 0, 800, 800);
                    //view.setBackgroundColor(getResources().getColor(R.color.share_background));
                    Bitmap bitmap =
                        Bitmap.createBitmap(view.getWidth(), view.getHeight(),
                                            Bitmap.Config.ARGB_8888);
                    view.draw(new Canvas(bitmap));
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.close();
                    intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(screenshotFile));
                    intent.setType("image/png");
                } else {
                    // Warn user that sharing with other apps is not possible without external
                    // storage.
                    Toast.makeText(getActivity(),
                                   R.string.i_am_sorry_but_cannot_share_zeo_sleep_without_sdcard,
                                   Toast.LENGTH_LONG).show();
                    return true;
                }
            } catch (IOException e) {
                Toast.makeText(
                    getActivity(),
                    R.string.oops_there_was_error_while_generating_image_for_sharing,
                    Toast.LENGTH_LONG).show();
            }
            startActivity(Intent.createChooser(intent, "Share Night of Sleep"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setSleepRecord(SleepSession sleepRecord) {
        if (sleepRecord == null) {
            return;
        }

        Log.d(TAG, "Setting sleep record.");
        mSleepChart.sync(sleepRecord);
        mMorningFeel.setRating(sleepRecord.getRating());
        mSleepEfficiency.setText(sleepRecord.getEfficiency());
        mTotalRecordingTime.setText(sleepRecord.getTotalRecordTime(getResources()));
        mTimesDisrupted.setText(sleepRecord.getTimesDisrupted());
        mTimeToFallAsleep.setText(sleepRecord.getTimeToFallAsleepText(getResources()));
        mNotes.setText(sleepRecord.getNotes());

		mSleepRecord = sleepRecord;
    }

}

