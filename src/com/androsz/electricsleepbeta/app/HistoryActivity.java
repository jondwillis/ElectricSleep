package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.support.v4.app.ActionBar.OnNavigationListener;
import android.support.v4.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.ArrayAdapter;

import com.androsz.electricsleepbeta.R;

public class HistoryActivity extends HostActivity implements OnNavigationListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this,
				R.array.review_sleep_actionbar_navigation, R.layout.abs__simple_spinner_item);
		list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bar.setListNavigationCallbacks(list, this);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		Fragment newActiveFragment = null;

        // TODO
        /*
		if (itemPosition == 0) {
		} else {
		}
        */

		ft.replace(R.id.frags, newActiveFragment).commit();
		return true;
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}
}
