package com.eureton.warmaodds.widgets;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SlidingPanelLayout extends SlidingUpPanelLayout
		implements SlidingUpPanelLayout.PanelSlideListener {

	private static final String TAG = SlidingPanelLayout.class.getSimpleName();
	private static final int NONE = -1;

	private GestureDetector mGestureDetector;
	private boolean mIsScreening;
	private int mTopId, mBottomId;

	public SlidingPanelLayout(Context context) {
		super(context);

		initialize(context);
	}

	public SlidingPanelLayout(Context context, AttributeSet attrs) {
		super(context, attrs);

		initialize(context);
		processAttributes(context, attrs);

		if (mTopId != NONE && mBottomId != NONE) {
			throw new IllegalArgumentException(
				"showTillTopOf and showTillBottomOf cannot both be set"
			);
		}
	}

	/*
	 *	We don't want the spinners receiving touch events while the panel
	 *	is slid up.
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return !mIsScreening ? super.onInterceptTouchEvent(event) : true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mIsScreening) mGestureDetector.onTouchEvent(event);

		return !mIsScreening ? super.onTouchEvent(event) : true;
	}

	@Override
	public void onPanelSlide(View panel, float slideOffset) { }

	@Override
	public void onPanelStateChanged(View panel,
			SlidingUpPanelLayout.PanelState previousState,
			SlidingUpPanelLayout.PanelState newState) {
		switch (newState) {
		case EXPANDED: mIsScreening = true; break;
		case COLLAPSED: mIsScreening = false; break;
		}
	}

	@Override
	public void setPanelHeight(int height) {
		super.setPanelHeight(height);

		if (BuildConfig.DEBUG) Log.d(TAG, "Panel height -> " + height);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		super.onLayout(changed, left, top, right, bottom);

		if (mTopId != NONE) {
			View v = findViewById(mTopId);

			if (v != null) {
				setPanelHeight(v.getTop());
			} else if (BuildConfig.DEBUG) {
				Log.w(TAG, "Could not find sliding panel height anchor");
			}
		} else if (mBottomId != NONE) {
			View v = findViewById(mBottomId);

			if (v != null) {
				setPanelHeight(v.getBottom());
			} else if (BuildConfig.DEBUG) {
				Log.w(TAG, "Could not find sliding panel height anchor");
			}
		}
	}

	private void initialize(Context context) {
		mGestureDetector = new GestureDetector(
			context,
			new GestureDetector.SimpleOnGestureListener() {

				@Override
				public boolean onSingleTapConfirmed(MotionEvent event) {
					if (BuildConfig.DEBUG) Log.d(TAG, "onSingleTapConfirmed");
					setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

					return true;
				}
			}
		);
		addPanelSlideListener(this);
	}

	private void processAttributes(Context context, AttributeSet attrs) {
		TypedArray a = null;

		try {
			a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.SlidingPanelLayout, 0, 0);

			mTopId = a.getResourceId(
				R.styleable.SlidingPanelLayout_showTillTopOf,
				NONE
			);
			mBottomId = a.getResourceId(
				R.styleable.SlidingPanelLayout_showTillBottomOf,
				NONE
			);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (a != null) a.recycle();
		}
	}
}

