package com.androsz.electricsleepbeta.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.db.SleepSession;
import com.androsz.electricsleepbeta.widget.SleepChart;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

public class IntentUtil {
	/**
	 * Indicates whether the specified action can be used as an intent. This
	 * method queries the package manager for installed packages that can
	 * respond to an intent with the specified action. If no suitable package is
	 * found, this method returns false.
	 *
	 * @param context
	 *            The application's environment.
	 * @param action
	 *            The Intent action to check for availability.
	 *
	 * @return True if an Intent with the specified action can be sent and
	 *         responded to, false otherwise.
	 */
	public static boolean doesIntentHaveReceivers(final Context context, final String action) {
		final PackageManager packageManager = context.getPackageManager();
		final Intent intent = new Intent(action);
		final List<ResolveInfo> list = packageManager.queryBroadcastReceivers(intent, 0);
		return list.size() > 0;
	}

	public static boolean isApplicationInstalled(final Context context, final String packageName) {
		final PackageManager packageManager = context.getPackageManager();
		try {
			packageManager.getApplicationInfo(packageName, 0);
			return true;
		} catch (final NameNotFoundException whocares) {
			return false;
		}
	}


    /**
     * @param session
     * @param activity
     */
    public static void shareSleep(SleepSession session, Activity activity) {
        if (session == null) {
            // Emit error if user attempts to share when no record is loaded.
            Toast.makeText(
                    activity,
                    R.string.unfortunately_no_sleep_record_was_available_for_sharing,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        final String dateString = DateUtils.formatDateTime(activity,
                session.getStartTimestamp(), DateUtils.FORMAT_NO_YEAR
                        | DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_ABBREV_ALL);
        intent.putExtra(Intent.EXTRA_SUBJECT,
                activity.getString(R.string.see_how_i_slept_on) + " " + dateString);
        StringBuilder builder = new StringBuilder();
        builder.append(activity.getString(R.string.see_how_i_slept_on) + " "
                + dateString + ".\n");
        builder.append(activity.getString(R.string.download_sleep_101_for_free));
        intent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        intent.setType("text/plain");
        try {
            final String filename = "zeo_actigraphy_detail.png";
            File screenshotFile;
            String storageState = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(storageState)
                    && !Environment.MEDIA_MOUNTED_READ_ONLY
                            .equals(storageState)) {
                // We only attempt to use external storage if its mounted and
                // NOT read only as
                // we must write our screenshots there.
                File screenshotDirectory = new File(
                        Environment.getExternalStorageDirectory(),
                        "/Android/data/com.androsz.electricsleepbeta/tmp/");
                screenshotDirectory.mkdirs();
                // Attempt to store the night detail screenshot on the SD card
                // if possible.
                screenshotFile = new File(screenshotDirectory, filename);
                FileOutputStream os = new FileOutputStream(screenshotFile);
                // Inflate the layout used for sharing night details with
                // others.
                LayoutInflater inflater = activity.getLayoutInflater();
                final View view = inflater.inflate(R.layout.share_sleep, null);
                ((TextView) view.findViewById(R.id.share_date)).setText(session
                        .getDayText(activity));
                ((SleepChart) view.findViewById(R.id.sleep_movement_chart))
                        .sync(session);
                ((RatingBar) view.findViewById(R.id.morning_feel))
                        .setRating(session.getRating());
                ((TextView) view.findViewById(R.id.sleep_efficiency))
                        .setText(session.getEfficiency());
                ((TextView) view.findViewById(R.id.total_recording_time))
                        .setText(session.getTotalRecordTime(activity.getResources()));
                ((TextView) view.findViewById(R.id.times_disrupted))
                        .setText(session.getTimesDisrupted());
                ((TextView) view.findViewById(R.id.time_to_fall_asleep))
                        .setText(session.getTimeToFallAsleepText(activity
                                .getResources()));

                view.setLayoutParams(new LinearLayout.LayoutParams(600, 900));

                // Begin process of drawing night sharing to bitmap.
                view.measure(View.MeasureSpec.makeMeasureSpec(600,
                        View.MeasureSpec.EXACTLY), View.MeasureSpec
                        .makeMeasureSpec(900, View.MeasureSpec.EXACTLY));
                view.layout(0, 0, 600, 900);
                view.setBackgroundColor(activity.getResources().getColor(
                        R.color.background_light));
                Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                        view.getHeight(), Bitmap.Config.ARGB_8888);
                view.draw(new Canvas(bitmap));
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.close();
                intent.putExtra(Intent.EXTRA_STREAM,
                        Uri.fromFile(screenshotFile));
                intent.setType("image/png");
            } else {
                // Warn user that sharing with other apps is not possible
                // without external
                // storage.
                Toast.makeText(
                        activity,
                        R.string.i_am_sorry_but_cannot_share_zeo_sleep_without_sdcard,
                        Toast.LENGTH_LONG).show();
                return;
            }
        } catch (IOException e) {
            Toast.makeText(
                    activity,
                    R.string.oops_there_was_error_while_generating_image_for_sharing,
                    Toast.LENGTH_LONG).show();
        }
        activity.startActivity(Intent.createChooser(intent, "Share Night of Sleep"));
    }
}
