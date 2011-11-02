package com.androsz.electricsleepbeta.preference;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.HomeActivity;

public abstract class CustomTitlebarPreferenceActivity extends PreferenceActivity {

	protected abstract int getContentAreaLayoutId();

	protected abstract String getPreferencesName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		final ListView lvw = getListView();
		lvw.setBackgroundColor(Color.BLACK);
		// lvw.setCacheColorHint(0);
		// lvw.setBackgroundDrawable(getResources().getDrawable(R.drawable.gradient_background_vert));
		if (getPreferencesName() != null) {
			getPreferenceManager().setSharedPreferencesName(getPreferencesName());
		}
		addPreferencesFromResource(getContentAreaLayoutId());
		// getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
		// R.layout.titlebar);
		// ((TextView) findViewById(R.id.title_text)).setText(getTitle());
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
		return false;
	}
}
