package com.androsz.electricsleepbeta.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.wizard.CalibrationWizardActivity;
import com.androsz.electricsleepbeta.app.wizard.WelcomeTutorialWizardActivity;

public abstract class HostActivity extends AnalyticActivity {

    private static final String TAG = HostActivity.class.getSimpleName();

	protected abstract int getContentAreaLayoutId();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final View root = getLayoutInflater().inflate(getContentAreaLayoutId(), null, false);
		setContentView(root);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.menu_host, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return (true);
		case R.id.menu_item_tutorial:
			startActivity(new Intent(this, WelcomeTutorialWizardActivity.class));
			break;
		case R.id.menu_item_calibrate:
			startActivity(new Intent(this, CalibrationWizardActivity.class));
			break;
		case R.id.menu_item_settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		case R.id.menu_item_report:
			String versionName = "???";
			try {
				versionName = getPackageManager().getPackageInfo(this.getPackageName(),
						PackageManager.GET_META_DATA).versionName;
			} catch (NameNotFoundException e) {
				this.trackEvent("Retrieving VersionName failed for HostActivity.", 1);
				break;
			}

			final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			emailIntent.setType("plain/text");
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
					new String[] { getString(R.string.developer_email_address) });
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
					getString(R.string.email_developer_subject, versionName));
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, getString(R.string.developer_email_body));
			startActivity(Intent.createChooser(emailIntent, getString(R.string.title_report)));
			// startActivity(new Intent("android.intent.action.VIEW",
			// Uri.parse("http://code.google.com/p/electricsleep/issues/entry")));
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
