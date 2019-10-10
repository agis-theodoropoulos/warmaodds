package com.eureton.warmaodds.widgets;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class PagerIndicator extends LinearLayout {

	private static final String TAG = PagerIndicator.class.getSimpleName();
	private static final int INVALID = -1;

	private ArrayList<ImageView> mDots;
	private int mDrawableId;
	private int mPosition;
	private int mSelectedColor, mUnselectedColor;

	public PagerIndicator(Context context) {
		super(context);

		initialize();
	}

	public PagerIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);

		initialize();
		processAttrs(context, attrs);
	}

	public void setSize(int size) {
		removeAllViews();
		mDots.clear();

		Context c = getContext();
		for (int i = 0; i < size; ++i) {
			ImageView v = new ImageView(c);
			mDots.add(v);

			Drawable d = c.getResources().getDrawable(mDrawableId);
			v.setImageDrawable(d);

			LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			);
			addView(v, p);
			setPosition(0);
		}
	}

	public void setPosition(int index) {
		mPosition = index;
		int size = mDots.size();

		if (index >= 0 && index < size) {
			for (int i = 0; i < size; i++) {
				setColor(mDots.get(i), mUnselectedColor);
			}
			setColor(mDots.get(index), mSelectedColor);
		}
	}

	private void initialize() {
		mDots = new ArrayList<ImageView>();
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray a = null;
		Resources r = context.getResources();

		try {
			a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.PagerIndicator, 0, 0);
			mDrawableId = a.getResourceId(
				R.styleable.PagerIndicator_dotDrawable,
				INVALID
			);
			int selId = a.getResourceId(
				R.styleable.PagerIndicator_selectedColor,
				INVALID
			);
			mSelectedColor = r.getColor(selId);
			int unselId = a.getResourceId(
				R.styleable.PagerIndicator_unselectedColor,
				INVALID
			);
			mUnselectedColor = r.getColor(unselId);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (a != null) a.recycle();
		}
	}

	private static void setColor(ImageView view, int color) {
		view.
				getDrawable().
				mutate().
				setColorFilter(color, PorterDuff.Mode.SRC_IN);
	}
}

