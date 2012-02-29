package com.androsz.electricsleepbeta.app;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.androsz.electricsleepbeta.R;

public class SuggestionFragment extends LayoutFragment {

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		Activity a = getActivity();
		a.findViewById(R.id.btn_sleep_store).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
					    startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.myzeo.com/sleep/shop/zeo-recommended.html")));
					}
				});
		a.findViewById(R.id.btn_expert_advice).setOnClickListener(
				new OnClickListener() {
					@Override
					public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://myzeo.com/")));
					}
				});
		a.findViewById(R.id.btn_consultation).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
                        startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://myzeo.com/")));
					}
				});
	}

	@Override
	public int getLayoutResourceId() {
		return R.layout.fragment_suggestion;
	}
}