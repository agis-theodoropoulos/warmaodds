package com.eureton.warmaodds.types;

import java.util.List;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.effects.Effect;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.types.Constants.OnCritModifier;
import com.eureton.warmaodds.types.Constants.OnHitModifier;
import com.eureton.warmaodds.util.Util;

public class Combination {
	
	private static final String TAG = Combination.class.getSimpleName();
	private static final String SEPARATOR = "-";
	
	private final int mSize;
	private final int[] mRolls;
	private final Mitigator[] mMitigators;
	private final int mStartRoll;
	private final int mEndRoll;
	private int mSpan;
	private int mAdvanceCount;
	private int mFocus;
	private int mFury;
	private float mAlgoA, mAlgoB;
	
	public Combination(int size, Range rollRange) {
		if (size <= 0) throw new IllegalArgumentException();

		mSize = size;
		mRolls = new int[size];
		mMitigators = new Mitigator[size];
		for (int i = 0; i < size; ++i) mRolls[i] = Util.ANY;

		mStartRoll = rollRange.max;
		mEndRoll = rollRange.min;
		mSpan = 0;
	}
	
	public final int getSize() { return mSize; }
	
	public final int getRoll(int index) { return mRolls[index]; }
	
	public final Mitigator getMitigator(int index) {
		return mMitigators[index];
	}
	
	public final int getSpan() { return mSpan; }
	
	public final int getAdvanceCount() { return mAdvanceCount; }
	
	public final boolean isTemplate() { return mSpan < mSize; }
	
	public final boolean advance(InputMeta meta) {
		final Input input = meta.mInput;
		final AttackStats[] attacks = input.attacks;
		boolean done = true;
		int anchor = (mSpan < mSize) ? mSpan - 1 : mSize - 2;
		int lead = anchor + 1;

		if (BuildConfig.DEBUG) Log.d(TAG, "mSpan: " + mSpan);
		if (mSize == 1 && mSpan == mSize) return false;
		do {
			done = true;
			if (anchor >= 0) {
				while (isDepleted(input, anchor)) {
					if (anchor == 0) return false;
					anchor--;
					lead--;
				}
				downtick(input, anchor);
			}

			seek(input, lead);
			while (!isInRange(attacks, lead)) {
				if (anchor == -1 && mSize == 1) return false;
				if (lead == 0 && mRolls[0] < mEndRoll) return false;
				if (lead == mSize - 1) {
					done = false;
					break;
				} else {
					maximize(attacks, lead);
					seek(input, ++lead);
					anchor++;
				}
			}
		} while (!done);

		return true;
	}

	public final float getKillProbability(InputMeta meta) {
		float pKill = getRawKillProbability(meta.mInput);
		List<Effect> effects = meta.getEffects();

		for (Effect e : effects) pKill = e.killProbability(pKill, meta, this);

		return pKill;
	}

	public final float getAlgoA() { return mAlgoA; }

	public final float getAlgoB() { return mAlgoB; }

	private float getRawKillProbability(Input input) {
		final int size = input.attacks.length;

		algoInit(input);
		for (int i = 0; i < size; ++i) {
			AttackStats s = input.attacks[i];
			int r = mRolls[i];

			switch (r) {
			case Util.ANY: break;
			case Util.MISS: algoOnMiss(s); break;
			default: algoOnHit(s, r, i); break;
			}
			if (BuildConfig.DEBUG) Log.d(
				TAG,
				String.format("[%d] A: %f, B: %f", i, mAlgoA, mAlgoB)
			);
		}

		return mAlgoA + mAlgoB;
	}

	private void algoInit(Input input) {
		if (input.kds) {
			mAlgoA = 0.f;
			mAlgoB = 1.f;
		} else {
			mAlgoA = 1.f;
			mAlgoB = 0.f;
		}
	}

	private void algoOnMiss(AttackStats attackStats) {
		mAlgoA *= attackStats.missProbability;
		mAlgoB *= attackStats.missKdProbability;
	}

	private void algoOnHit(AttackStats attackStats, int roll, int index) {
		int c = isUsingCumulative(index) ? 1 : 0;
		float[][][] ps = attackStats.damageProbabilities;

		if (attackStats.onHitModifier == OnHitModifier.KNOCKDOWN) {
			mAlgoB = mAlgoA * ps[0][roll][c] + mAlgoB * ps[1][roll][c];
			mAlgoA = 0.f;
		} else if (attackStats.onCritModifier == OnCritModifier.KNOCKDOWN) {
			mAlgoB = mAlgoA * ps[2][roll][c] + mAlgoB * ps[1][roll][c];
			mAlgoA *= ps[3][roll][c];
		} else {
			mAlgoA *= ps[0][roll][c];
			mAlgoB *= ps[1][roll][c];
		}
	}

	private boolean isDepleted(Input input, int index) {
		final AttackStats s = input.attacks[index];
		final int r = mRolls[index];
		boolean res = false;
		
		if (index == 0 && r == mEndRoll) {
			res = true;
		} else if (r != Util.ANY) {
			if (s.isAutoHit(input)) {
				res = r == s.minDamageRoll;
			} else {
				res = r == Util.MISS;
			}
		}
		if (BuildConfig.DEBUG) {
			Log.d(TAG, String.format("depleted(%d): %d in [%d, %d]? %s",
					index, r, index == 0 ? mEndRoll : s.minDamageRoll,
					index == 0 ? mStartRoll : s.maxDamageRoll, res));
		}
		
		return res;
	}

	private void downtick(Input input, int index) {
		final int size = mSize;
		final AttackStats s = input.attacks[index];
		final int[] rs = mRolls;
		final int r = rs[index];
		int res = Util.ANY;

		for (int i = index + 1; i < mSize; ++i) {
			if (rs[i] != Util.ANY) mSpan--;
			rs[i] = Util.ANY;
		}
		if (r == Util.ANY) {
			res = s.maxDamageRoll;
			mSpan++;
		} else if (!s.isAutoHit(input) && r == s.minDamageRoll) {
			res = Util.MISS;
		} else {
			res = r - 1;
		}
		rs[index] = res;
		if (index == 0) mAdvanceCount++;
		if (BuildConfig.DEBUG) Log.d(TAG, "downtick(" + index + ") -> " + res);
	}

	private void seek(Input input, int index) {
		final int[] rs = mRolls;
		final AttackStats[] attacks = input.attacks;
		AttackStats s = null;
		int d = 0;

		mFocus = input.focus;
		mFury = input.fury;
		if (BuildConfig.DEBUG) for (int i = 0; i < mMitigators.length; ++i) {
			mMitigators[i] = Mitigator.NONE;
		}

		for (int i = 0; i < index; ++i) {
			s = attacks[i];
			d += mitigateDamage(input, d, rs[i], i, index);
		}

		s = attacks[index];
		int res = lowerBound(s, mitigateRoll(s, d, index));
		if (rs[index] == Util.ANY && res != Util.ANY) mSpan++;
		rs[index] = res;
		if (BuildConfig.DEBUG) Log.d(TAG, "seek(" + index + ") -> " + res);
	}

	private int mitigateDamage(Input input, int previousDamage, int roll,
			int index, int lastIndex) {
		AttackStats s = input.attacks[index];
		int d = s.damage[roll];
		int totalDamage = previousDamage + d;

		if (spendFocus(input, totalDamage, d, index)) {
			d = Math.max(0, d - 5);
			mFocus--;
			if (BuildConfig.DEBUG) mMitigators[index] = Mitigator.FOCUS;
		} else if (spendFury(input, s, totalDamage, d, index)) {
			d = 0;
			mFury--;
			if (BuildConfig.DEBUG) mMitigators[index] = Mitigator.FURY;
		}
		if (BuildConfig.DEBUG) Log.d(TAG, String.format(
				"mitigateDamage(%d) %d -> %d", index, s.damage[roll], d));

		return d;
	}

	private boolean spendFocus(Input input, int totalDamage, int damage,
			int index) {
		boolean s = (
			mFocus > 0 && (
				totalDamage >= input.boxes ||	// kills
				mFocus >= mSize - index ||		// might as well
				damage >= 5						// put to good use
			)
		);

		if (BuildConfig.DEBUG) Log.d(TAG, String.format(
			"spendFocus(%d, %d, %d): %s (focus: %d)",
			totalDamage,
			damage,
			index,
			s,
			mFocus
		));

		return s;
	}

	private boolean spendFury(Input input, AttackStats attackStats,
			int totalDamage, int damage, int index) {
		int remaining = mSize - 1 - index;
		int better = attackStats.betterAttacks[damage];
		boolean s = (
			mFury > 0 &&
			(
				totalDamage >= input.boxes ||			// kills
				mFury > remaining ||					// might as well
				(mFury > better && mFury != remaining)	// omniscience
			)
		);

		if (BuildConfig.DEBUG) Log.d(TAG, String.format(
			"spendFury(%d, %d, %d, %d): %s (fury: %d)",
			totalDamage, damage, index, better, s, mFury
		));

		return s;
	}

	private int mitigateRoll(AttackStats attackStats, int damage, int index) {
		int r = 0;

		if (mFocus > 0) {
			mFocus--;
			r = attackStats.focusKillRolls[damage];
			if (BuildConfig.DEBUG) mMitigators[index] = Mitigator.FOCUS;
		} else if (mFury > 0) {
			mFury--;
			r = attackStats.furyKillRolls[damage];
			if (BuildConfig.DEBUG) mMitigators[index] = Mitigator.FURY;
		} else {
			r = attackStats.killRolls[damage];
		}
		if (BuildConfig.DEBUG) Log.d(TAG,
				String.format("mitigateRoll(%d, %d) -> %d", index, damage, r));

		return r;
	}

	private int lowerBound(AttackStats attackStats, int roll) {
		return roll != Util.ANY ?
				Math.max(attackStats.minDamageRoll, roll) :
				roll;
	}

	private boolean isInRange(AttackStats[] attacks, int index) {
		final AttackStats s = attacks[index];
		final int r = mRolls[index];
		boolean b = false;
		final String f = "%d in [%d, %d]? %s";
		int min = s.minDamageRoll;
		int max = s.maxDamageRoll;

		if (index == 0) {
			b = (r <= mStartRoll && r >= mEndRoll);
		} else {
			b = (
				r == Util.ANY ||
				r == Util.MISS || (
					r <= s.maxDamageRoll &&
					r >= s.minDamageRoll
				)
			);
		}
		if (BuildConfig.DEBUG) Log.d(TAG, String.format(f, r, min, max, b));

		return b;
	}

	private void maximize(AttackStats[] attacks, int index) {
		final int[] rs = mRolls;
		int res = (index == 0) ?
				mStartRoll :
				attacks[index].maxDamageRoll;

		if (rs[index] == Util.ANY) mSpan++;
		rs[index] = res;
		if (BuildConfig.DEBUG) Log.d(TAG, "maximize(" + index + ") -> " + res);
	}
	
	private boolean isUsingCumulative(int index) {
		int r = mRolls[index];

		return index == mSpan - 1 && r != Util.MISS;
	}

	public enum Mitigator {
		NONE,
		FOCUS,
		FURY
	}
}

