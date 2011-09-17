package com.androsz.electricsleepbeta.app;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ViewSwitcher;

import com.androsz.electricsleepbeta.widget.calendar.MonthView;

public class MonthFragment extends Fragment {
	final MonthView mv;

	public MonthFragment(HistoryMonthActivity activity, Time time) {
		super();
		try {
			mv = new MonthView(activity);
			mv.setLayoutParams(new ViewSwitcher.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.MATCH_PARENT));
			mv.setSelectedTime(time);
		} catch (ClassCastException cce) {
			throw new ClassCastException(
					"A MonthView can only be held by a HistoryMonthActivity");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		return mv;
	}
}
