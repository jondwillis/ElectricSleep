package com.androsz.electricsleep.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.View;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.util.AlarmDatabase;

import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

public class AlarmsActivity extends CustomTitlebarActivity {

	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		List<ApplicationInfo> apps = AlarmDatabase.getPossibleAlarmClocks(getPackageManager());
		List<String> appnames = new ArrayList<String>(apps.size());
		for(ApplicationInfo appInfo : apps)
		{
			appnames.add(appInfo.packageName);
		}

        Spinner s1 = (Spinner) findViewById(R.id.Spinner01);
 
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_dropdown_item, appnames);
 
        s1.setAdapter(adapter);
	}
	
	@Override
	public int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_alarms;
	}

	public void onChangeAlarmSettings(View v)
	{
		AlarmDatabase adb = new AlarmDatabase(getContentResolver(), "com.android.deskclock");
		CharSequence text = adb.getNearestEnabledAlarm().audio;
		int duration = Toast.LENGTH_SHORT;

		Toast.makeText(getApplicationContext(), text, duration).show();
		
		//startActivity(AlarmDatabase.startNightClock());
		//startActivity(AlarmDatabase.changeAlarmSettings(getPackageManager()));
	}
	
}
