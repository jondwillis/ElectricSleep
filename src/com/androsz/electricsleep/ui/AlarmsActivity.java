package com.androsz.electricsleep.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.util.AlarmDatabase;

public class AlarmsActivity extends CustomTitlebarActivity {

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_alarms;
	}

	public void onChangeAlarmSettings(View v) {
		final AlarmDatabase adb = new AlarmDatabase(getContentResolver(),
				"com.android.deskclock");
		adb.getNearestEnabledAlarm().getNearestAlarmDate();
		final CharSequence text = adb.getNearestEnabledAlarm().alert.toString();
		final int duration = Toast.LENGTH_SHORT;

		Toast.makeText(getApplicationContext(), text, duration).show();

		// startActivity(AlarmDatabase.startNightClock());
		// startActivity(AlarmDatabase.changeAlarmSettings(getPackageManager()));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final List<ApplicationInfo> apps = AlarmDatabase
				.getPossibleAlarmClocks(getPackageManager());
		final List<String> appnames = new ArrayList<String>(apps.size());
		for (final ApplicationInfo appInfo : apps) {
			appnames.add(appInfo.packageName);
		}

		final Spinner s1 = (Spinner) findViewById(R.id.Spinner01);

		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, appnames);

		s1.setAdapter(adapter);
	}

}
