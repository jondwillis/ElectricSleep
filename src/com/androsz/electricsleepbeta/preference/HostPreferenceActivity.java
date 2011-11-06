package com.androsz.electricsleepbeta.preference;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.SherlockPreferenceActivity;
import android.support.v4.view.MenuItem;
import android.widget.ListView;

import com.androsz.electricsleepbeta.app.HostActivity;

public abstract class HostPreferenceActivity extends SherlockPreferenceActivity {

	protected static final int NO_CONTENT = 0;

	protected abstract int getContentAreaLayoutId();

	protected abstract String getPreferencesName();

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final ListView lvw = getListView();
		lvw.setBackgroundColor(Color.BLACK);

		//if (getPreferencesName() != null) {
		//	getPreferenceManager().setSharedPreferencesName(getPreferencesName());
		//}

		if (getContentAreaLayoutId() != NO_CONTENT) {
			addPreferencesFromResource(getContentAreaLayoutId());
		}

		HostActivity.prepareActionBar(this);
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
