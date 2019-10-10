package com.eureton.warmaodds.models;

import android.os.Parcel;
import android.os.Parcelable;

public class AttackResults implements Parcelable {

	public float hit;
	public float critical;
	public float averageDamage;
	
	public AttackResults() { this(0.f, 0.f, 0.f); }
	
	public AttackResults(float hit, float critical, float averageDamage) {
		this.hit = hit;
		this.critical = critical;
		this.averageDamage = averageDamage;
	}
	
	public AttackResults(AttackResults other) {
		this.hit = other.hit;
		this.critical = other.critical;
		this.averageDamage = other.averageDamage;
	}

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeFloat(hit);
		dest.writeFloat(critical);
		dest.writeFloat(averageDamage);
	}
	
	public static final Parcelable.Creator<AttackResults> CREATOR =
			new Parcelable.Creator<AttackResults>() {
				public AttackResults createFromParcel(Parcel in) {
					return new AttackResults(
						in.readFloat(),
						in.readFloat(),
						in.readFloat()
					);
				}

				public AttackResults[] newArray(int size) {
					return new AttackResults[size];
				}
			};
}
