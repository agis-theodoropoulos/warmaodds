package com.eureton.warmaodds.types;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.eureton.warmaodds.effects.Effect;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;

public class InputMeta {
	
	public final Input mInput;
	private final List<Effect> mEffects;

	public InputMeta(Input input, List<Effect> effects) {
		mInput = input;
		mEffects = effects;
	}

	public List<Effect> getEffects() { return mEffects; }
	
	public int getCombinationCount() {
		int count = 1;

		for (AttackStats s : mInput.attacks) {
			count *= s.maxDamageRoll - s.minDamageRoll + 1;
		}

		return count;
	}
}

