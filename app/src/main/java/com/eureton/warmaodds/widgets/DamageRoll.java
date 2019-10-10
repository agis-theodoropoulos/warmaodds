package com.eureton.warmaodds.widgets;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class DamageRoll extends Roll {
	
	private static final String TAG = DamageRoll.class.getSimpleName();

	private TextView mToCaption;

	public DamageRoll(Context context) {
		super(context);
	}
	
	public DamageRoll(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public void copyTo(Roll other) {
		if (other instanceof DamageRoll) {
			DamageRoll dr = (DamageRoll) other;

			dr.mToCaption.setText(mToCaption.getText());
		}

		super.copyTo(other);
	}

	public void setPns() { mToCaption.setText(R.string.attacks_pns); }

	public void setPow() { mToCaption.setText(R.string.attacks_pow); }

	@Override
	protected void initCaptions(TypedArray attrs) {
		super.initCaptions(attrs);

		mToCaption = (TextView) findViewById(R.id.ROLL_TV_FROM);
	}
}

