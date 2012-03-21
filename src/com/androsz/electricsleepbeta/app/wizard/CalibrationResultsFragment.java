package com.androsz.electricsleepbeta.app.wizard;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.app.LayoutFragment;

import android.os.Bundle;

public class CalibrationResultsFragment extends LayoutFragment
{
	@Override
	public int getLayoutResourceId() {
		return R.layout.wizard_calibration_results;
	}


    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("DUMMY", 0);
    }
}