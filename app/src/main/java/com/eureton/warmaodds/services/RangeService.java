package com.eureton.warmaodds.services;

import java.util.List;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.types.Range;

public interface RangeService {
	
	Range getFull(InputMeta meta);
	List<Range> getRanges(InputMeta meta, int count);
	int getRangeSize(Input input, AttackStats attackStats);
}

