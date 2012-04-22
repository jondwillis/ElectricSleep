package com.androsz.electricsleepbeta.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.actionbarsherlock.view.Window;
import com.androsz.electricsleepbeta.R;

public class HistoryActivity extends HostActivity {

	/**
	 * Reads the user's preference for which history view to show. Defaults to
	 * List.
	 */
	private final AsyncTask<Void, Void, Void> mLoadFragmentTask = new AsyncTask<Void, Void, Void>() {

		@Override
		protected Void doInBackground(Void... params) {
			FragmentManager manager = getSupportFragmentManager();

			// make sure we are starting from scratch. if not, just exit the
			// task.
			if (manager.findFragmentById(android.R.id.content) != null) {
				return null;
			}

			final SharedPreferences userPrefs = HistoryActivity.this
					.getSharedPreferences(
							SettingsActivity.PREFERENCES_ENVIRONMENT,
							Context.MODE_PRIVATE);

			final boolean viewHistoryAsList = userPrefs
					.getBoolean(
							SettingsActivity.PREFERENCES_KEY_HISTORY_VIEW_AS_LIST,
							true);
			if (isCancelled()) {
				return null;
			}
			// Set the default navigation mode as the history month fragment
			Fragment historyFragment = viewHistoryAsList ? new HistoryListFragment()
					: new HistoryMonthFragment();
			manager.beginTransaction()
					.add(android.R.id.content, historyFragment).commit();

			return null;
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(savedInstanceState);

		mLoadFragmentTask.execute();
	}

	@Override
	protected void onDestroy() {
		mLoadFragmentTask.cancel(true);
		super.onDestroy();
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}
}
