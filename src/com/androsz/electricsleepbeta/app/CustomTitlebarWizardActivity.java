package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.widget.ViewFlipperBugfix;

public abstract class CustomTitlebarWizardActivity extends HostActivity {

	protected ViewFlipperBugfix viewFlipper;

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_wizard;
	}

	protected abstract int getWizardLayoutId();

	@Override
	public void onBackPressed() {
		onLeftButtonClick(null);
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ViewStub wizardViewFlipperStub = (ViewStub) findViewById(R.id.wizardViewFlipperStub);
		wizardViewFlipperStub.setLayoutResource(getWizardLayoutId());
		viewFlipper = (ViewFlipperBugfix) wizardViewFlipperStub.inflate();
		setupNavigationButtons();
	}

	protected abstract void onFinishWizardActivity();

	public void onLeftButtonClick(final View v) {

		if (viewFlipper.getDisplayedChild() != 0) {
			viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_left_in));
			viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.slide_left_out));
			viewFlipper.showPrevious();
			setupNavigationButtons();
		} else {
			super.onBackPressed();
		}
	}

	protected abstract void onPrepareLastSlide();

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		viewFlipper.setDisplayedChild(savedState.getInt("child"));

		setupNavigationButtons();
	}

	public void onRightButtonClick(final View v) {
		viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_right_in));
		viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
				R.anim.slide_right_out));

		final int lastChildIndex = viewFlipper.getChildCount() - 1;
		final int displayedChildIndex = viewFlipper.getDisplayedChild();

		if (displayedChildIndex == lastChildIndex) {
			onFinishWizardActivity();
		} else {
			if (!onWizardActivity()) {
				viewFlipper.showNext();
				setupNavigationButtons();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("child", viewFlipper.getDisplayedChild());
	}

	protected abstract boolean onWizardActivity();

	protected void setupNavigationButtons() {
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		final Button rightButton = (Button) findViewById(R.id.rightButton);
		final int lastChildIndex = viewFlipper.getChildCount() - 1;
		final int displayedChildIndex = viewFlipper.getDisplayedChild();
		if (displayedChildIndex > -1 && displayedChildIndex < lastChildIndex) {
			leftButton.setText(R.string.back);
			rightButton.setText(R.string.next);
		} else if (displayedChildIndex == lastChildIndex) {
			leftButton.setText(R.string.back);
			rightButton.setText(R.string.finish);

			onPrepareLastSlide();
		}
	}
}
