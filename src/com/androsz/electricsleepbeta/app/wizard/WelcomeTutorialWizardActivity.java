package com.androsz.electricsleepbeta.app.wizard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActionBar;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.SettingsActivity;
import com.androsz.electricsleepbeta.app.ZeoSplashActivity;
import com.viewpagerindicator.TitleProvider;

public class WelcomeTutorialWizardActivity extends WizardActivity {

	public static boolean enforceCalibrationBeforeStartingSleep(final Activity context) {

		final SharedPreferences userPrefs = context.getSharedPreferences(
				SettingsActivity.PREFERENCES_ENVIRONMENT, Context.MODE_PRIVATE);
		final int prefsVersion = userPrefs.getInt(SettingsActivity.PREFERENCES_ENVIRONMENT, 0);
		String message = "";
		if (prefsVersion == 0) {
			message = context.getString(R.string.message_not_calibrated);
		} else if (prefsVersion != context.getResources().getInteger(R.integer.prefs_version)) {
			message = context.getString(R.string.message_prefs_not_compatible);
		}

		if (message.length() > 0) {
			message += context.getString(R.string.message_recommend_calibration);
			final AlertDialog.Builder dialog = new AlertDialog.Builder(context)
					.setMessage(message)
					.setCancelable(false)
					.setPositiveButton(context.getString(R.string.calibrate),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int id) {
									context.startActivity(new Intent(context,
											CalibrationWizardActivity.class));
									context.finish();
								}
							})
					.setNeutralButton(context.getString(R.string.manual),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int id) {
									context.startActivity(new Intent(context,
											SettingsActivity.class));
									context.finish();
								}
							})
					.setNegativeButton(context.getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(final DialogInterface dialog, final int id) {
									dialog.cancel();
								}
							});
			dialog.show();
			return false;
		} else {
			return true;
		}
	}

	private boolean required = false;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		required = getIntent().hasExtra("required");
		if (required) {
			final ActionBar bar = getSupportActionBar();
			bar.setDisplayHomeAsUpEnabled(false);
		}
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_learn_more:
			ZeoSplashActivity.learnMore(this);
			break;
		}
	}

	// Prevents options from showing up if required
	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		if (required) {
			return false;
		} else {
			return super.onCreateOptionsMenu(menu);
		}
	}

	// Prevents Home button from triggering if required
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		if (required && item.getItemId() == android.R.id.home) {
			return false;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onFinishWizardActivity() {
		if (enforceCalibrationBeforeStartingSleep(this)) {
			finish();
		}
	}

	@Override
	public void onLeftButtonClick(final View v) {
		if (getCurrentWizardIndex() == 0) {
			onFinishWizardActivity();
		} else {
			super.onLeftButtonClick(v);
		}
	}

	@Override
	protected void onPrepareLastSlide() {
	}

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {
		super.onRestoreInstanceState(savedState);
		required = savedState.getBoolean("required");
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("required", required);
	}

	@Override
	protected boolean onPerformWizardAction() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void setupNavigationButtons() {
		super.setupNavigationButtons();
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		if (getCurrentWizardIndex() == 0) {
			leftButton.setText(R.string.skip_tutorial);
		}
	}

	private class WizardPagerAdapter extends PagerAdapter implements TitleProvider {

		private String[] titles = new String[] { "Zeo", "Welcome", "How It Works" };

		@Override
		public String getTitle(int position) {
			return titles[position];
		}

		@Override
		public int getCount() {
			return titles.length;
		}

		@Override
		public void startUpdate(View container) {
			// TODO Auto-generated method stub

		}

		@Override
		public Object instantiateItem(View container, int position) {
			View instantiatedItem = null;
			LayoutInflater inflater = getLayoutInflater();
			switch (position) {
			case 0:
				instantiatedItem = inflater.inflate(R.layout.activity_zeo_splash, null);
				break;
			case 1:
				instantiatedItem = inflater.inflate(R.layout.wizard_welcome_welcome, null);
				break;
			case 2:
				instantiatedItem = inflater.inflate(R.layout.wizard_welcome_how, null);
				break;
			}
			((ViewGroup) container).addView(instantiatedItem);
			return instantiatedItem;
		}

		@Override
		public void destroyItem(View container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}

		@Override
		public void finishUpdate(View container) {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean isViewFromObject(View view, Object object) {
			return ((View) object).equals(view);
		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void restoreState(Parcelable state, ClassLoader loader) {
			// TODO Auto-generated method stub

		}

	}

	@Override
	protected PagerAdapter getPagerAdapter() {
		return new WizardPagerAdapter();
	}
}
