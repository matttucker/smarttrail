package org.bouldermountainbike.smarttrail.ui;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.bouldermountainbike.smarttrail.R;
import org.bouldermountainbike.smarttrail.model.Condition;

//import org.bouldermountainbike.smarttrail.R;

public class ConditionView extends View {
	private static final String TAG = ConditionView.class.getSimpleName();
	private static final int NUM_BINS = 4;
	private int mWidth;
	private int mHeight;

	private Paint mBarPaint;
	private Paint mIndicatorPaint;
	private Paint mTextPaint;

	Path path = new Path();
	Point mPointCenter = new Point();
	Point mPointLeft = new Point();
	Point mPointRight = new Point();

	float[] mDetents;
	private float mHalfBinWidth;
	private Paint mGoodPaint;
	private Paint mFairPaint;
	private Paint mPoorPaint;
	private Paint mClosedPaint;
	private int mStatusIndex;

	public ConditionView(Context context) {
		super(context);
		init(context);
	}

	public ConditionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ConditionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		Log.i(TAG, "init");
		Resources res = context.getResources();

		mBarPaint = new Paint();
		mBarPaint.setColor(Color.DKGRAY);
		mBarPaint.setStyle(Style.FILL);

		mGoodPaint = new Paint();
		mGoodPaint.setColor(res.getColor(R.color.conditionGood));
		mGoodPaint.setStyle(Style.FILL);

		mFairPaint = new Paint();
		mFairPaint.setColor(res.getColor(R.color.conditionFair));
		mFairPaint.setStyle(Style.FILL);

		mPoorPaint = new Paint();
		mPoorPaint.setColor(res.getColor(R.color.conditionPoor));
		mPoorPaint.setStyle(Style.FILL);

		mClosedPaint = new Paint();
		mClosedPaint.setColor(res.getColor(R.color.conditionClosed));
		mClosedPaint.setStyle(Style.FILL);

		mIndicatorPaint = new Paint();
		mIndicatorPaint.setColor(Color.DKGRAY);
		mIndicatorPaint.setStyle(Style.FILL);
		path.setFillType(Path.FillType.EVEN_ODD);

		mTextPaint = new Paint();
		mTextPaint.setColor(Color.WHITE);
		mTextPaint.setTextAlign(Paint.Align.CENTER);
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextPaint.setAntiAlias(true);

	}

	public String getStatus() {
		switch (mStatusIndex) {
		case 0:
			return Condition.GOOD;
		case 1:
			return Condition.FAIR;
		case 2:
			return Condition.POOR;
		case 3:
			return Condition.CLOSED;
		default:
			return Condition.GOOD;
		}
	}

	public void setStatus(int status) {
		mStatusIndex = status;
		moveIndicator(mDetents[mStatusIndex]);
		invalidate();
	}

	/**
	 * Sets the text size for a Paint object so a given string of text will be a
	 * given width.
	 * 
	 * @param paint
	 *            the Paint to set the text size for
	 * @param desiredWidth
	 *            the desired width
	 * @param text
	 *            the text that should be that width
	 */
	private static void setTextSizeForWidth(Paint paint, float desiredWidth,
			String text) {

		// Pick a reasonably large value for the test. Larger values produce
		// more accurate results, but may cause problems with hardware
		// acceleration. But there are workarounds for that, too; refer to
		// http://stackoverflow.com/questions/6253528/font-size-too-large-to-fit-in-cache
		final float testTextSize = 48f;

		// Get the bounds of the text, using our testTextSize.
		paint.setTextSize(testTextSize);
		Rect bounds = new Rect();
		paint.getTextBounds(text, 0, text.length(), bounds);

		// Calculate the desired size as a proportion of our testTextSize.
		float desiredTextSize = testTextSize * desiredWidth / bounds.width();

		// Set the paint for that size.
		paint.setTextSize(desiredTextSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// compute our measured dimensions
		int width = View.resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		int height = View.resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		setMeasuredDimension(width, height);

		// extract size bits from the dimensions
		mWidth = View.MeasureSpec.getSize(width);
		mHeight = View.MeasureSpec.getSize(height);

		mDetents = new float[NUM_BINS];
		mHalfBinWidth = (0.5f * mWidth) / NUM_BINS;
		for (int i = 0; i < NUM_BINS; i++) {
			mDetents[i] = (2 * i + 1) * mHalfBinWidth;
		}
		moveIndicator(mDetents[mStatusIndex]);
		setTextSizeForWidth(mTextPaint, 0.15f * mWidth, "GOOD");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// Log.i(TAG,"onDraw");
		// canvas.clipRect(getLeft(), getTop(), getRight(), getBottom());

		// draw bottom bar
		canvas.drawRect(0, 0.80f * mHeight, mWidth, mHeight, mBarPaint);
		drawGood(canvas);
		drawFair(canvas);
		drawPoor(canvas);
		drawClosed(canvas);
		drawIndicator(canvas);
	}

	private void drawGood(Canvas canvas) {
		// left, top, right, bottom
		canvas.drawRect(0, 0, mDetents[0] + mHalfBinWidth, 0.80f * mHeight,
				mGoodPaint);

		Rect bounds = new Rect();
		mTextPaint.getTextBounds("GOOD", 0, "GOOD".length(), bounds);
		canvas.drawText("GOOD", mDetents[0], 0.5f * 0.80f * mHeight
				+ (bounds.bottom - bounds.top) / 2, mTextPaint);

	}

	private void drawFair(Canvas canvas) {
		// left, top, right, bottom
		canvas.drawRect(mDetents[1] - mHalfBinWidth, 0, mDetents[1]
				+ mHalfBinWidth, 0.80f * mHeight, mFairPaint);

		Rect bounds = new Rect();
		mTextPaint.getTextBounds("FAIR", 0, "FAIR".length(), bounds);
		canvas.drawText("FAIR", mDetents[1], 0.5f * 0.80f * mHeight
				+ (bounds.bottom - bounds.top) / 2, mTextPaint);

	}

	private void drawPoor(Canvas canvas) {
		// left, top, right, bottom
		canvas.drawRect(mDetents[2] - mHalfBinWidth, 0, mDetents[2]
				+ mHalfBinWidth, 0.80f * mHeight, mPoorPaint);

		Rect bounds = new Rect();
		mTextPaint.getTextBounds("POOR", 0, "POOR".length(), bounds);
		canvas.drawText("POOR", mDetents[2], 0.5f * 0.80f * mHeight
				+ (bounds.bottom - bounds.top) / 2, mTextPaint);

	}

	private void drawClosed(Canvas canvas) {
		// left, top, right, bottom
		canvas.drawRect(mDetents[3] - mHalfBinWidth, 0, mDetents[3]
				+ mHalfBinWidth, 0.80f * mHeight, mClosedPaint);

		Rect bounds = new Rect();
		mTextPaint.getTextBounds("CLOSED", 0, "CLOSED".length(), bounds);
		canvas.drawText("CLOSED", mDetents[3], 0.5f * 0.80f * mHeight
				+ (bounds.bottom - bounds.top) / 2, mTextPaint);

	}

	private void drawIndicator(Canvas canvas) {

		path.reset();

		path.moveTo(mPointCenter.x, mPointCenter.y);
		path.lineTo(mPointLeft.x, mPointLeft.y);
		path.lineTo(mPointRight.x, mPointRight.y);
		path.close();

		canvas.drawPath(path, mIndicatorPaint);

	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			moveIndicator(event.getX());

		case MotionEvent.ACTION_MOVE:
			moveIndicator(event.getX());
			break;

		case MotionEvent.ACTION_UP:
			detentIndicator(event.getX());
			break;

		default:
			return false;
		}

		invalidate();
		return true;
	}

	private void detentIndicator(float x) {
		int detentIndex = 0;
		for (int i = 0; i < mDetents.length; i++) {
			if (x < mDetents[i] + mHalfBinWidth) {
				break;
			}
			detentIndex++;
		}

		mStatusIndex = detentIndex;
		moveIndicator(mDetents[detentIndex]);

	}

	private void moveIndicator(float x) {
		if (x < 0.0f) {
			x = 0.0f;
		}
		mPointCenter.x = (int) x;
		mPointCenter.y = (int) (0.62f * mHeight);

		mPointLeft.set(mPointCenter.x - (int) (0.04f * mWidth),
				(int) (0.80f * mHeight));

		mPointRight.set(mPointCenter.x + (int) (0.04f * mWidth),
				(int) (0.80f * mHeight));
	}

}
