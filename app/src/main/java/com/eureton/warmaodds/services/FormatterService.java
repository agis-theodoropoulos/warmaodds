package com.eureton.warmaodds.services;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.types.Combination;
import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.types.Range;

public interface FormatterService {

	String boxes(int n);
	String focus(int n);
	String transfers(int n);
	String count(int n);
	String probability(float p);
	String damage(float n);
	String combination(Combination c);
	String input(Input i);
	String range(Range r);
	String attackStats(AttackStats s);
	int attackModifier(Constants.AttackModifier modifier);
	int damageModifier(Constants.DamageModifier modifier);
	int onHitModifier(Constants.OnHitModifier modifier);
	int onCritModifier(Constants.OnCritModifier modifier);
	int onKillModifier(Constants.OnKillModifier modifier);
}

