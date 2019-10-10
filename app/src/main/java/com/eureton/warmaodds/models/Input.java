package com.eureton.warmaodds.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Input implements Parcelable {

	public AttackStats[] attacks;
	public int boxes;
	public int focus;
	public int fury;
	public boolean kds;
	public boolean tough;
	
	public Input(AttackStats[] attacks, int boxes, int focus,
			int fury, boolean kds, boolean tough) {
		this.attacks = attacks;
		this.boxes = boxes;
		this.focus = focus;
		this.fury = fury;
		this.tough = tough;
	}
	
	public Input(Input other) {
		attacks = new AttackStats[other.attacks.length];
		for (int i = 0; i < attacks.length; ++i) {
			attacks[i] = new AttackStats(other.attacks[i]);
		}

		boxes = other.boxes;
		focus = other.focus;
		fury = other.fury;
		kds = other.kds;
		tough = other.tough;
	}

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedArray(attacks, 0);
		dest.writeInt(boxes);
		dest.writeInt(focus);
		dest.writeInt(fury);
		dest.writeByte((byte) (kds ? 1 : 0));
		dest.writeByte((byte) (tough ? 1 : 0));
	}
	
	public void copyTo(Input other) {
		final int size = attacks.length;

		if (other.attacks.length != size) other.attacks = new AttackStats[size];
		for (int i = 0; i < size; ++i) {
			other.attacks[i] = new AttackStats(attacks[i]);
		}
		other.boxes = boxes;
		other.focus = focus;
		other.fury = fury;
		other.kds = kds;
		other.tough = tough;
	}

	public void add(AttackStats attackStats) {
		final int oldSize = attacks.length;
		AttackStats[] as = new AttackStats[oldSize + 1];
		for (int i = 0; i < oldSize; ++i) as[i] = attacks[i];
		as[oldSize] = attackStats;

		attacks = as;
	}

	public void remove(int index) {
		final int oldSize = attacks.length;

		if (index >= 0 && index < oldSize) {
			AttackStats[] as = new AttackStats[oldSize - 1];
			int j = 0;
			for (int i = 0; i < oldSize; ++i) {
				if (i != index) as[j++] = attacks[i];
			}
			attacks = as;
		}
	}
	
	public static final Parcelable.Creator<Input> CREATOR =
			new Parcelable.Creator<Input>() {
				public Input createFromParcel(Parcel in) {
					return new Input(
						in.createTypedArray(AttackStats.CREATOR),
						in.readInt(),
						in.readInt(),
						in.readInt(),
						in.readByte() == 0 ? false : true,
						in.readByte() == 0 ? false : true
					);
				}

				public Input[] newArray(int size) { return new Input[size]; }
			};
}
