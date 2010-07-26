/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.androsz.electricsleep.ui;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.R.drawable;
import com.androsz.electricsleep.R.layout;
import com.androsz.electricsleep.service.SleepAccelerometerService;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Front-door {@link Activity} that displays high-level features the application
 * offers to users.
 */
public class HomeActivity extends CustomTitlebarActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// change title in titlebar without changing the app's name in the
		// launcher
		this.setTitle(R.string.title_home);

		super.onCreate(savedInstanceState);

		showTitleButton1(R.drawable.ic_title_export);
		showTitleButton2(R.drawable.ic_title_refresh);
	}
	
	public void onHomeClick(View v){}//do nothing b/c home is home!

	public void onTitleButton1Click(View v) {
		Toast.makeText(getApplicationContext(), "ohhh", Toast.LENGTH_SHORT);
	}
	
	@Override
	public int getContentAreaLayoutId() {
		return R.layout.activity_home;
	}

	public void onSleepClick(View v) {
		startService(new Intent(this, SleepAccelerometerService.class));
		startActivity(new Intent(this, SleepActivity.class));
	}

	public void onTestClick(View v) {
		startActivity(new Intent(this, TestActivity.class));
	}

	public void onAlarmsClick(View v) {
		startActivity(new Intent(this, AlarmsActivity.class));
	}

	public void onSettingsClick(View v) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onHistoryClick(View v) {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	public void onCloudClick(View v) {
		startActivity(new Intent(this, CloudActivity.class));
		// startActivity(new Intent(Intent.ACTION_VIEW, CloudAc));
	}
}
