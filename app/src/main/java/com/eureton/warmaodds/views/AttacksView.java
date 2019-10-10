package com.eureton.warmaodds.views;

import com.eureton.warmaodds.models.AttackStats;

public interface AttacksView {

	void add(AttackStats attack);
	boolean remove(int index);
	void clear();
}

