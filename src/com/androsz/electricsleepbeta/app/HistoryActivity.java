package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import com.androsz.electricsleepbeta.R;

public class HistoryActivity extends HostActivity {

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // Set the default navigation mode as the history month fragment
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(android.R.id.content) == null) {
            manager.beginTransaction().add(android.R.id.content, new HistoryListFragment()).commit();
        }
    }

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}
}
