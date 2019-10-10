package com.eureton.warmaodds.views;

import com.eureton.warmaodds.widgets.DamageRoll;
import com.eureton.warmaodds.widgets.HitRoll;

import android.os.Bundle;

public interface Attack extends AttackView {
	void setRemovable(boolean status);
	void copyTo(Attack other);
	AttackEventListener setAttackEventListener(AttackEventListener listener);
	HitRoll getAttack();
	DamageRoll getDamage();
	
	public interface AttackEventListener {
		void onTypeChanged(Attack view, int newValue);
		void onMatChanged(Attack view, int newValue);
		void onDefChanged(Attack view, int newValue);
		void onPowChanged(Attack view, int newValue);
		void onArmChanged(Attack view, int newValue);
		void onAttackDiceChanged(Attack view, int newValue);
		void onDamageDiceChanged(Attack view, int newValue);
		void onRemoved(Attack view);
	}
}

