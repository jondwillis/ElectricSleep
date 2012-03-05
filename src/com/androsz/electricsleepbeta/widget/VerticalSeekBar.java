package com.androsz.electricsleepbeta.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class VerticalSeekBar extends DecimalSeekBar {

	private OnSeekBarChangeListener myListener;

	public VerticalSeekBar(Context context) {
		super(context);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public VerticalSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(h, w, oldh, oldw);
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(heightMeasureSpec, widthMeasureSpec);
		setMeasuredDimension(getMeasuredHeight(), getMeasuredWidth());
	}

	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener mListener) {
		this.myListener = mListener;
	}

	protected void onDraw(Canvas c) {
		c.rotate(-90);
		c.translate(-getHeight(), 0);

		super.onDraw(c);
	}

    @Override
    public synchronized void setProgress(final float progress) {
        super.setProgress(progress);
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!isEnabled()) {
			return false;
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
            setPressed(true);
            setSelected(true);
			if (myListener != null) {
                myListener.onStartTrackingTouch(this);
            }
            break;
		case MotionEvent.ACTION_MOVE:
			float dx = getFloatMax() - (getFloatMax() * event.getY() / getHeight());
			myListener.onProgressChanged(this, (int) (dx*PRECISION), true);
            setProgress(dx);
			break;
		case MotionEvent.ACTION_UP:
            setPressed(false);
            setSelected(false);
			myListener.onStopTrackingTouch(this);
			break;
		case MotionEvent.ACTION_CANCEL:
            setPressed(false);
			break;
		}
		return true;
	}
}
