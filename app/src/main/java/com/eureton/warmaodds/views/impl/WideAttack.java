package com.eureton.warmaodds.views.impl;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.eureton.warmaodds.R;
import com.eureton.warmaodds.views.Attack;
import com.eureton.warmaodds.widgets.DamageRoll;
import com.eureton.warmaodds.widgets.HitRoll;
import com.eureton.warmaodds.widgets.Roll;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class WideAttack extends LinearLayout implements
		Attack, Roll.RollEventListener {
	
	private AttackImpl mImpl;
	private AttackEventListener mListener;
	@Bind(R.id.BT_REMOVEATTACK) ImageButton mRemoveBtn;
	
	public WideAttack(Context context) {
		super(context);
		init(context);
	}
	
	public WideAttack(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	@Override
	public void setRemovable(boolean status) {
		mRemoveBtn.setVisibility(status ? View.VISIBLE : View.INVISIBLE);
	}

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

	@OnClick(R.id.BT_REMOVEATTACK)
	protected void onRemoveClick(View v) {
		if (mListener != null) mListener.onRemoved(this);
	}
	
	private void init(Context context) {
		mImpl = new AttackImpl(context, this);

		setOrientation(LinearLayout.HORIZONTAL);
		inflate(context, R.layout.widget_attack_stats, this);
		inflate(context, R.layout.widget_attack_results, this);

		ButterKnife.bind(mImpl, this);
		ButterKnife.bind(this, this);
		mImpl.init();
	}
}
