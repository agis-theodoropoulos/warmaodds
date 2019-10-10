package com.eureton.warmaodds.effects;

import com.eureton.warmaodds.types.Combination;
import com.eureton.warmaodds.types.InputMeta;

public interface Effect {
	
	boolean isKilling(InputMeta meta, Combination combination);
	float killProbability(float previous, InputMeta meta,
			Combination combination);
}

