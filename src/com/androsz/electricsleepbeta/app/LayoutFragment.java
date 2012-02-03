package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class LayoutFragment extends Fragment {

	public static LayoutFragment newInstance(final int layoutId) {
		return new LayoutFragment() {
			@Override
			public int getLayoutResourceId() {
				return layoutId;
			}
		};
	}
	
	public abstract int getLayoutResourceId();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(getLayoutResourceId(), container, false);
	}
}