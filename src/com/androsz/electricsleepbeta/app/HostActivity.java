package com.androsz.electricsleepbeta.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;

import com.androsz.electricsleepbeta.R;

public abstract class HostActivity extends AnalyticActivity {

	protected abstract int getContentAreaLayoutId();

	boolean alreadyPrepared = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getContentAreaLayoutId());

		ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.gradient_background_vert));
		bar.setDisplayHomeAsUpEnabled(true);
		bar.setDisplayShowHomeEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_item_tutorial:
			startActivity(new Intent(this, WelcomeTutorialWizardActivity.class));
			break;
		case R.id.menu_item_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.menu_item_donate:
			trackPageView("donate");
			final Uri marketUri = Uri
					.parse("market://details?id=com.androsz.electricsleepdonate");
			final Intent marketIntent = new Intent(Intent.ACTION_VIEW,
					marketUri);
			startActivity(marketIntent);
			break;
		case R.id.menu_item_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.menu_item_report:
			startActivity(new Intent(
					"android.intent.action.VIEW",
					Uri.parse("http://code.google.com/p/electricsleep/issues/entry")));
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return alreadyPrepared;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.titlebar_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { if
	 * (!alreadyPrepared) { final android.view.MenuInflater inflater =
	 * getMenuInflater(); inflater.inflate(R.menu.titlebar_menu, menu);
	 * alreadyPrepared = true; } return super.onPrepareOptionsMenu(menu); }
	 */
}
