package com.androsz.electricsleep.ui;

import com.androsz.electricsleep.R;
import com.androsz.electricsleep.R.id;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public abstract class CustomTitlebarActivity extends Activity {

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFormat(PixelFormat.RGBA_8888);

		setContentView(R.layout.titlebar);
		View.inflate(this, getContentAreaLayoutId(),
				(ViewGroup) findViewById(R.id.custom_titlebar_container));
	}

	public void setContentView(int layoutResID) {
		super.setContentView(layoutResID);

		((TextView) findViewById(R.id.title_text)).setText(getTitle());
		/*
		 * if(!isHomeActivity()) { ((ImageButton)
		 * findViewById(R.id.title_home_button)) .setOnClickListener(new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { v.getContext().startActivity(
		 * new Intent(v.getContext(), HomeActivity.class)); } }); }
		 */
	}

	public abstract int getContentAreaLayoutId();

	public void onHomeClick(View v) {
		final Intent intent = new Intent(v.getContext(), HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		v.getContext().startActivity(intent);
	}

	public void showTitleButton1(int drawableResourceId) {
		ImageButton btn1 = (ImageButton) findViewById(R.id.title_button_1);
		btn1.setVisibility(View.VISIBLE);
		btn1.setImageResource(drawableResourceId);
		findViewById(R.id.title_sep_1).setVisibility(View.VISIBLE);
	}

	public void showTitleButton2(int drawableResourceId) {
		ImageButton btn2 = (ImageButton) findViewById(R.id.title_button_2);
		btn2.setVisibility(View.VISIBLE);
		btn2.setImageResource(drawableResourceId);
		findViewById(R.id.title_sep_2).setVisibility(View.VISIBLE);
	}
}
