package com.eureton.warmaodds.models;

import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class Output implements Parcelable {
	public List<AttackResults> attackResults;
	public float hit;
	public float critical;
	public float hitAll;
	public float criticalAll;
	public float kill;
	
	public Output(List<AttackResults> attackResults, float hit, float critical,
			float hitAll, float criticalAll, float kill) {
		this.attackResults = attackResults;
		this.hit = hit;
		this.critical = critical;
		this.hitAll = hitAll;
		this.criticalAll = criticalAll;
		this.kill = kill;
	}

	public Output() {
		this(new LinkedList<AttackResults>(), 0.f, 0.f, 0.f, 0.f, 0.f);
	}
	
	public Output(Output other) {
		attackResults = new LinkedList<AttackResults>();
		for (AttackResults r : other.attackResults) {
			attackResults.add(new AttackResults(r));
		}

		hit = other.hit;
		critical = other.critical;
		hitAll = other.hitAll;
		criticalAll = other.criticalAll;
		kill = other.kill;
	}

	@Override
	public int describeContents() { return 0; }

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeTypedList(attackResults);
		dest.writeFloat(hit);
		dest.writeFloat(critical);
		dest.writeFloat(hitAll);
		dest.writeFloat(criticalAll);
		dest.writeFloat(kill);
	}
	
	public void copyTo(Output other) {
		other.attackResults = new LinkedList<AttackResults>();
		for (AttackResults r : attackResults) {
			other.attackResults.add(new AttackResults(r));
		}

		other.hit = hit;
		other.critical = critical;
		other.hitAll = hitAll;
		other.criticalAll = criticalAll;
		other.kill = kill;
	}
	
	public static final Parcelable.Creator<Output> CREATOR =
			new Parcelable.Creator<Output>() {
				public Output createFromParcel(Parcel in) {
					List<AttackResults> l =
							in.createTypedArrayList(AttackResults.CREATOR);

					return new Output(
						new LinkedList<AttackResults>(l),
						in.readFloat(),
						in.readFloat(),
						in.readFloat(),
						in.readFloat(),
						in.readFloat()
					);
				}

				public Output[] newArray(int size) { return new Output[size]; }
			};
}

