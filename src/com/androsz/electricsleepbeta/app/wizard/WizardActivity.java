package com.androsz.electricsleepbeta.app.wizard;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.Button;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.HostActivity;
import com.androsz.electricsleepbeta.app.Log;
import com.androsz.electricsleepbeta.widget.DisablableTitlePageIndicator;
import com.androsz.electricsleepbeta.widget.DisablableViewPager;

public abstract class WizardActivity extends HostActivity {

	private DisablableViewPager wizardPager;
	private DisablableTitlePageIndicator indicator;
	private IndicatorPageChangeListener indicatorListener;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_wizard;
	}

	@Override
	public void onBackPressed() {
		onLeftButtonClick(null);
	}

	private final class IndicatorPageChangeListener implements
			OnPageChangeListener {

		private int lastSettledPosition = 0;
		private int lastPosition = -2;

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
			// lastPosition = position;
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			// if we are settled on a new page...
			if (state == ViewPager.SCROLL_STATE_IDLE
					&& lastSettledPosition != lastPosition) {

				lastSettledPosition = lastPosition;

				setupNavigationButtons(lastSettledPosition);
				onPerformWizardAction(lastSettledPosition);

			}
		}

		@Override
		public void onPageSelected(int position) {
			lastPosition = position;
		}

		public int getLastSettledPosition() {
			return lastSettledPosition;
		}

		public void setLastSettledPosition(int index) {
			lastSettledPosition = index;
		}
	}

	protected abstract PagerAdapter getPagerAdapter();

	protected static String makeFragmentName(int index) {
		return "android:switcher:" + R.id.wizardPager + ":" + index;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		wizardPager = (DisablableViewPager) findViewById(R.id.wizardPager);
		wizardPager.setAdapter(getPagerAdapter());

		indicator = (DisablableTitlePageIndicator) findViewById(R.id.indicator);
		indicator.setFooterColor(getResources().getColor(R.color.primary1));
		indicatorListener = new IndicatorPageChangeListener();
		indicator.setOnPageChangeListener(indicatorListener);

		int initialPosition = 0;
		indicator.setViewPager(wizardPager, initialPosition);

		Log.d("ES", "this happens");
		if (savedInstanceState != null) {
			initialPosition = savedInstanceState.getInt("child");
			setCurrentWizardIndex(initialPosition);
			indicatorListener.setLastSettledPosition(initialPosition);
			Log.d("ES", "and that happens");
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("ES", "evilonresume");
		setupNavigationButtons(wizardPager.getCurrentItem());
	}

	protected abstract void onFinishWizardActivity()
			throws IllegalStateException;

	public void onLeftButtonClick(final View v) {

		int currentItem = getCurrentWizardIndex();
		if (currentItem != 0) {
			setCurrentWizardIndex(currentItem - 1);
		} else {
			super.onBackPressed();
		}
	}

	protected void setCurrentWizardIndex(int index) {
		indicator.setCurrentItem(index, true);
		Log.d("ES", "setCurrentWizardIndex to " + index);
	}

	protected int getCurrentWizardIndex() {
		return indicatorListener.getLastSettledPosition();
	}

	protected void setPagingEnabled(boolean enabled) {
		this.indicator.setPagingEnabled(enabled);
		this.wizardPager.setPagingEnabled(enabled);
	}

	protected abstract void onPrepareLastSlide();

	public void onRightButtonClick(final View v) {

		final int lastPageIndex = getPagerAdapter().getCount() - 1;
		final int displayedChildIndex = getCurrentWizardIndex();

		if (displayedChildIndex == lastPageIndex) {
			onFinishWizardActivity();
		} else {
			setCurrentWizardIndex(displayedChildIndex + 1);
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("child", getCurrentWizardIndex());
	}

	protected abstract void onPerformWizardAction(int focusedIndex);

	protected void setupNavigationButtons(int index) {
		Log.d("ES", "setupNavigationButtons "+index);
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		final Button rightButton = (Button) findViewById(R.id.rightButton);
		final int lastChildIndex = getPagerAdapter().getCount() - 1;
		if (index > -1 && index < lastChildIndex) {
			leftButton.setText(R.string.back);
			rightButton.setText(R.string.next);
		} else if (index == lastChildIndex) {
			leftButton.setText(R.string.back);
			rightButton.setText(R.string.finish);

			onPrepareLastSlide();
		}
	}
}
