package com.eureton.warmaodds.views.impl;

import butterknife.Bind;

import java.util.Locale;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.util.Util;
import com.eureton.warmaodds.views.Attack;
import com.eureton.warmaodds.widgets.DamageRoll;
import com.eureton.warmaodds.widgets.HitRoll;
import com.eureton.warmaodds.widgets.Roll;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

class AttackImpl implements Attack, HitRoll.HitRollEventListener {

	private static final String TAG = AttackImpl.class.getSimpleName();

	private final Context mContext;
	private final Attack mFacade;
	private AttackEventListener mListener;
	@Bind(R.id.ROLL_ATTACK) HitRoll mAttack;
	@Bind(R.id.ROLL_DAMAGE) DamageRoll mDamage;
	@Bind(R.id.ATTACK_TV_HIT) TextView mProbHit;
	@Bind(R.id.ATTACK_TV_CRITICAL) TextView mProbCrit;
	@Bind(R.id.ATTACK_TV_DAMAGE) TextView mAvgDmg;
	@Bind(R.id.ATTACKSTATS_MODIFIED_BADGE) TextView mModifiedBadge;
	
	public AttackImpl(Context context, Attack facade) {
		mContext = context;
		mFacade = facade;
	}

	@Override
	public void setRemovable(boolean status) { }
	
	@Override
	public void setHit(String s) {
		final String finalText = s;
		
		runViewOperation(new Runnable() {
			
			@Override
			public void run() { mProbHit.setText(finalText); }
		});
	}

	@Override
	public void setCritical(String s) {
		final String finalText = s;
		
		runViewOperation(new Runnable() {
			
			@Override
			public void run() { mProbCrit.setText(finalText); }
		});
	}

	@Override
	public void setDamage(String s) {
		final String finalText = s;
		
		runViewOperation(new Runnable() {
			
			@Override
			public void run() { mAvgDmg.setText(finalText); }
		});
	}

	@Override
	public void onFromChanged(Roll view, int newValue) {
		if (view == mAttack) {
			if (emit()) mListener.onMatChanged(mFacade, newValue);
		} else if (view == mDamage) {
			if (emit()) mListener.onPowChanged(mFacade, newValue);
		}
	}

	@Override
	public void onToChanged(Roll view, int newValue) {
		if (view == mAttack) {
			if (emit()) mListener.onDefChanged(mFacade, newValue);
		} else if (view == mDamage) {
			if (emit()) mListener.onArmChanged(mFacade, newValue);
		}
	}

	@Override
	public void onDiceChanged(Roll view, int newValue) {
		if (view == mAttack) {
			if (emit()) mListener.onAttackDiceChanged(mFacade, newValue);
		} else if (view == mDamage) {
			if (emit()) mListener.onDamageDiceChanged(mFacade, newValue);
		}
	}

	@Override
	public void onTypeChanged(Roll view, int newValue) {
		if (emit()) mListener.onTypeChanged(mFacade, newValue);
		setDamageFromCaption(newValue);
	}
	
	@Override
	public AttackEventListener setAttackEventListener(
			AttackEventListener listener) {
		AttackEventListener previous = mListener;

		mListener = listener;
		
		return previous;
	}
	
	@Override
	public void copyTo(Attack other) {
		mAttack.copyTo(other.getAttack());
		mDamage.copyTo(other.getDamage());
	}

	@Override
	public HitRoll getAttack() { return mAttack; }

	@Override
	public DamageRoll getDamage() { return mDamage; }

	@Override
	public void setStats(int type, int mat, int def, int attackDice, int pow,
			int arm, int damageDice) {
		if (BuildConfig.DEBUG) Log.d(
			TAG,
			String.format(
				"setStats(T: %d, M: %d, D: %d, AD: %d, P: %d, A: %d, DD: %d)",
				type, mat, def, attackDice, pow, arm, damageDice
			)
		);

		mAttack.setStats(type, mat, def, attackDice);
		mDamage.setStats(pow, arm, damageDice);
		setDamageFromCaption(type);
	}

	@Override
	public void setModified(boolean modified) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setModified(" + modified + ")");

		mModifiedBadge.setEnabled(modified);
	}

	public void init() {
		mAttack.setRollEventListener(this);
		mDamage.setRollEventListener(this);
	}

	private boolean emit() { return mListener != null; }

	private void setDamageFromCaption(int type) {
		switch (type) {
		case AttackStats.MAT:
			mDamage.setPns();
			break;
		case AttackStats.RAT:
		case AttackStats.MGC:
			mDamage.setPow();
			break;
		}
	}

	private void runViewOperation(Runnable r) {
		if (Util.isUiThread()) {
			r.run();
		} else {
			Context context = mContext;
			
			if (context instanceof Activity) {
				((Activity) context).runOnUiThread(r);
			}
		}
	}
}

