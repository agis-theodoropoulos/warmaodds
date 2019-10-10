package com.eureton.warmaodds.tasks;

import java.util.List;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.models.AttackResults;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.util.Util;

public class AllButKillTask extends Task {
	
	private static final String TAG = AllButKillTask.class.getSimpleName();

	private final WarmachineService mWarmachineService;
	
	public AllButKillTask(WarmachineService warmachineService) {
		mWarmachineService = warmachineService;
	}

	@Override
	protected void run() {
		singles();
		totals();
	}

	private void singles() {
		Input input = mState.input;
		int len = input.attacks.length;

		for (int i = 0; i < len; ++i) {
			AttackStats s = input.attacks[i];
			AttackResults r = mState.output.attackResults.get(i);

			hit(input, s, r, i);
			crit(input, s, r, i);
			average(input, s, r, i);
		}
	}

	private void hit(Input input, AttackStats attackStats,
			AttackResults attackResults, int index) {
		long start = 0, end = 0;

		if (BuildConfig.DEBUG) start = System.currentTimeMillis();
		attackResults.hit =
				mWarmachineService.hitProbability(input, attackStats);
		if (BuildConfig.DEBUG) {
			end = System.currentTimeMillis();
			Util.log(TAG, "ATTACK", index, attackResults.hit * 100,
					end - start);
		}
	}

	private void crit(Input input, AttackStats attackStats,
			AttackResults attackResults, int index) {
		long start = 0, end = 0;

		if (BuildConfig.DEBUG) start = System.currentTimeMillis();
		attackResults.critical =
				mWarmachineService.critProbability(input, attackStats);
		if (BuildConfig.DEBUG) {
			end = System.currentTimeMillis();
			Util.log(TAG, "CRITICAL", index, attackResults.critical * 100,
					end - start);
		}
	}

	private void average(Input input, AttackStats attackStats,
			AttackResults attackResults, int index) {
		long start = 0, end = 0;
		
		if (BuildConfig.DEBUG) start = System.currentTimeMillis();
		attackResults.averageDamage =
				mWarmachineService.averageDamage(input, attackStats);
		if (BuildConfig.DEBUG) {
			end = System.currentTimeMillis();
			Log.d(TAG, String.format(
				"setting AVG_DMG[%d] to %f [%d ms]",
				index,
				attackResults.averageDamage,
				end - start
			));
		}
	}

	private void totals() {
		Input input = mState.input;
		Output out = mState.output;
		long startHit = 0, endHit = 0, startCrit = 0, endCrit = 0;

		if (BuildConfig.DEBUG) startHit = System.currentTimeMillis();
		out.hit = mWarmachineService.hitAny(input);
		if (BuildConfig.DEBUG) endHit = System.currentTimeMillis();

		if (BuildConfig.DEBUG) startCrit = endHit;
		out.critical = mWarmachineService.critAny(input);
		if (BuildConfig.DEBUG) endCrit = System.currentTimeMillis();
		
		out.hitAll = mWarmachineService.hitAll(input);
		out.criticalAll = mWarmachineService.critAll(input);

		if (BuildConfig.DEBUG) {
			Util.log(TAG, "HIT", out.hit, endHit - startHit);
			Util.log(TAG, "CRITICAL", out.critical, endCrit - startCrit);
		}
	}
}

