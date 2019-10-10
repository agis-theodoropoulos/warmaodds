package com.eureton.warmaodds.widgets;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public abstract class SwipeClosableDrawer extends Drawer {

	private static final String TAG = SwipeClosableDrawer.class.getSimpleName();
	private static final int INVALID = -1;

	private View mIcon, mCloseView;
	private int mCloseLayoutId, mCloseIconId;
	private int mLevel;
	private boolean mSwipeCloseEnabled;

	public SwipeClosableDrawer(Context context) {
		super(context);
		init(context);
	}

	public SwipeClosableDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		processAttrs(context, attrs);
	}

	@Override
	protected void onLeftHandleGrab(MotionEvent e) {
		if (mSwipeCloseEnabled) {
			super.onLeftHandleGrab(e);

			if (BuildConfig.DEBUG) Log.d(TAG, "onLeftHandleGrab: " + e.getX());
			mIcon.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onLeftHandleRelease(MotionEvent e) {
		if (mSwipeCloseEnabled) {
			mIcon.setVisibility(View.INVISIBLE);
			if (BuildConfig.DEBUG) Log.d(TAG, "onLeftHandRelease: " + mLevel);
			if (mLevel >= 17) {
				onSwipeClose();
				mLevel = 0;
			} else {
				super.onLeftHandleRelease(e);
			}
		}
	}

	@Override
	protected void onLeftHandleDrag(MotionEvent event) {
		if (mSwipeCloseEnabled) {
			float x = event.getRawX();
			int iconWidth = mIcon.getWidth();
			float maxIconX = iconWidth * 1.1f;
			float tail = iconWidth * 2.f;
			float iconX = Math.min(mLeftTopTouchX + x - tail, maxIconX);
			float fullX = getWidth() / 3.f;

			mLevel = (int) Math.max((x / fullX) * 17, 0);
			if (BuildConfig.DEBUG) Log.v(TAG, "onLeftHandleDrag: " + mLevel);
			if (mLevel < 17) {
				super.onLeftHandleDrag(event);
			} else {
				super.onLeftHandleResistedDrag(event);
			}

			mIcon.setX(iconX);
			mIcon.getBackground().setLevel(mLevel);
		}
	}

	protected View addCloseView() {
		mCloseView = addLayer(mCloseLayoutId);

		if (mCloseIconId != INVALID) {
			mIcon = findViewById(mCloseIconId);
			setClipChildren(false);
		}

		return mCloseView;
	}

	protected View removeCloseView() {
		View v = mCloseView;

		if (mCloseView != null) {
			removeView(mCloseView);
			mCloseView = null;
		}

		return v;
	}

	protected void setSwipeCloseEnabled(boolean enabled) {
		mSwipeCloseEnabled = enabled;
	}

	protected abstract void onSwipeClose();

	private void init(Context context) {
		mSwipeCloseEnabled = true;
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray array = null;

		try {
			array = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.SwipeClosableDrawer, 0, 0);
			mCloseLayoutId = array.getResourceId(
				R.styleable.SwipeClosableDrawer_closeLayout,
				INVALID
			);
			mCloseIconId = array.getResourceId(
				R.styleable.SwipeClosableDrawer_closeIcon,
				INVALID
			);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (array != null) array.recycle();
		}
	}
}

