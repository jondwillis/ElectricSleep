package com.androsz.electricsleep.app;

import com.androsz.electricsleep.R;

public class WelcomeTutorialWizardActivity extends CustomTitlebarWizardActivity {

	@Override
	protected int getWizardLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.wizard_welcome;
	}

	@Override
	protected boolean onWizardActivity() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onFinishWizardActivity() {
		finish();
	}

	@Override
	protected void onPrepareLastSlide() {
		// TODO Auto-generated method stub

	}
}
