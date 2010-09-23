package com.androsz.electricsleep.ui;

import android.content.Intent;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.androsz.electricsleep.R;

public abstract class CustomTitlebarPreferenceActivity extends
		PreferenceActivity {

	protected abstract int getContentAreaLayoutId();

	public void hideTitleButton1() {
		final ImageButton btn1 = (ImageButton) findViewById(R.id.title_button_1);
		btn1.setVisibility(View.INVISIBLE);
		findViewById(R.id.title_sep_1).setVisibility(View.INVISIBLE);
	}

	public void hideTitleButton2() {
		final ImageButton btn2 = (ImageButton) findViewById(R.id.title_button_2);
		btn2.setVisibility(View.INVISIBLE);
		findViewById(R.id.title_sep_2).setVisibility(View.INVISIBLE);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		getWindow().setFormat(PixelFormat.RGBA_8888);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		setTheme(R.style.Theme_electricsleep);
		final ListView lvw = getListView();
		lvw.setCacheColorHint(0);
		lvw.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.gradient_background_vert));
		addPreferencesFromResource(getContentAreaLayoutId());
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.titlebar);
		((TextView) findViewById(R.id.title_text)).setText(getTitle());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.titlebar_menu, menu);
		return true;
	}

	public void onHomeClick(View v) {
		final Intent intent = new Intent(v.getContext(), HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuItemDonate:
			final Uri marketUri = Uri.parse("market://details?id=com.androsz.electricsleepdonate");
			final Intent marketIntent = new Intent(Intent.ACTION_VIEW, marketUri);
			startActivity(marketIntent);
			return true;
		case R.id.menuItemSettings:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menuItemExit:
			finish();
		default:
			return false;
		}
	}

	public void setHomeButtonAsLogo() {
		final ImageButton btnHome = (ImageButton) findViewById(R.id.title_home_button);
		btnHome.setImageResource(R.drawable.logo);
	}

	public void showTitleButton1(int drawableResourceId) {
		final ImageButton btn1 = (ImageButton) findViewById(R.id.title_button_1);
		btn1.setVisibility(View.VISIBLE);
		btn1.setImageResource(drawableResourceId);
		findViewById(R.id.title_sep_1).setVisibility(View.VISIBLE);
	}

	public void showTitleButton2(int drawableResourceId) {
		final ImageButton btn2 = (ImageButton) findViewById(R.id.title_button_2);
		btn2.setVisibility(View.VISIBLE);
		btn2.setImageResource(drawableResourceId);
		findViewById(R.id.title_sep_2).setVisibility(View.VISIBLE);
	}
}
