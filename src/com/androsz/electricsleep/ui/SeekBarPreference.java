package com.androsz.electricsleep.ui;

import java.text.NumberFormat;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class SeekBarPreference extends DialogPreference {

	private Context context;
	private SeekBar seekBar;
	private TextView textView;

	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	protected void onPrepareDialogBuilder(Builder builder) {

		LinearLayout layout = new LinearLayout(context);
		layout.setLayoutParams(new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT));
		layout.setMinimumWidth(400);
		layout.setPadding(20, 20, 20, 20);

		textView = new TextView(context);
		textView.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));
		syncTextViewText(getPersistedInt(0));
		textView.setPadding(5, 5, 5, 5);
		layout.addView(textView);

		seekBar = new SeekBar(context);
		seekBar.setMax(100);
		seekBar.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT));

		seekBar.setProgress(getPersistedInt(0));
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				syncTextViewText(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {}
		});
		layout.addView(seekBar);

		builder.setView(layout);
		builder.setTitle(getTitle());

		super.onPrepareDialogBuilder(builder);
	}

	private void syncTextViewText(int progress) {
		NumberFormat nf = NumberFormat.getIntegerInstance();
		nf.setMinimumIntegerDigits(3);
		textView.setText(nf.format(progress));
	}

	protected void onDialogClosed(boolean positiveResult) {
		if (positiveResult) {
			persistInt(seekBar.getProgress());
			//setSummary(String.format("The value is %d", seekBar.getProgress()));
		}
	}
}
