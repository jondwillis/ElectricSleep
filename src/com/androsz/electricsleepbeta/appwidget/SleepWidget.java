package com.androsz.electricsleepbeta.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.content.StartSleepReceiver;

public class SleepWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			final int[] appWidgetIds) {
		final RemoteViews updateViews = new RemoteViews(context.getPackageName(),
				R.layout.appwidget_sleep);

		final PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(
				StartSleepReceiver.START_SLEEP), 0);

		updateViews.setOnClickPendingIntent(R.id.appwidget_btn_sleep, pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}