package com.eureton.warmaodds.widgets;

import java.util.Locale;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class IconStat extends RelativeLayout {

	private static final String TAG = IconStat.class.getSimpleName();

	private TextView mTextView;

	public IconStat(Context context) {
		super(context);
		init(context);
	}

	public IconStat(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		processAttrs(context, attrs);
	}

	public void setText(String text) { mTextView.setText(text); }

	private void init(Context context) {
		IconStat v =
				(IconStat) inflate(context, R.layout.widget_icon_stat, this);
		mTextView = (TextView) v.findViewById(R.id.action_item_textview);
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray array = null;

		try {
			array = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.IconStat, 0, 0);
			Drawable d = array.getDrawable(R.styleable.IconStat_icon);

			ImageView iv = (ImageView) findViewById(R.id.action_item_image);
			iv.setImageDrawable(d);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (array != null) array.recycle();
		}
	}
}

