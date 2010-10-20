package com.androsz.electricsleep.app;

import android.os.Bundle;
import android.view.View;
import android.view.ViewStub;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.androsz.electricsleep.R;

public abstract class CustomTitlebarWizardActivity extends
		CustomTitlebarActivity {

	@Override
	protected int getContentAreaLayoutId() {
		return R.layout.activity_wizard;
	}

	protected abstract int getWizardLayoutId();

	protected ViewFlipper viewFlipper;

	protected abstract boolean onWizardActivity();

	protected abstract void onFinishWizardActivity();
	
	protected abstract void onPrepareLastSlide();

	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		ViewStub wizardViewFlipperStub = (ViewStub) findViewById(R.id.wizardViewFlipperStub);
		wizardViewFlipperStub.setLayoutResource(getWizardLayoutId());
		viewFlipper = (ViewFlipper)wizardViewFlipperStub.inflate();
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
	public void onBackPressed() {
		onLeftButtonClick(null);
	}

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

	protected void setupNavigationButtons() {
		final Button leftButton = (Button) findViewById(R.id.leftButton);
		final Button rightButton = (Button) findViewById(R.id.rightButton);
		final int lastChildIndex = viewFlipper.getChildCount() - 1;
		final int displayedChildIndex = viewFlipper.getDisplayedChild();
		if (displayedChildIndex > -1
				&& displayedChildIndex < lastChildIndex) {
			leftButton.setText(R.string.back);
			rightButton.setText(R.string.next);
		}
		else if (displayedChildIndex == lastChildIndex) {
			leftButton.setText(R.string.back);
			rightButton.setText(R.string.finish);

			onPrepareLastSlide();
		}
	}
	

	@Override
	protected void onRestoreInstanceState(final Bundle savedState) {

		super.onRestoreInstanceState(savedState);

		viewFlipper.setDisplayedChild(savedState.getInt("child"));
		
		setupNavigationButtons();
	}

	@Override
	protected void onSaveInstanceState(final Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("child", viewFlipper.getDisplayedChild());
	}
}
