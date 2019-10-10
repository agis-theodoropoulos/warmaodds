package com.eureton.warmaodds.effects.impl;

import java.util.List;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.effects.Effect;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.UtilService;
import com.eureton.warmaodds.types.Combination;
import com.eureton.warmaodds.types.Constants.OnCritModifier;
import com.eureton.warmaodds.types.Constants.OnHitModifier;
import com.eureton.warmaodds.types.Constants.OnKillModifier;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.util.Util;

public class Tough implements Effect {
	
	private static final String TAG = Tough.class.getSimpleName();

	private final UtilService mUtilService;

	public Tough(UtilService utilService) {
		mUtilService = utilService;
	}

	@Override
	public boolean isKilling(InputMeta meta, Combination combination) {
		return true;
	}

	@Override
	public float killProbability(float previous, InputMeta meta,
			Combination combination) {
		float res = previous;

		if (!isToughCancelled(meta, combination)){
			if (combination.isTemplate()) {
				InputMeta m = transformMeta(meta, combination);
				float pKill = mUtilService.killProbability(m, TAG);

				res = Util.NOT_TOUGH * combination.getAlgoA() +
						combination.getAlgoB() +
						Util.TOUGH * combination.getAlgoA() * pKill;
			} else {
				res = Util.NOT_TOUGH * combination.getAlgoA() +
						combination.getAlgoB();
			}
		} else if (BuildConfig.DEBUG) {
			Log.d(TAG, "Attack #" + (combination.getSpan() - 1) + ": no Tough");
		}

		return res;
	}

	private InputMeta transformMeta(InputMeta meta, Combination combination) {
		Input i = new Input(meta.mInput);
		int diff = i.attacks.length - combination.getSpan();
		InputMeta m = null;

		if (diff > 0) {
			while (i.attacks.length > diff) i.remove(0);

			i.boxes = 1;
			i.tough = false;
			i.kds = true;
			for (AttackStats s : i.attacks) s.def = Util.KD;

			m = mUtilService.createMeta(i);
		}

		return m;
	}

	private boolean isToughCancelled(InputMeta meta, Combination combination) {
		Input i = meta.mInput;
		AttackStats s = i.attacks[combination.getSpan() - 1];
		
		return (
			i.kds ||
			s.def == Util.KD ||
			s.onKillModifier == OnKillModifier.NO_TOUGH ||
			isKnockedDown(i, combination)
		);
	}

	private boolean isKnockedDown(Input input, Combination combination) {
		boolean isKd = false;
		int span = combination.getSpan();

		for (int i = 0; i < span; ++i) {
			AttackStats s = input.attacks[i];
			int r = combination.getRoll(i);

			if (r != Util.MISS && s.onHitModifier == OnHitModifier.KNOCKDOWN) {
				isKd = true;
				break;
			}
		}

		return isKd;
	}
}

