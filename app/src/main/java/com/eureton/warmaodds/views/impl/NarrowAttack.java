package com.eureton.warmaodds.views.impl;

import butterknife.ButterKnife;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.views.Attack;
import com.eureton.warmaodds.widgets.SwipeClosableDrawer;
import com.eureton.warmaodds.widgets.DamageRoll;
import com.eureton.warmaodds.widgets.HitRoll;
import com.eureton.warmaodds.widgets.Roll;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class NarrowAttack extends SwipeClosableDrawer
		implements Attack, Roll.RollEventListener {
	
	private static final String TAG = NarrowAttack.class.getSimpleName();

	private AttackImpl mImpl;
	private AttackEventListener mListener;
	
	public NarrowAttack(Context context) {
		super(context);
		init(context);
	}
	
	public NarrowAttack(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	public void setRemovable(boolean status) { setSwipeCloseEnabled(status); }

	@Override
	public void setHit(String s) { mImpl.setHit(s); }

	@Override
	public void setCritical(String s) { mImpl.setCritical(s); }

	@Override
	public void setDamage(String s) { mImpl.setDamage(s); }

	@Override
	public void onFromChanged(Roll view, int newValue) {
		mImpl.onFromChanged(view, newValue);
	}

	@Override
	public void onToChanged(Roll view, int newValue) {
		mImpl.onToChanged(view, newValue);
	}

	@Override
	public void onDiceChanged(Roll view, int newValue) {
		mImpl.onDiceChanged(view, newValue);
	}
	
	@Override
	public AttackEventListener setAttackEventListener(
			AttackEventListener listener) {
		mListener = listener;

		return mImpl.setAttackEventListener(listener);
	}
	
	@Override
	public void copyTo(Attack other) { mImpl.copyTo(other); }
	
	@Override
	public HitRoll getAttack() { return mImpl.getAttack(); }
	
	@Override
	public DamageRoll getDamage() { return mImpl.getDamage(); }

	@Override
	public void setStats(int type, int mat, int def, int attackDice, int pow,
			int arm, int damageDice) {
		mImpl.setStats(type, mat, def, attackDice, pow, arm, damageDice);
	}

	@Override
	public void setModified(boolean modified) { mImpl.setModified(modified); }

	@Override
	protected void onSwipeClose() {
		if (BuildConfig.DEBUG) Log.i(TAG, "onSwipeClose");
		if (mListener != null) mListener.onRemoved(this);
	}

	private void init(Context context) {
		mImpl = new AttackImpl(context, this);

		addBottomView();
		addTopView();
		addCloseView();
		
		startListening();
		ButterKnife.bind(mImpl, this);
		mImpl.init();
	}
}

