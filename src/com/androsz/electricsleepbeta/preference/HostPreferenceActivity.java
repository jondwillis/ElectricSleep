package com.androsz.electricsleepbeta.preference;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.app.SherlockPreferenceActivity;
import android.support.v4.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.HomeActivity;

public abstract class HostPreferenceActivity extends SherlockPreferenceActivity {

	protected abstract int getContentAreaLayoutId();

	protected abstract String getPreferencesName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ListView lvw = getListView();
		lvw.setBackgroundColor(Color.BLACK);

		if (getPreferencesName() != null) {
			getPreferenceManager().setSharedPreferencesName(getPreferencesName());
		}

		addPreferencesFromResource(getContentAreaLayoutId());

		final Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.actionbar_bg);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		final ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(bitmapDrawable);

		bar.setDisplayHomeAsUpEnabled(true);
	}

	public void onClick(final View v) {
		final Intent intent = new Intent(v.getContext(), HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return (true);
		}
		return super.onOptionsItemSelected(item);
	}
}
