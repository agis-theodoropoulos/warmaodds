package com.eureton.warmaodds.presenters;

import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.views.Modifiers;

public interface ModifiersPresenter extends Presenter<Modifiers> {

	void indexChanged(int newValue);
	void attackModifierChanged(int index, Constants.AttackModifier newValue);
	void damageModifierChanged(int index, Constants.DamageModifier newValue);
	void onHitModifierChanged(int index, Constants.OnHitModifier newValue);
	void onCritModifierChanged(int index, Constants.OnCritModifier newValue);
	void onKillModifierChanged(int index, Constants.OnKillModifier newValue);
	void onModifiersCleared(int index);
	void cancel();
}

