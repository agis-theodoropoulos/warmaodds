package com.eureton.warmaodds.services;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;

public interface ModifierService {

	void decorate(Input input, AttackStats attackStats);
}

