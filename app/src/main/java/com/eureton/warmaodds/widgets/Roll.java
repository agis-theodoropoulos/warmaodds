package com.eureton.warmaodds.widgets;

import java.util.Locale;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.util.Util;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class Roll extends GridLayout
		implements AdapterView.OnItemSelectedListener {

	private static final String TAG = Roll.class.getSimpleName();
	private static final int INVALID = -1;
	private static final int[] CAPTION_INDICES = {
	    R.styleable.Roll_fromCaption,
	    R.styleable.Roll_toCaption,
	    R.styleable.Roll_diceCaption
	};

	protected RollEventListener mListener;
	private String mKD;
	private String mND;
	private String mAUT;
	private Spinner mFrom;
	private Spinner mTo;
	private Spinner mDice;

	public Roll(Context context) {
		super(context);
		init(context);
	}
	
	public Roll(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
		processAttrs(context, attrs);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		try {
			Spinner spinner = (Spinner) parent;
			int value = stringToInt(
				spinner.getItemAtPosition(position).toString()
			);

			switch ((Integer) parent.getTag()) {
			case R.id.ROLL_SP_FROM: mListener.onFromChanged(this, value); break;
			case R.id.ROLL_SP_TO: mListener.onToChanged(this, value); break;
			case R.id.ROLL_SP_DICE: mListener.onDiceChanged(this, value); break;
			}
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) { }
	
	public void copyTo(Roll other) {
		other.mFrom.setSelection(mFrom.getSelectedItemPosition());
		other.mTo.setSelection(mTo.getSelectedItemPosition());
		other.mDice.setSelection(mDice.getSelectedItemPosition());
	}
	
	public RollEventListener setRollEventListener(RollEventListener listener) {
		RollEventListener previous = mListener;
		
		mListener = listener;
		
		return previous;
	}
	
	public int getFrom() { return getSpinnerInteger(mFrom); }
	
	public int getTo() { return getSpinnerInteger(mTo); }
	
	public int getDice() { return getSpinnerInteger(mDice); }
	
	public void setStats(int from, int to, int dice) {
		mFrom.setSelection(intToIndex(mFrom, from));
		mTo.setSelection(intToIndex(mTo, to));
		mDice.setSelection(intToIndex(mDice, dice));
	}

	protected int getLayoutId() { return R.layout.widget_roll; }

	protected void initCaptions(TypedArray attrs) {
		int[] ids = { R.id.ROLL_TV_FROM, R.id.ROLL_TV_TO,
				R.id.ROLL_TV_DICE };

		for (int i = 0; i < ids.length; ++i) {
			View view = findViewById(ids[i]);

			if (view instanceof TextView) {
				TextView textView = (TextView) view;
				int id = attrs.getResourceId(CAPTION_INDICES[i], INVALID);

				textView.setText(id);
			}
		}
	}
	
	protected void initSpinner(Context context, TypedArray attrs,
			int valuesIndex, int defaultIndex, Spinner picker) {
		int vId = attrs.getResourceId(valuesIndex, INVALID);
		int defaultVal = attrs.getInteger(defaultIndex, INVALID);
		boolean ok = false;
		
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				context, vId, android.R.layout.simple_spinner_item);
		adapter.setDropDownViewResource(
				android.R.layout.simple_spinner_dropdown_item);
		picker.setAdapter(adapter);
		
		for (int i = 0; i < adapter.getCount(); ++i) {
			int n = stringToInt(adapter.getItem(i).toString());
			
			if (n == defaultVal) {
				picker.setSelection(i);
				ok = true;
				break;
			}
		}
		if (!ok) throw new IllegalArgumentException("Invalid: " + defaultVal);
	}
	
	protected int stringToInt(String s) {
		int i = 0;

		if (mKD.equals(s)) {
			i = Util.KD;
		} else if (mAUT.equals(s)) {
			i = Util.AUT;
		} else if (mND.equals(s)) {
			i = Util.ND;
		} else {
			i = Integer.parseInt(s);
		}

		return i;
	}
	
	private void init(Context context) {
		Roll roll = (Roll) inflate(context, getLayoutId(), this);
		setRowCount(2);
		setColumnCount(3);
		setAlignmentMode(GridLayout.ALIGN_BOUNDS);
		setColumnOrderPreserved(false);
			
		mFrom = (Spinner) roll.getChildAt(3);
		mTo = (Spinner) roll.getChildAt(4);
		mDice = (Spinner) roll.getChildAt(5);
		mFrom.setTag(R.id.ROLL_SP_FROM);
		mTo.setTag(R.id.ROLL_SP_TO);
		mDice.setTag(R.id.ROLL_SP_DICE);
		mFrom.setOnItemSelectedListener(this);
		mTo.setOnItemSelectedListener(this);
		mDice.setOnItemSelectedListener(this);
			
		Resources r = context.getResources();
		mKD = r.getString(R.string.KD);
		mND = r.getString(R.string.ND);
		mAUT = r.getString(R.string.AUT);
	}

	private void processAttrs(Context context, AttributeSet attrs) {
		TypedArray array = null;

		try {
			array = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.Roll, 0, 0);
			initSpinner(context, array, R.styleable.Roll_fromValues,
					R.styleable.Roll_fromDefault, mFrom);
			initSpinner(context, array, R.styleable.Roll_toValues,
					R.styleable.Roll_toDefault, mTo);
			initSpinner(context, array, R.styleable.Roll_diceValues,
					R.styleable.Roll_diceDefault, mDice);
			initCaptions(array);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (array != null) array.recycle();
		}
	}
	
	private int getSpinnerInteger(Spinner spinner) {
		return stringToInt((String) spinner.getSelectedItem().toString());
	}
	
	private int intToIndex(Spinner spinner, int value) {
		int i = 0;

		for (i = 0; i < spinner.getCount(); ++i) {
			if (value == stringToInt((String) spinner.getItemAtPosition(i))) {
				break;
			}
		}

		return i;
	}
	
	public interface RollEventListener {
		void onFromChanged(Roll view, int newValue);
		void onToChanged(Roll view, int newValue);
		void onDiceChanged(Roll view, int newValue);
	}
}

