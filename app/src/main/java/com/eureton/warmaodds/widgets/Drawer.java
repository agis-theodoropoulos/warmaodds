package com.eureton.warmaodds.widgets;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

public class Drawer extends FrameLayout implements View.OnTouchListener {

	private static final String TAG = Drawer.class.getSimpleName();
	private static final int INVALID = -1;
	private static final float LOCK_THRESHOLD = 5.f;
	private static final long ANIM_DURATION = 400L;

	protected float mLeftTopTouchX, mLeftDownX, mRightTopTouchX, mRightDownX;
	private View mTop, mBottom;
	private int mTopLayoutId, mBottomLayoutId, mLeftHandleId, mRightHandleId;
	private boolean mOpen;
	private boolean mHasDetectedTap;
	private boolean mLeftDown, mRightDown;
	private boolean mLeftDragging, mRightDragging;
	private GestureDetector mGestureDetector;
	private int[] mCoordinates;

	public Drawer(Context context) {
		super(context);

		initialize();
	}

	public Drawer(Context context, AttributeSet attrs) {
		super(context, attrs);

		initialize();
		processAttrs(context, attrs);
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		int id = view.getId();
		boolean res = false;

		if (id == mLeftHandleId) {
			res = onTouchLeftHandle(event);
		} else if (id == mRightHandleId) {
			res = onTouchRightHandle(event);
		}

		return res;
	}

	/*
	 *	We don't want the spinners receiving touch events while the drawer
	 *	is open.
	 */
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) { return mOpen; }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mOpen) {
			mTop.getLocationOnScreen(mCoordinates);
			int l = mCoordinates[0];
			int t = mCoordinates[1];
			int w = mTop.getWidth();
			int h = mTop.getHeight();
			float x = event.getRawX();
			float y = event.getRawY();

			if (BuildConfig.DEBUG) Log.v(TAG, String.format(
				"TL: (%d, %d), BR: (%d, %d), Event: (%f, %f)",
				l, t, t + h, l + w, x, y
			));

			if (l <= x && x <= l + w && t <= y && y <= t + h) {
				mHasDetectedTap = mGestureDetector.onTouchEvent(event);
				onTouchRightHandle(event);
				mHasDetectedTap = false;
			}
		}

		return mOpen;
	}

	public View getTopView() { return mTop; }

	public View getBottomView() { return mBottom; }

	public boolean isOpen() { return mOpen; }

	public void open(boolean animate) {
		if (BuildConfig.DEBUG) Log.d(TAG, "open()");

		animateTop(-mBottom.getWidth(), animate ? ANIM_DURATION : 0);
		mOpen = true;
	}

	public void close(boolean animate) {
		if (BuildConfig.DEBUG) Log.d(TAG, "close()");

		animateTop(0.f, animate ? ANIM_DURATION : 0);
		mOpen = false;
	}

	protected View addTopView() { return addLayer(mTopLayoutId); }

	protected View addBottomView() { return addLayer(mBottomLayoutId); }

	protected View addLayer(int layoutId) {
		return (layoutId != INVALID) ?
			inflate(getContext(), layoutId, this) :
			null;
	}

	protected void startListening() {
		View lh = findViewById(mLeftHandleId);
		View rh = findViewById(mRightHandleId);
		
		if (lh != null) lh.setOnTouchListener(this);
		if (rh != null) rh.setOnTouchListener(this);
		
		mTop = getChildAt(1);
		mBottom = getChildAt(0);
		mGestureDetector = new GestureDetector(
			getContext(),
			new GestureDetector.SimpleOnGestureListener() {

				// need this so that GestureDetector#onTouchEvent shall return
				// true on ACTION_UP after a tap
				@Override
				public boolean onSingleTapUp(MotionEvent e) { return true; }

				@Override
				public boolean onSingleTapConfirmed(MotionEvent e) {
					if (BuildConfig.DEBUG) Log.d(TAG, "onSingleTapConfirmed()");
					close(true);

					return true;
				}
			}
		);
	}

	private boolean onTouchLeftHandle(MotionEvent event) {
		int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			onLeftHandleGrab(event);
			break;
		case MotionEvent.ACTION_UP:
			onLeftHandleRelease(event);
			break;
		case MotionEvent.ACTION_MOVE:
			onLeftHandleDrag(event);
			break;
		}

		return true;
	}

	private boolean onTouchRightHandle(MotionEvent event) {
		int action = event.getAction();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			onRightHandleGrab(event);
			break;
		case MotionEvent.ACTION_UP:
			onRightHandleRelease(event);
			break;
		case MotionEvent.ACTION_MOVE:
			onRightHandleDrag(event);
			break;
		}

		return true;
	}

	private void onRightHandleGrab(MotionEvent e) {
		if (BuildConfig.DEBUG && mRightDown) Log.w(TAG, "Already DOWN");
		if (!mRightDragging) {
			mRightTopTouchX = mTop.getX() - e.getRawX();
			mRightDownX = e.getRawX();
		}
		mRightDown = true;
		disableVerticalScrolling();
	}

	private void onRightHandleRelease(MotionEvent e) {
		if (BuildConfig.DEBUG && !mRightDown) Log.w(TAG, "Already UP");
		mRightDown = false;
		mRightDragging = false;

		float dx = mRightDownX - e.getRawX();
		float delta = mBottom.getWidth() - dx;
		if (BuildConfig.DEBUG) Log.v(TAG, "dx: " + dx + ", delta: " + delta);
		if (delta < LOCK_THRESHOLD) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Locked");
			open(true);
		} else {
			if (BuildConfig.DEBUG) Log.d(TAG, "Slide back");
			if (!mHasDetectedTap) close(true);
		}
	}

	private void onRightHandleDrag(MotionEvent e) {
		if (mRightDown) {
			mRightDragging = true;
			float x = e.getRawX() + mRightTopTouchX;

			animateTop(
				Math.min(0, Math.max(-mBottom.getWidth(), x)),
				0
			);
		}
	}

	protected void onLeftHandleGrab(MotionEvent e) {
		if (BuildConfig.DEBUG && mLeftDown) Log.w(TAG, "Already DOWN");
		if (!mRightDragging) {
			mLeftTopTouchX = mTop.getX() - e.getRawX();
			mLeftDownX = e.getRawX();
		}
		mLeftDown = true;
		disableVerticalScrolling();
	}

	protected void onLeftHandleRelease(MotionEvent e) {
		if (BuildConfig.DEBUG && !mLeftDown) Log.w(TAG, "Already UP");
		mLeftDown = false;
		mLeftDragging = false;

		if (BuildConfig.DEBUG) {
			Log.d(TAG, "dX: " + Math.abs(mLeftDownX - e.getRawX()));
		}
		animateTop(0.f, ANIM_DURATION);
	}

	protected void onLeftHandleDrag(MotionEvent e) {
		if (mLeftDown) {
			mLeftDragging = true;
			animateTop(e.getRawX() + mLeftTopTouchX, 0);
		}
	}

	protected void onLeftHandleResistedDrag(MotionEvent e) {
		if (mLeftDown) {
			float x = e.getRawX() + mLeftTopTouchX;
			float maxX = getWidth() / 2.f;

			mLeftDragging = true;
			animateTop(
				Math.min(maxX, x),
				0
			);
		}
	}

	private void animateTop(float x, long duration) {
		mTop.animate().
				x(x).
				setDuration(duration).
				start();
	}

	private void initialize() {
		mCoordinates = new int[2];
	}

	private void disableVerticalScrolling() {
		ViewParent p = getParent();
		if (p != null) p.requestDisallowInterceptTouchEvent(true);
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray array = null;

		try {
			array = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.Drawer, 0, 0);
			mTopLayoutId = array.getResourceId(R.styleable.Drawer_topLayout,
					INVALID);
			mBottomLayoutId = array.getResourceId(
					R.styleable.Drawer_bottomLayout, INVALID);
			mLeftHandleId = array.getResourceId(R.styleable.Drawer_leftHandle,
					INVALID);
			mRightHandleId = array.getResourceId(R.styleable.Drawer_rightHandle,
					INVALID);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (array != null) array.recycle();
		}
	}

	public interface DrawerListener {
		void onOpen(Drawer view);
		void onClose(Drawer view);
	}
}

