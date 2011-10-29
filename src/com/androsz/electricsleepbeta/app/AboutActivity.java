package com.androsz.electricsleepbeta.app;

import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.androsz.electricsleepbeta.R;

public class AboutActivity extends HostActivity {
	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_about;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			((TextView) findViewById(R.id.about_version_text))
					.setText(getPackageManager().getPackageInfo(this.getPackageName(),
							PackageManager.GET_META_DATA).versionName);
		} catch (final NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}