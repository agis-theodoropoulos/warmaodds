package com.eureton.warmaodds.models;

import java.util.UUID;

import android.os.Parcel;
import android.os.Parcelable;

import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.util.Util;

public class AttackStats implements Parcelable, Comparable<AttackStats> {

	public static final int MAT = 1;
	public static final int RAT = 2;
	public static final int MGC = 3;

	public int type;
	public int mat;
	public int def;
	public int pow;
	public int arm;
	public int attackDice;
	public int damageDice;
	public Constants.AttackModifier attackModifier;
	public Constants.DamageModifier damageModifier;
	public Constants.OnHitModifier onHitModifier;
	public Constants.OnCritModifier onCritModifier;
	public Constants.OnKillModifier onKillModifier;
	public final UUID id;

	// meta info, included directly inside the object to improve performance
	public boolean isDecorated;
	public float hitProbability;
	public float critProbability;
	public float hitKdProbability;
	public float critKdProbability;
	public float missProbability;
	public float missKdProbability;
	public int[] damage;
	public int[] killRolls;
	public int[] focusKillRolls;
	public int[] furyKillRolls;
	/*
	 *	[0 .. 1] (0: No KD/S, 1: KD/S, 2: Crit, 3: No Crit, No KD/S)
	 *	[0 .. maxDamageRoll + 1]
	 *	[0 .. 1] (0: Simple, 1: Cumulative)
	 */
	public float[][][] damageProbabilities;
	public int minDamageRoll;
	public int maxDamageRoll;
	public int[] betterAttacks;
	public float averageDamage;
	
	public AttackStats(int type, int mat, int def, int pow, int arm,
			int attackDice, int damageDice,
			Constants.AttackModifier attackModifier,
			Constants.DamageModifier damageModifier,
			Constants.OnHitModifier onHitModifier,
			Constants.OnCritModifier onCritModifier,
			Constants.OnKillModifier onKillModifier) {
		this.type = type;
		this.mat = mat;
		this.def = def;
		this.pow = pow;
		this.arm = arm;
		this.attackDice = attackDice;
		this.damageDice = damageDice;
		this.attackModifier = attackModifier;
		this.damageModifier = damageModifier;
		this.onHitModifier = onHitModifier;
		this.onCritModifier = onCritModifier;
		this.onKillModifier = onKillModifier;
		id = UUID.randomUUID();
	}
	
	public AttackStats(AttackStats other) {
		this(
			other.type,
			other.mat,
			other.def,
			other.pow,
			other.arm,
			other.attackDice,
			other.damageDice,
			other.attackModifier,
			other.damageModifier,
			other.onHitModifier,
			other.onCritModifier,
			other.onKillModifier
		);
	}

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeInt(mat);
		dest.writeInt(def);
		dest.writeInt(pow);
		dest.writeInt(arm);
		dest.writeInt(attackDice);
		dest.writeInt(damageDice);
		dest.writeSerializable(attackModifier);
		dest.writeSerializable(damageModifier);
		dest.writeSerializable(onHitModifier);
		dest.writeSerializable(onCritModifier);
		dest.writeSerializable(onKillModifier);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof AttackStats)) return false;
		if (obj == this) return true;
		
		AttackStats other = (AttackStats) obj;
		
		return type == other.type &&
				mat == other.mat &&
				def == other.def &&
				pow == other.pow &&
				arm == other.arm &&
				attackDice == other.attackDice &&
				damageDice == other.damageDice &&
				attackModifier == other.attackModifier &&
				damageModifier == other.damageModifier &&
				onHitModifier == other.onHitModifier &&
				onCritModifier == other.onCritModifier &&
				onKillModifier == other.onKillModifier;
	}
	
	@Override
	public int compareTo(AttackStats other) {
		int res = 0;
		
		if (damageDice > other.damageDice) {
			res = 1;
		} else if (damageDice < other.damageDice) {
			res = -1;
		}
		
		return res;
	}

	public void copyTargetStatsFrom(AttackStats other) {
		def = other.def;
		arm = other.arm;
	}

	public final boolean isAutoHit(Input input) {
		return (
			mat == Util.AUT || (
				type == MAT &&
				(input.kds || def == Util.KD)
			)
		);
	}

	public final boolean isDamageDealing() { return pow != Util.ND; }

	public final boolean hasModifiers() {
		return (
			attackModifier != Constants.AttackModifier.NONE ||
			damageModifier != Constants.DamageModifier.NONE ||
			onHitModifier != Constants.OnHitModifier.NONE ||
			onCritModifier != Constants.OnCritModifier.NONE ||
			onKillModifier != Constants.OnKillModifier.NONE
		);
	}

	public static final Parcelable.Creator<AttackStats> CREATOR =
			new Parcelable.Creator<AttackStats>() {
				public AttackStats createFromParcel(Parcel in) {
					return new AttackStats(
						in.readInt(),
						in.readInt(),
						in.readInt(),
						in.readInt(),
						in.readInt(),
						in.readInt(),
						in.readInt(),
						(Constants.AttackModifier) in.readSerializable(),
						(Constants.DamageModifier) in.readSerializable(),
						(Constants.OnHitModifier) in.readSerializable(),
						(Constants.OnCritModifier) in.readSerializable(),
						(Constants.OnKillModifier) in.readSerializable()
					);
				}

				public AttackStats[] newArray(int size) {
					return new AttackStats[size];
				}
			};
}

