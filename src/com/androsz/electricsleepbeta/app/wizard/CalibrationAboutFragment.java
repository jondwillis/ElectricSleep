package com.androsz.electricsleepbeta.app.wizard;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.LayoutFragment;

import android.os.Bundle;

public class CalibrationAboutFragment extends LayoutFragment
{

	@Override
	public int getLayoutResourceId() {
		// TODO Auto-generated method stub
		return R.layout.wizard_calibration_about;
	}

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("DUMMY", 0);
    }
}