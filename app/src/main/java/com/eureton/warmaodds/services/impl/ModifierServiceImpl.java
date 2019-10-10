package com.eureton.warmaodds.services.impl;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.DiceService;
import com.eureton.warmaodds.services.MathService;
import com.eureton.warmaodds.services.ModifierService;
import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.util.Util;

public class ModifierServiceImpl implements ModifierService {

	private static final String TAG = ModifierServiceImpl.class.getSimpleName();

	private final DiceService mDiceService;
	private final MathService mMathService;

	@Inject
	public ModifierServiceImpl(
		DiceService diceService,
		MathService mathService
	) {
		mDiceService = diceService;
		mMathService = mathService;
	}

	@Override
	public void decorate(Input input, AttackStats attackStats) {
		defaultProbabilities(input, attackStats);
		defaultRolls(attackStats);
		if (attackStats.isDamageDealing()) defaultKillRolls(input, attackStats);
		else impossibleKillRolls(input, attackStats);

		switch (attackStats.onHitModifier) {
		case DOUBLE_DAMAGE: onHitDoubleDamage(input, attackStats); break;
		case MIN_1_DAMAGE: onHitMinOneDamage(input, attackStats); break;
		case NO_ROLL_1_DAMAGE: onHitNoDamageRoll(input, attackStats, 1); break;
		case NO_ROLL_3_DAMAGE: onHitNoDamageRoll(input, attackStats, 3); break;
		case D3_PLUS_3_DAMAGE: onHitD3Plus3Damage(input, attackStats); break;
		}

		switch (attackStats.onCritModifier) {
		case EXTRA_DIE: onCritExtraDie(input, attackStats); break;
		}
		defaultBetterAttacks(input, attackStats);
		attackStats.isDecorated = true;
	}

	private float averageDamage(Input input, AttackStats attack) {
		float avg = 0.f;

		if (attack.onCritModifier == Constants.OnCritModifier.EXTRA_DIE) {
			avg = onCritExtraDieAverageDamage(input, attack);
		} else if (attack.isDamageDealing()) {
			int[] dmg = damage(attack);
			int max = maxDamageRoll(attack);

			for (int i = minDamageRoll(attack); i <= max; ++i) {
				avg += dmg[i] * damageProbability(
					attack.damageDice,
					attack.damageModifier,
					i
				);
			}
		}

		return avg;
	}

	private int[] damage(AttackStats a) {
		int maxDmg = maxDamageRoll(a);
		int[] dmg = new int[maxDmg + 1];

		if (a.isDamageDealing()) {
			int d = damageDelta(a);

			for (int i = minDamageRoll(a); i <= maxDmg; ++i) {
				dmg[i] = Math.max(0, i + d);

				switch (a.onHitModifier) {
				case DOUBLE_DAMAGE: dmg[i] *= 2; break;
				case MIN_1_DAMAGE: dmg[i] = Math.max(1, dmg[i]); break;
				case NO_ROLL_1_DAMAGE: dmg[i] = 1; break;
				case NO_ROLL_3_DAMAGE: dmg[i] = 3; break;
				case D3_PLUS_3_DAMAGE: dmg[i] = i; break;
				}
			}
		}

		return dmg;
	}

	private int damageDelta(AttackStats attackStats) {
		int d = attackStats.pow != Util.ND ?
				attackStats.pow - attackStats.arm :
				0;

		switch (attackStats.onHitModifier) {
		case NO_ROLL_1_DAMAGE: d = 0; break;
		case NO_ROLL_3_DAMAGE: d = 2; break;
		case D3_PLUS_3_DAMAGE: d = 0; break;
		}

		return d;
	}

	private int minDamageRoll(AttackStats a) {
		int dd = a.damageModifier == Constants.DamageModifier.DISCARD_LOWEST ?
				a.damageDice - 1 :
				a.damageDice;
		int r = mDiceService.min(dd);

		switch (a.onHitModifier) {
		case NO_ROLL_1_DAMAGE:
		case NO_ROLL_3_DAMAGE: r = 1; break;
		case D3_PLUS_3_DAMAGE: r = 4; break;
		}

		return r;
	}

	private int maxDamageRoll(AttackStats a) {
		int r = mDiceService.max(a.damageDice);

		switch (a.onHitModifier) {
		case NO_ROLL_1_DAMAGE:
		case NO_ROLL_3_DAMAGE: r = 1; break;
		case D3_PLUS_3_DAMAGE: r = 6; break;
		}

		if (a.onCritModifier == Constants.OnCritModifier.EXTRA_DIE) {
			r = mDiceService.max(a.damageDice + 1);
		}

		return r;
	}

	private float hitProbability(Input input, AttackStats attackStats) {
		float p = 0.f;

		if (attackStats.isAutoHit(input)) {
			p = 1.f;
		} else if (
			(input.kds || attackStats.def == Util.KD) &&
			attackStats.type != AttackStats.MAT
		) {
			p = hitProbability(attackStats.mat, 5, attackStats.attackDice,
					attackStats.attackModifier);
		} else {
			p = hitProbability(attackStats.mat, attackStats.def,
					attackStats.attackDice, attackStats.attackModifier);
		}

		return p;
	}

	private float hitKdProbability(Input input, AttackStats attackStats) {
		int origDef = attackStats.def;
		attackStats.def = Util.KD;
		float p = hitProbability(input, attackStats);
		attackStats.def = origDef;

		return p;
	}

	private float critProbability(Input input, AttackStats attack) {
		float p = 0.f;
		int def = (input.kds || attack.def == Util.KD) ? 0 : attack.def;
		int r = Math.max(attack.attackDice, def - attack.mat);

		switch (attack.attackModifier) {
		case NONE:
			p = mDiceService.crit(r, attack.attackDice);
			break;
		case REROLL:
			p = mDiceService.crit_rr(r, attack.attackDice);
			break;
		case DISCARD_LOWEST:
			p = mDiceService.crit_dl(r, attack.attackDice);
			break;
		case DISCARD_HIGHEST:
			p = mDiceService.crit_dh(r, attack.attackDice);
			break;
		}

		return p;
	}

	private float critKdProbability(Input input, AttackStats attackStats) {
		int origDef = attackStats.def;
		attackStats.def = Util.KD;
		float p = critProbability(input, attackStats);
		attackStats.def = origDef;

		return p;
	}
	
	private float hitProbability(int mat, int def, int attackDice,
			Constants.AttackModifier modifier) {
		float p = 0.f;
		int r = Math.min(
			attackDice * 6,
			Math.max(attackDice, def - mat)
		);

		switch (modifier) {
		case NONE: p = mDiceService.hit(r, attackDice); break;
		case REROLL: p = mDiceService.hitRr(r, attackDice); break;
		case DISCARD_LOWEST: p = mDiceService.hitDl(r, attackDice); break;
		case DISCARD_HIGHEST: p = mDiceService.hitDh(r, attackDice); break;
		}

		return p;
	}

	private void onHitDoubleDamage(Input input, AttackStats attackStats) {
		if (attackStats.isDamageDealing()) {
			int d = damageDelta(attackStats);
			for (int i = 0; i < attackStats.killRolls.length; ++i) {
				setKillRolls(input, attackStats, i, (input.boxes - i) / 2 - d);
			}
			attackStats.averageDamage *= 2;
		}
	}

	private void onHitMinOneDamage(Input input, AttackStats attackStats) {
		if (input.boxes - 1 < attackStats.killRolls.length) {
			attackStats.killRolls[input.boxes - 1] = attackStats.minDamageRoll;
		}
	}

	private void onHitNoDamageRoll(Input input, AttackStats a, int damage) {
		if (a.isDamageDealing()) {
			int min = minDamageRoll(a);
			int max = maxDamageRoll(a);
			a.averageDamage = damage;
			a.killRolls = new int[input.attacks.length * damage];
			a.focusKillRolls = new int[a.killRolls.length];
			a.furyKillRolls = new int[a.killRolls.length];
			for (int i = 0; i < input.attacks.length; ++i) {
				int j = i * damage;
				a.killRolls[j] = ((i + 1) * damage >= input.boxes) ?
						min : max + 1;
				a.focusKillRolls[j] = input.boxes - j + 5 < damage ?
						min : Integer.MAX_VALUE;
				a.furyKillRolls[j] = Integer.MAX_VALUE;
			}

			a.damageProbabilities = new float[4][max + 1][2];
			for (int i = a.minDamageRoll; i <= max; ++i) {
				a.damageProbabilities[0][i][0] = a.hitProbability;
				a.damageProbabilities[1][i][0] = a.hitKdProbability;
				a.damageProbabilities[2][i][0] = a.critProbability;
				a.damageProbabilities[3][i][0] =
						a.hitProbability - a.critProbability;
				a.damageProbabilities[0][i][1] = a.hitProbability;
				a.damageProbabilities[1][i][1] = a.hitKdProbability;
				a.damageProbabilities[2][i][1] = a.critProbability;
				a.damageProbabilities[3][i][1] =
						a.hitProbability - a.critProbability;
			}
		}
	}

	private void onHitD3Plus3Damage(Input input, AttackStats a) {
		a.averageDamage = 5.f;
		float p = 1.f / 3;
		float pHitKd = hitKdProbability(input, a);

		int len = a.maxDamageRoll;
		a.damageProbabilities = new float[4][len + 1][2];
		for (int i = a.minDamageRoll; i <= len; ++i) {
			int c = len - i + 1;
			a.damageProbabilities[0][i][0] = a.hitProbability * p;
			a.damageProbabilities[1][i][0] = pHitKd * p;
			a.damageProbabilities[2][i][0] = a.critProbability * p;
			a.damageProbabilities[3][i][0] =
					(a.hitProbability - a.critProbability) * p;
			a.damageProbabilities[0][i][1] = a.hitProbability * c * p;
			a.damageProbabilities[1][i][1] = pHitKd * c * p;
			a.damageProbabilities[2][i][1] = a.critProbability * c * p;
			a.damageProbabilities[3][i][1] =
					(a.hitProbability - a.critProbability) * c * p;
		}

		len = input.attacks.length * a.maxDamageRoll;
		a.killRolls = new int[len];
		a.focusKillRolls = new int[len];
		a.furyKillRolls = new int[len];
		for (int i = 0; i < len; ++i) {
			a.killRolls[i] =
					Math.max(a.minDamageRoll, input.boxes - i);
			a.focusKillRolls[i] = (i == input.boxes - 1) ?
					a.maxDamageRoll :
					Integer.MAX_VALUE;
			a.furyKillRolls[i] = Integer.MAX_VALUE;
		}
	}

	private void onCritExtraDie(Input input, AttackStats attackStats) {
		final int dd = attackStats.damageDice + 1;
		final int max = mDiceService.max(dd);
		float hCr = attackStats.critProbability;
		float hNoCr = attackStats.hitProbability - hCr;
		float hKdCr = critKdProbability(input, attackStats);
		float hKdNoCr = hitKdProbability(input, attackStats) - hKdCr;
		int d = damageDelta(attackStats);
		attackStats.maxDamageRoll = max;
		attackStats.damageProbabilities = new float[2][max + 1][2];

		Constants.DamageModifier m = attackStats.damageModifier;
		for (int i = attackStats.minDamageRoll; i <= max; ++i) {
			attackStats.damageProbabilities[0][i][0] =
					hNoCr * damageProbability(dd - 1, m, i) +
					hCr * damageProbability(dd, m, i);
			attackStats.damageProbabilities[1][i][0] =
					hKdNoCr * damageProbability(dd - 1, m, i) +
					hKdCr * damageProbability(dd, m, i);
			attackStats.damageProbabilities[0][i][1] =
					hNoCr * damageCumulativeProbability(dd - 1, m, i) +
					hCr * damageCumulativeProbability(dd, m, i);
			attackStats.damageProbabilities[1][i][1] =
					hKdNoCr * damageCumulativeProbability(dd - 1, m, i) +
					hKdCr * damageCumulativeProbability(dd, m, i);
		}
	}

	private float[][][] damageProbabilities(Input input, AttackStats a) {
		int max = maxDamageRoll(a);
		float[][][] ps = new float[4][max + 1][2];
		float pHit = hitProbability(input, a);
		float pHitKd = hitKdProbability(input, a);
		float pCrit = critProbability(input, a);

		for (int i = minDamageRoll(a); i <= max; ++i) {
			float pDmg = damageProbability(a.damageDice, a.damageModifier, i);
			float pCumulDmg = damageCumulativeProbability(a.damageDice,
				a.damageModifier, i);

			ps[0][i][0] = pHit * pDmg;
			ps[1][i][0] = pHitKd * pDmg;
			ps[2][i][0] = pCrit * pDmg;
			ps[3][i][0] = ps[0][i][0] - ps[2][i][0];
			ps[0][i][1] = pHit * pCumulDmg;
			ps[1][i][1] = pHitKd * pCumulDmg;
			ps[2][i][1] = pCrit * pCumulDmg;
			ps[3][i][1] = ps[0][i][1] - ps[2][i][1];
		}

		return ps;
	}

	private void defaultProbabilities(Input input, AttackStats attackStats) {
		attackStats.hitProbability = hitProbability(input, attackStats);
		attackStats.critProbability = critProbability(input, attackStats);
		attackStats.hitKdProbability = hitKdProbability(input, attackStats);
		attackStats.critKdProbability = critKdProbability(input, attackStats);
		attackStats.missProbability = 1.f - attackStats.hitProbability;
		attackStats.missKdProbability = 1.f - attackStats.hitKdProbability;
		attackStats.averageDamage = averageDamage(input, attackStats);
		attackStats.damageProbabilities =
				damageProbabilities(input, attackStats);
	}

	private void defaultRolls(AttackStats attackStats) {
		attackStats.minDamageRoll = minDamageRoll(attackStats);
		attackStats.maxDamageRoll = maxDamageRoll(attackStats);
		attackStats.damage = damage(attackStats);
	}

	private void defaultKillRolls(Input input, AttackStats attackStats) {
		int seekMax = maxKillRoll(input, attackStats);
		int d = damageDelta(attackStats);

		attackStats.killRolls = new int[seekMax + 1];
		attackStats.focusKillRolls = new int[seekMax + 1];
		attackStats.furyKillRolls = new int[seekMax + 1];

		for (int i = 0; i <= seekMax; ++i) {
			setKillRolls(input, attackStats, i, input.boxes - i - d);
		}
	}

	private void impossibleKillRolls(Input input, AttackStats attackStats) {
		int r = maxKillRoll(input, attackStats);
		final int max = Integer.MAX_VALUE;

		attackStats.killRolls = new int[r + 1];
		for (int i = 0; i <= r; ++i) attackStats.killRolls[i] = max;

		attackStats.focusKillRolls = new int[r + 1];
		for (int i = 0; i <= r; ++i) attackStats.focusKillRolls[i] = max;

		attackStats.furyKillRolls = new int[r + 1];
		for (int i = 0; i <= r; ++i) attackStats.furyKillRolls[i] = max;
	}

	private int maxKillRoll(Input input, AttackStats attackStats) {
		int r = 0;

		for (AttackStats s : input.attacks) {
			if (s == attackStats) break;
			r += Math.max(0, s.maxDamageRoll + damageDelta(s));
		}

		return r;
	}

	private void defaultBetterAttacks(Input input, AttackStats attackStats) {
		int i = 0;
		for (AttackStats s : input.attacks) {
			if (s == attackStats) break;
			i++;
		}

		List<Float> avgs = new LinkedList<Float>();
		for (int j = i + 1; j < input.attacks.length; ++j) {
			AttackStats s = input.attacks[j];
			int d = damageDelta(s);
			int min = minDamageRoll(s);
			int max = maxDamageRoll(s);
			float avg = (Math.max(1, min + d) + Math.max(1, max + d)) / 2.f;
			if (max + d <= 0) avg = 0.f;
			avgs.add(avg);
		}
		int size = attackStats.maxDamageRoll + damageDelta(attackStats) + 1;
		attackStats.betterAttacks = new int[size];
		for (int j = 0; j < size; j++) {
			for (float avg : avgs) {
				if (j < avg) attackStats.betterAttacks[j]++;
			}
		}
	}

	private void setKillRolls(Input input, AttackStats attackStats, int index,
			int roll) {
		if (index < input.boxes) {
			attackStats.killRolls[index] =
					Math.max(attackStats.minDamageRoll, roll);
			attackStats.focusKillRolls[index] =
					Math.max(attackStats.minDamageRoll, roll + 5);
			attackStats.furyKillRolls[index] = Integer.MAX_VALUE;
		} else {
			attackStats.killRolls[index] = Util.ANY;
			attackStats.focusKillRolls[index] = Util.ANY;
			attackStats.furyKillRolls[index] = Util.ANY;
		}
	}

	private float damageProbability(int damageDice,
			Constants.DamageModifier damageModifier, int roll) {
		float p = 0.f;

		switch (damageModifier) {
		case NONE:
			p = mDiceService.dmg(roll, damageDice);
			break;
		case REROLL:
			p = mDiceService.dmgRr(roll, damageDice);
			break;
		case DISCARD_LOWEST:
			p = mDiceService.dmgDl(roll, damageDice);
			break;
		}

		return p;
	}

	private float damageCumulativeProbability(int damageDice,
			Constants.DamageModifier damageModifier, int roll) {
		float p = 0.f;

		switch (damageModifier) {
		case NONE:
			p = mDiceService.dmgCum(roll, damageDice);
			break;
		case REROLL:
			p = mDiceService.dmgCumRr(roll, damageDice);
			break;
		case DISCARD_LOWEST:
			p = mDiceService.dmgCumDl(roll, damageDice);
			break;
		}

		return p;
	}

	private float onCritExtraDieAverageDamage(Input input, AttackStats a) {
		float avg = 0.f;

		if (a.isDamageDealing()) {
			int max = maxDamageRoll(a);
			int[] dmg = damage(a);
			float pCrit = critProbability(input, a);
			float pHitNoCrit = hitProbability(input, a) - pCrit;
			float total = pCrit + pHitNoCrit;
			pCrit /= total;
			pHitNoCrit /= total;

			for (int i = minDamageRoll(a); i <= max; ++i) {
				avg +=
					pHitNoCrit * dmg[i] * damageProbability(a.damageDice,
						a.damageModifier, i) +
					pCrit * dmg[i] * damageProbability(a.damageDice + 1,
						a.damageModifier, i);
			}
		}

		return avg;
	}
}

