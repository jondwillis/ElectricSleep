package com.androsz.electricsleepbeta.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.view.View;

import com.androsz.electricsleepbeta.R;

public abstract class HostActivity extends AnalyticActivity {

	static ColorMatrixColorFilter COLOR_FILTER;

	// private AdView adView;

	/*
	 * @Override protected void onDestroy() { // Destroy the AdView.
	 * adView.destroy(); super.onDestroy(); }
	 */

	static {
		// COLOR_FILTER = new PorterDuffColorFilter(Color.WHITE,
		// PorterDuff.Mode.MULTIPLY);

		final ColorMatrix cm = new ColorMatrix();
		cm.setScale(4, 4, 4, 1); // tint it closer to white (too much can cause
									// transparent pixels to look too opaque)
		COLOR_FILTER = new ColorMatrixColorFilter(cm);
	}

	protected abstract int getContentAreaLayoutId();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// setContentView();
		final View root = getLayoutInflater().inflate(getContentAreaLayoutId(), null, false);
		root.setBackgroundResource(R.drawable.gradient_background_vert);
		setContentView(root);

		final Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.actionbar_bg);
		final BitmapDrawable bitmapDrawable = new BitmapDrawable(bmp);
		bitmapDrawable.setTileModeXY(Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
		final ActionBar bar = getSupportActionBar();
		bar.setBackgroundDrawable(bitmapDrawable);

		bar.setDisplayShowHomeEnabled(true);
		bar.setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_host, menu);
		for (int i = 0; i < menu.size(); i++) {
			final MenuItem mi = menu.getItem(i);
			final Drawable icon = mi.getIcon();
			if (icon != null) {
				final Drawable mutated = icon.getCurrent().mutate();
				mutated.setColorFilter(COLOR_FILTER);
				mi.setIcon(mutated);
			}
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case com.actionbarsherlock.R.id.abs__home:
			finish();
			return (true);
		case R.id.menu_item_tutorial:
			startActivity(new Intent(this, WelcomeTutorialWizardActivity.class));
			break;
		case R.id.menu_item_about:
			startActivity(new Intent(this, AboutActivity.class));
			break;
		case R.id.menu_item_donate:
			trackPageView("donate");
			final Uri marketUri = Uri.parse("market://details?id=com.androsz.electricsleepdonate");
			final Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			startActivity(marketIntent);
			break;
		case R.id.menu_item_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.menu_item_report:
			startActivity(new Intent("android.intent.action.VIEW",
					Uri.parse("http://code.google.com/p/electricsleep/issues/entry")));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
