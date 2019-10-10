package com.eureton.warmaodds.types;

public final class Constants {
	
	public enum AttackModifier {
		NONE,
		REROLL,
		DISCARD_LOWEST,
		DISCARD_ANY_1,
		DISCARD_HIGHEST,
		REROLL_1S_AND_2S,
		ADD_2_DISCARD_ANY_2
	}

	public enum DamageModifier {
		NONE,
		REROLL,
		DISCARD_LOWEST,
		DISCARD_ANY_1,
		REROLL_1S_AND_2S
	}

	public enum OnHitModifier {
		NONE,
		NO_ROLL_1_DAMAGE,
		NO_ROLL_3_DAMAGE,
		D3_PLUS_3_DAMAGE,
		MIN_1_DAMAGE,
		DOUBLE_DAMAGE,
		KNOCKDOWN
	}

	public enum OnCritModifier {
		NONE,
		EXTRA_DIE,
		HALVE_ARM,
		RFP,
		KNOCKDOWN
	}

	public enum OnKillModifier {
		NONE,
		NO_TOUGH
	}
}

