package com.eureton.warmaodds.services;

import com.eureton.warmaodds.models.AttackResults;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.types.Constants;

public interface DefaultsService {
	
	int type();
	Constants.AttackModifier attackModifier();
	Constants.DamageModifier damageModifier();
	Constants.OnHitModifier onHitModifier();
	Constants.OnCritModifier onCritModifier();
	Constants.OnKillModifier onKillModifier();
	int mat();
	int def();
	int pow();
	int arm();
	int attackDice();
	int damageDice();
	int boxes();
	int focus();
	int fury();
	boolean kds();
	boolean tough();
	
	AttackStats attackStats();
	AttackResults attackResults();
	Input input();
	Output output();
}

