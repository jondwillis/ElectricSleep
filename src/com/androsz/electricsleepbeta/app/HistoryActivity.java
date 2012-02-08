package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.support.v4.app.ActionBar.OnNavigationListener;
import android.support.v4.app.ActionBar;
import android.support.v4.app.FragmentManager;
import android.widget.ArrayAdapter;

import com.androsz.electricsleepbeta.R;

public class HistoryActivity extends HostActivity implements OnNavigationListener {

    // Please note that the following values are highly dependent upon items held in the list
    // drop-down etc...
    private static final int FRAGMENT_MONTH_VIEW = 0;
    private static final int FRAGMENT_LIST = 1;

    /** The fragment currently selected. */
    private int mSelectedFragment;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ActionBar bar = getSupportActionBar();
		bar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this,
				R.array.review_sleep_actionbar_navigation, R.layout.abs__simple_spinner_item);
		list.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		bar.setListNavigationCallbacks(list, this);

        // Set the default navigation mode as the history month fragment
        FragmentManager manager = getSupportFragmentManager();
        if (manager.findFragmentById(android.R.id.content) == null) {
            manager.beginTransaction().add(android.R.id.content, new HistoryMonthFragment()).commit();
            mSelectedFragment = FRAGMENT_MONTH_VIEW;
        }
    }

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {

        if (itemPosition == mSelectedFragment) {
            // TODO potentially return false here ?
            return true;
        }

        switch (itemPosition) {
        case FRAGMENT_MONTH_VIEW:
            getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new HistoryMonthFragment())
                .commit();
            break;
        case FRAGMENT_LIST:
            getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new HistoryListFragment())
                .commit();
            break;
        }
        mSelectedFragment = itemPosition;

        return true;
	}

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_history;
	}
}
