package com.androsz.electricsleepbeta.achartengine;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.androsz.electricsleepbeta.R;
import com.androsz.electricsleepbeta.achartengine.chart.AbstractChart;
import com.androsz.electricsleepbeta.achartengine.chart.XYChart;
import com.androsz.electricsleepbeta.achartengine.renderer.DefaultRenderer;
import com.androsz.electricsleepbeta.achartengine.renderer.XYMultipleSeriesRenderer;
import com.androsz.electricsleepbeta.achartengine.tools.FitZoom;
import com.androsz.electricsleepbeta.achartengine.tools.Pan;
import com.androsz.electricsleepbeta.achartengine.tools.Zoom;

/**
 * The view that encapsulates the graphical chart.
 */
public abstract class ChartView extends View {
	/** The zoom buttons background color. */
	private static final int ZOOM_BUTTONS_COLOR = Color.argb(175, 150, 150, 150);
	/** The zoom area size. */
	private static final int ZOOM_SIZE = 45;
	/** The fit zoom tool. */
	private FitZoom fitZoom;
	/** The fit zoom icon. */
	private Bitmap fitZoomImage;
	/** The chart to be drawn. */
	protected AbstractChart mChart;
	/** The user interface thread handler. */
	private Handler mHandler;
	/** The paint to be used when drawing the chart. */
	private final Paint mPaint = new Paint();
	/** The view bounds. */
	private final Rect mRect = new Rect();
	/** The chart renderer. */
	private XYMultipleSeriesRenderer mRenderer;
	/** The old x coordinate. */
	private float oldX;
	/** The old y coordinate. */
	private float oldY;
	/** The pan tool. */
	private Pan pan;
	/** The zoom in tool. */
	private Zoom zoomIn;
	/** The zoom in icon. */
	private Bitmap zoomInImage;
	/** The zoom out tool. */
	private Zoom zoomOut;
	/** The zoom out icon. */
	private Bitmap zoomOutImage;
	/** The zoom buttons rectangle. */
	private final RectF zoomR = new RectF();

	/**
	 * Creates a new graphical view.
	 * 
	 * @param context
	 *            the context
	 * @param chart
	 *            the chart to be drawn
	 */
	public ChartView(Context context) {
		super(context);
		setup(context);
	}

	public ChartView(final Context context, final AttributeSet as) {
		super(context, as);
		setup(context);
	}

	protected abstract AbstractChart buildChart();

	public void handleTouch(MotionEvent event) {
		final int action = event.getAction();
		if (mRenderer != null && action == MotionEvent.ACTION_MOVE) {
			if (oldX >= 0 || oldY >= 0) {
				final float newX = event.getX();
				final float newY = event.getY();
				if (mRenderer.isPanXEnabled() || mRenderer.isPanYEnabled()) {
					pan.apply(oldX, oldY, newX, newY);
				}
				oldX = newX;
				oldY = newY;
				repaint();
			}
		} else if (action == MotionEvent.ACTION_DOWN) {
			oldX = event.getX();
			oldY = event.getY();
			if (mRenderer != null && (mRenderer.isZoomXEnabled() || mRenderer.isZoomYEnabled())
					&& zoomR.contains(oldX, oldY)) {
				if (oldX < zoomR.left + zoomR.width() / 3) {
					zoomIn.apply();
				} else if (oldX < zoomR.left + zoomR.width() * 2 / 3) {
					zoomOut.apply();
				} else {
					fitZoom.apply();
				}
			}
		} else if (action == MotionEvent.ACTION_UP) {
			oldX = 0;
			oldY = 0;
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		canvas.getClipBounds(mRect);
		final int top = mRect.top;
		final int left = mRect.left;
		final int width = mRect.width();
		final int height = mRect.height();
		mChart.draw(canvas, left, top, width, height, mPaint);
		if (mRenderer != null && (mRenderer.isZoomXEnabled() || mRenderer.isZoomYEnabled())) {
			mPaint.setColor(ZOOM_BUTTONS_COLOR);
			zoomR.set(left + width - ZOOM_SIZE * 3, top + height - ZOOM_SIZE * 0.775f,
					left + width, top + height);
			canvas.drawRoundRect(zoomR, ZOOM_SIZE / 3, ZOOM_SIZE / 3, mPaint);
			final float buttonY = top + height - ZOOM_SIZE * 0.625f;
			canvas.drawBitmap(zoomInImage, left + width - ZOOM_SIZE * 2.75f, buttonY, null);
			canvas.drawBitmap(zoomOutImage, left + width - ZOOM_SIZE * 1.75f, buttonY, null);
			canvas.drawBitmap(fitZoomImage, left + width - ZOOM_SIZE * 0.75f, buttonY, null);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mRenderer != null
				&& (mRenderer.isPanXEnabled() || mRenderer.isZoomYEnabled()
						|| mRenderer.isZoomXEnabled() || mRenderer.isZoomYEnabled())) {
			handleTouch(event);
			return true;
		}
		return super.onTouchEvent(event);
	}

	/**
	 * Schedule a view content repaint.
	 */
	public void repaint() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidate();
			}
		});
	}

	/**
	 * Schedule a view content repaint, in the specified rectangle area.
	 * 
	 * @param left
	 *            the left position of the area to be repainted
	 * @param top
	 *            the top position of the area to be repainted
	 * @param right
	 *            the right position of the area to be repainted
	 * @param bottom
	 *            the bottom position of the area to be repainted
	 */
	public void repaint(final int left, final int top, final int right, final int bottom) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				invalidate(left, top, right, bottom);
			}
		});
	}

	private void setup(final Context context) {
		mChart = buildChart();
		mHandler = new Handler();
		if (mChart instanceof XYChart) {
			final Resources res = context.getResources();
			zoomInImage = BitmapFactory.decodeResource(res, R.drawable.zoom_in);
			zoomOutImage = BitmapFactory.decodeResource(res, R.drawable.zoom_out);
			fitZoomImage = BitmapFactory.decodeResource(res, R.drawable.zoom_1);
			mRenderer = ((XYChart) mChart).getRenderer();
			if (mRenderer.getMarginsColor() == DefaultRenderer.NO_COLOR) {
				mRenderer.setMarginsColor(mPaint.getColor());
			}
			if (mRenderer.isPanXEnabled() || mRenderer.isPanYEnabled()) {
				pan = new Pan((XYChart) mChart);
			}
			if (mRenderer.isZoomXEnabled() || mRenderer.isZoomYEnabled()) {
				zoomIn = new Zoom((XYChart) mChart, true, mRenderer.getZoomRate());
				zoomOut = new Zoom((XYChart) mChart, false, mRenderer.getZoomRate());
				fitZoom = new FitZoom((XYChart) mChart);
			}
		}
	}

}
