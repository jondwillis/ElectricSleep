package com.androsz.electricsleepbeta.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsSessionHelper;
import com.androsz.electricsleepbeta.util.GoogleAnalyticsTrackerHelper;

public class SuggestionFragment extends LayoutFragment {

	private void setOnClickForEntireViewGroup(View v,
			BubbleDownOnClickListener l) {
		v.setOnClickListener(l);

		if (v instanceof ViewGroup) {
			for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
				((ViewGroup) v).getChildAt(i).setOnClickListener(l);
			}
		}
	}

	class BubbleDownOnClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if (v instanceof ViewGroup) {
				for (int i = 0; i < ((ViewGroup) v).getChildCount(); i++) {
					((ViewGroup) v).getChildAt(i).performClick();
				}
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity a = getActivity();

		setOnClickForEntireViewGroup(a.findViewById(R.id.btn_sleep_store),
				new BubbleDownOnClickListener() {
					@Override
					public void onClick(View v) {
						super.onClick(v);
						trackPageView("SleepScore");
						startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
								.parse("http://myzeo.com/sleep/shop/zeo-recommended.html")));
					}
				});
		setOnClickForEntireViewGroup(a.findViewById(R.id.btn_expert_advice),
				new BubbleDownOnClickListener() {
					@Override
					public void onClick(View v) {
						super.onClick(v);
						trackPageView("ExpertAdvice");
						startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri
								.parse("http://www.myzeo.com/expert_advice/")));
					}
				});
		setOnClickForEntireViewGroup(a.findViewById(R.id.btn_consultation),
				new BubbleDownOnClickListener() {
					@Override
					public void onClick(View v) {
						super.onClick(v);
					}
				});
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_suggestion;
	}
}
