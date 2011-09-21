package com.androsz.electricsleepbeta.widget;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

import com.androsz.electricsleepbeta.util.IntentUtil;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

public class BlockableAdView extends AdView {

	public BlockableAdView(Activity activity, AdSize adSize, String adUnitId) {
		super(activity, adSize, adUnitId);
	}

	public BlockableAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public BlockableAdView(Context arg0, AttributeSet arg1, int arg2) {
		super(arg0, arg1, arg2);
	}

	@Override
	public void loadAd(AdRequest adRequest) {
		// extremely simple way of determining if we should load an ad!
		// if the donate app is installed, don't show it.
		if (IntentUtil.isApplicationInstalled(getContext(), "com.androsz.electricsleep")) {
			this.stopLoading();
		} else {
			super.loadAd(adRequest);
		}
	}

}
