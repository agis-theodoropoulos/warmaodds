package com.eureton.warmaodds.services;

import java.util.List;
import java.util.concurrent.Future;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;

public interface WarmachineService {

	float hitProbability(Input input, AttackStats attack);
	float critProbability(Input input, AttackStats attack);
	float averageDamage(Input input, AttackStats attack);

	float hitAny(Input input);
	float hitAll(Input input);
	float critAny(Input input);
	float critAll(Input input);
	Future<Float> killProbability(Input input, ProgressListener listener);
	
	public interface ProgressListener {
		
		void onProgress(int status);
	}
}

