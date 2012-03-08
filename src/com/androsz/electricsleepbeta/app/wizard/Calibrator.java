package com.androsz.electricsleepbeta.app.wizard;

import com.androsz.electricsleepbeta.app.LayoutFragment;

import android.app.Activity;
import android.os.Bundle;

public abstract class Calibrator extends LayoutFragment {

	protected CalibratorStateListener calibrationStateListener;
	
	public void setCalibratorStateListener(CalibratorStateListener csl)
	{
		calibrationStateListener = csl;
	}

	public abstract void startCalibration(Activity context);

	public abstract void stopCalibration(Activity context);
}
