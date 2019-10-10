package com.eureton.warmaodds.widgets;

import java.lang.reflect.Field;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class NumberPicker extends android.widget.NumberPicker {
	private static final String TAG = NumberPicker.class.getSimpleName();
	private static int INVALID = -1;

	public NumberPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		processAttributeSet(context, attrs);
	}

	public NumberPicker(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		processAttributeSet(context, attrs);
	}

	private void processAttributeSet(Context context, AttributeSet attrs) {
		TypedArray array = null;

		try {
			array = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.NumberPicker, 0, 0);

			setValues(array);
			setStyles(array);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (array != null) array.recycle();
		}
    }
	
	private void setValues(TypedArray array) {
		int min = array.getInteger(R.styleable.NumberPicker_minValue, INVALID);
		int max = array.getInteger(R.styleable.NumberPicker_maxValue, INVALID);
		int val = array.getInteger(R.styleable.NumberPicker_value, INVALID);
		
		setMinValue(min);
		setMaxValue(max);
		setValue(val);
	}
	
	private void setStyles(TypedArray array) {
		int textColor = array.getColor(R.styleable.NumberPicker_textColor,
				INVALID);
		int highlightColor = array.getColor(
				R.styleable.NumberPicker_textHighlightColor, INVALID);
		int dividerColor = array.getColor(R.styleable.NumberPicker_dividerColor,
				INVALID);

		EditText editText = getEditText();
		try {
			Field field = getField("mSelectorWheelPaint");
			((Paint) field.get(this)).setColor(textColor);
			editText.setTextColor(textColor);
			editText.setHighlightColor(highlightColor);

			field = getField("mSelectionDivider");
			ColorDrawable colorDrawable = new ColorDrawable(dividerColor);
			field.set(this, colorDrawable);

			invalidate();
		} catch (Exception e) { }
	}
	
	private Field getField(String name) throws NoSuchFieldException {
		Field field = android.widget.NumberPicker.class.getDeclaredField(name);

		field.setAccessible(true);
			
		return field;
	}
	
	private EditText getEditText() {
		EditText editText = null;
		
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);

			if (child instanceof EditText) {
				editText = (EditText) child;
				break;
			}
		}
		
		return editText;
	}
}
