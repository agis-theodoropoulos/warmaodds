package com.eureton.warmaodds.widgets;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Checkable;
import android.widget.ImageView;

public class IconToggle extends ImageView implements Checkable {

	private static final String TAG = IconToggle.class.getSimpleName();
	private static final int[] STATE_SET = { android.R.attr.state_checked };


	private boolean mChecked;

	public IconToggle(Context context) {
		super(context);
	}

	public IconToggle(Context context, AttributeSet attrs) {
		super(context, attrs);
		processAttrs(context, attrs);
	}

	@Override
	public boolean isChecked() { return mChecked; }

	@Override
	public void setChecked(boolean checked) {
		if (mChecked != checked) {
			mChecked = checked;
			refreshDrawableState();
		}
	}

	@Override
	public void toggle() { setChecked(!mChecked); }

	@Override
	public int[] onCreateDrawableState(int extraSpace) {
		final int[] ds = super.onCreateDrawableState(extraSpace + 1);

		if (isChecked()) mergeDrawableStates(ds, STATE_SET);

		return ds;
	}

	@Override
	public boolean performClick() {
		toggle();

		return super.performClick();
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray array = null;

		try {
			array = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.IconStat, 0, 0);
			Drawable d = array.getDrawable(R.styleable.IconStat_icon);

			setImageDrawable(d);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (array != null) array.recycle();
		}
	}
}

