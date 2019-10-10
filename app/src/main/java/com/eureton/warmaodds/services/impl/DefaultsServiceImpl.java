package com.eureton.warmaodds.services.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.eureton.warmaodds.R;
import com.eureton.warmaodds.models.AttackResults;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.services.DefaultsService;
import com.eureton.warmaodds.types.Constants;

import android.content.Context;
import android.content.res.Resources;

public class DefaultsServiceImpl implements DefaultsService {
	
	private final Resources mResources;
	
	@Inject
	public DefaultsServiceImpl(Context context) {
		mResources = context.getResources();
	}

	@Override
	public int type() { return mResources.getInteger(R.integer.default_type); }

	@Override
	public Constants.AttackModifier attackModifier() {
		return Constants.AttackModifier.valueOf(
			mResources.getString(R.string.default_attack_modifier)
		);
	}

	@Override
	public Constants.DamageModifier damageModifier() {
		return Constants.DamageModifier.valueOf(
			mResources.getString(R.string.default_damage_modifier)
		);
	}

	@Override
	public Constants.OnHitModifier onHitModifier() {
		return Constants.OnHitModifier.valueOf(
			mResources.getString(R.string.default_onhit_modifier)
		);
	}

	@Override
	public Constants.OnCritModifier onCritModifier() {
		return Constants.OnCritModifier.valueOf(
			mResources.getString(R.string.default_oncrit_modifier)
		);
	}

	@Override
	public Constants.OnKillModifier onKillModifier() {
		return Constants.OnKillModifier.valueOf(
			mResources.getString(R.string.default_onkill_modifier)
		);
	}

	@Override
	public int mat() { return mResources.getInteger(R.integer.default_mat); }

	@Override
	public int def() { return mResources.getInteger(R.integer.default_def); }

	@Override
	public int pow() { return mResources.getInteger(R.integer.default_pns); }

	@Override
	public int arm() { return mResources.getInteger(R.integer.default_arm); }

	@Override
	public int attackDice() {
		return mResources.getInteger(R.integer.default_attackdice);
	}

	@Override
	public int damageDice() {
		return mResources.getInteger(R.integer.default_damagedice);
	}

	@Override
	public int boxes() {
		return mResources.getInteger(R.integer.default_boxes);
	}

	@Override
	public int focus() {
		return mResources.getInteger(R.integer.default_focus);
	}

	@Override
	public int fury() {
		return mResources.getInteger(R.integer.default_fury);
	}

	@Override
	public boolean kds() {
		return mResources.getBoolean(R.bool.default_kds);
	}

	@Override
	public boolean tough() {
		return mResources.getBoolean(R.bool.default_tough);
	}

	@Override
	public AttackStats attackStats() {
		return new AttackStats(
			type(),
			mat(),
			def(),
			pow(),
			arm(),
			attackDice(),
			damageDice(),
			attackModifier(),
			damageModifier(),
			onHitModifier(),
			onCritModifier(),
			onKillModifier()
		);
	}

	@Override
	public AttackResults attackResults() { return new AttackResults(); }

	@Override
	public Input input() {
		return new Input(
			new AttackStats[0],
			boxes(),
			focus(),
			fury(),
			kds(),
			tough()
		);
	}

	@Override
	public Output output() {
		List<AttackResults> results = new ArrayList<AttackResults>(1);

		return new Output(
			results,
			0.f,
			0.f,
			0.f,
			0.f,
			0.f
		);
	}
}

