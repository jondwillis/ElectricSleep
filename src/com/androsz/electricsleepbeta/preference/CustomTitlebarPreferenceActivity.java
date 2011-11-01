package com.androsz.electricsleepbeta.preference;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.HomeActivity;

public abstract class CustomTitlebarPreferenceActivity extends PreferenceActivity {

	protected abstract int getContentAreaLayoutId();

	protected abstract String getPreferencesName();

	public void hideTitleButton1() {
		/*
		 * final ImageButton btn1 = (ImageButton)
		 * findViewById(R.id.title_button_1);
		 * btn1.setVisibility(View.INVISIBLE);
		 * findViewById(R.id.title_sep_1).setVisibility(View.INVISIBLE);
		 */
	}

	public void hideTitleButton2() {
		/*
		 * final ImageButton btn2 = (ImageButton)
		 * findViewById(R.id.title_button_2);
		 * btn2.setVisibility(View.INVISIBLE);
		 * findViewById(R.id.title_sep_2).setVisibility(View.INVISIBLE);
		 */
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_Sherlock);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			this.getActionBar().setDisplayHomeAsUpEnabled(true);
		}

		// final ListView lvw = getListView();
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

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		// final MenuInflater inflater = getMenuInflater();
		// inflater.inflate(R.menu.titlebar_menu, menu);
		return false;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
