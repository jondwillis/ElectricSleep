package com.androsz.electricsleepbeta.widget;

import com.androsz.electricsleepbeta.R;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.widget.Button;

public class TintableButton extends Button {
    public TintableButton(final Context context) {
        this(context, null);
    }

    public TintableButton(final Context context, final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TintableButton(final Context context, final AttributeSet attrs,
            int defStyle) {
        super(context, attrs);

        // Now begin processing attributes
        final Resources resources = context.getResources();
        final TypedArray array = context.obtainStyledAttributes(attrs,
                R.styleable.TintableButton, defStyle, 0);

        // background drawable color tint
        if (array.hasValue(R.styleable.TintableButton_lightingTint)) {
            getBackground().setColorFilter(
                    new LightingColorFilter(0xFFFFFFFF, array.getColor(
                            R.styleable.TintableButton_lightingTint,
                            Color.BLACK)));
        }
        if (array.hasValue(R.styleable.TintableButton_porterDuffTint)) {
            getBackground().setColorFilter(
                    array.getColor(R.styleable.TintableButton_porterDuffTint,
                            Color.BLACK), PorterDuff.Mode.MULTIPLY);
        }
    }

}
