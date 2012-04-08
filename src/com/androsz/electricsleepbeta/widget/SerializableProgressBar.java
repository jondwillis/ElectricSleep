package com.androsz.electricsleepbeta.widget;

import java.io.Serializable;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class SerializableProgressBar extends ProgressBar implements Serializable {

	private static final long serialVersionUID = -2755645944654374920L;

	public SerializableProgressBar(final Context context) {
		this(context, null);
	}
	public SerializableProgressBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	public SerializableProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

}