package com.eureton.warmaodds.widgets;

import java.util.Locale;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.models.AttackStats;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

public class HitRoll extends Roll {
	
	private static final String TAG = HitRoll.class.getSimpleName();

	private Spinner mType;

	public HitRoll(Context context) {
		super(context);
	}
	
	public HitRoll(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Spinner spinner = (Spinner) parent;
		int value = stringToInt(
			spinner.getItemAtPosition(position).toString()
		);

		if (spinner == mType) {
			HitRollEventListener l = (HitRollEventListener) mListener;

			l.onTypeChanged(this, value);
		} else {
			super.onItemSelected(parent, view, position, id);
		}
	}

	@Override
	public void copyTo(Roll other) {
		if (other instanceof HitRoll) {
			HitRoll hr = (HitRoll) other;

			hr.mType.setSelection(mType.getSelectedItemPosition());
		}

		super.copyTo(other);
	}
	
	public void setType(int type) {
		mType.setSelection(intToIndex(type));
	}
	
	public int getType() {
		String s = (String) mType.getSelectedItem().toString();

		return stringToInt(s);
	}
	
	public void setStats(int type, int from, int to, int dice) {
		mType.setSelection(intToIndex(type));
		
		setStats(from, to, dice);
	}

	@Override
	protected int getLayoutId() { return R.layout.widget_hit_roll; }

	@Override
	protected void initCaptions(TypedArray attrs) {
		super.initCaptions(attrs);

		mType = (Spinner) findViewById(R.id.ROLL_SP_TYPE);
		initSpinner(
			getContext(),
			attrs,
			R.styleable.Roll_typeValues,
			R.styleable.Roll_typeDefault,
			mType
		);
		mType.setOnItemSelectedListener(this);
	}
	
	@Override
	protected int stringToInt(String s) {
		int v = -1;

		if (s.equals("MAT")) {
			v = AttackStats.MAT;
		} else if (s.equals("RAT")) {
			v = AttackStats.RAT;
		} else if (s.equals("MGC")) {
			v = AttackStats.MGC;
		} else {
			v = super.stringToInt(s);
		}

		return v;
	}
	
	private int intToIndex(int value) {
		int i = 0;

		switch (value) {
		case AttackStats.MAT: i = 0; break;
		case AttackStats.RAT: i = 1; break;
		case AttackStats.MGC: i = 2; break;
		}

		return i;
	}
	
	public interface HitRollEventListener extends RollEventListener {
		void onTypeChanged(Roll view, int newValue);
	}
}

