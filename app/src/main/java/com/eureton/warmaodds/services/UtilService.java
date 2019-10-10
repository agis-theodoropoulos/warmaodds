package com.eureton.warmaodds.services;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.types.Range;

public interface UtilService {
	
	InputMeta createMeta(Input input);
	float killProbability(InputMeta meta, String effect);
	float killProbability(InputMeta meta, String effect, Range rollRange,
			WarmachineService.ProgressListener listener);
}

