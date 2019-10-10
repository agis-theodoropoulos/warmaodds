package com.eureton.warmaodds.widgets;

import java.util.Locale;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

public class DiscreetSpinner extends Spinner implements
		View.OnTouchListener, AdapterView.OnItemSelectedListener {
	
	private static final String TAG = DiscreetSpinner.class.getSimpleName();
	private static final int INVALID = -1;

	private boolean mIsUserSelection;
	private AdapterView.OnItemSelectedListener mListener;

	public DiscreetSpinner(Context context) {
		super(context);
		init();
	}
	
	public DiscreetSpinner(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
		processAttrs(context, attrs);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mIsUserSelection = true;
		log("Received user input");
		
		return false;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		if (mIsUserSelection) {
			log("User-generated selection", position);
			if (mListener != null) {
				mListener.onItemSelected(parent, view, position, id);
			}

			mIsUserSelection = false;
		} else {
			log("System-generated selection", position);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) { }
	
	@Override
	public void setOnItemSelectedListener(
			AdapterView.OnItemSelectedListener listener) {
		mListener = listener;
	}
	
	private void init() {
		super.setOnItemSelectedListener(this);
		setOnTouchListener(this);
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray arr = null;

		try {
			arr = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.DiscreetSpinner, 0, 0);

			init(arr, R.styleable.DiscreetSpinner_values,
					R.styleable.DiscreetSpinner_defaultValue);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (arr != null) arr.recycle();
		}
	}
	
	private void init(TypedArray attrs, int valuesIndex, int defaultIndex) {
		int v = attrs.getResourceId(valuesIndex, INVALID);
		
		if (v != INVALID) {
			String d = attrs.getString(defaultIndex);
			boolean ok = false;

			ArrayAdapter<CharSequence> a = ArrayAdapter.createFromResource(
					getContext(), v, android.R.layout.simple_spinner_item);
			a.setDropDownViewResource(
					android.R.layout.simple_spinner_dropdown_item);
			setAdapter(a);
		
			if (d != null) {
				for (int i = 0; i < a.getCount(); ++i) {
					String s = a.getItem(i).toString();

					if (s.equals(d)) {
						setSelection(i);
						ok = true;
						break;
					}
				}
				if (!ok) throw new IllegalArgumentException("Invalid: " + d);
			}
		}
	}

	private void log(String msg) {
		if (BuildConfig.DEBUG) Log.d(
			TAG,
			String.format(Locale.US, "[%d]: %s", (Integer) getTag(), msg)
		);
	}

	private void log(String msg, int position) {
		if (BuildConfig.DEBUG) Log.d(TAG, String.format(
			Locale.US,
			"[%d]: %s: [%d] -> %s",
			(Integer) getTag(),
			msg,
			position,
			getItemAtPosition(position)
		));
	}
}

