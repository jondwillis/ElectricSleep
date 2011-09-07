package com.androsz.electricsleepbeta.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.R;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public abstract class HostActivity extends AnalyticActivity {

	protected abstract int getContentAreaLayoutId();

	boolean alreadyPrepared = false;

	//private AdView adView;

	/*@Override
	protected void onDestroy() {
		// Destroy the AdView.
		adView.destroy();
		super.onDestroy();
	}*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(getContentAreaLayoutId());
		//ViewGroup v = (ViewGroup) getLayoutInflater().inflate(getContentAreaLayoutId(), null, false);
		//setContentView(v);
		// Create an ad.
		//adView = new AdView(this, AdSize.IAB_BANNER, "a14e64057e1e23e");

		// Create an ad request.
		//AdRequest adRequest = new AdRequest();
		// Fill out ad request.

		// Add the AdView to the view hierarchy. The view will have no size
		// until the ad is loaded.

		// Start loading the ad in the background.
		//adView.loadAd(adRequest);
		//v.addView(adView);
		new AsyncTask<Void, Void, BitmapDrawable>() {

			@Override
			protected void onPostExecute(BitmapDrawable result) {

				ActionBar bar = getSupportActionBar();
				bar.setBackgroundDrawable(result);

				bar.setDisplayShowHomeEnabled(true);
			}

			@Override
			protected BitmapDrawable doInBackground(Void... params) {
				Bitmap bmp = BitmapFactory.decodeResource(getResources(),
						R.drawable.actionbar_bg);
				BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
				bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT,
						Shader.TileMode.REPEAT);
				return bitmapDrawable;
			}
		}.execute();
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

	static ColorMatrixColorFilter COLOR_FILTER;
	static {
		// COLOR_FILTER = new PorterDuffColorFilter(Color.WHITE,
		// PorterDuff.Mode.MULTIPLY);

		ColorMatrix cm = new ColorMatrix();
		cm.setScale(2, 2, 2, 1); // make it doubly closer to whiiiiiiite
		COLOR_FILTER = new ColorMatrixColorFilter(cm);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.titlebar_menu, menu);

		for (int i = 0; i < menu.size(); i++) {
			MenuItem mi = menu.getItem(i);
			mi.getIcon().setColorFilter(COLOR_FILTER);
		}
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * @Override public boolean onPrepareOptionsMenu(Menu menu) { if
	 * (!alreadyPrepared) { final android.view.MenuInflater inflater =
	 * getMenuInflater(); inflater.inflate(R.menu.titlebar_menu, menu);
	 * alreadyPrepared = true; } return super.onPrepareOptionsMenu(menu); }
	 */
}
