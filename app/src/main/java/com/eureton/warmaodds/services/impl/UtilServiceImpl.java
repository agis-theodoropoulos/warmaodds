package com.eureton.warmaodds.services.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.effects.Effect;
import com.eureton.warmaodds.effects.impl.Tough;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.DiceService;
import com.eureton.warmaodds.services.FormatterService;
import com.eureton.warmaodds.services.ModifierService;
import com.eureton.warmaodds.services.RangeService;
import com.eureton.warmaodds.services.UtilService;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.types.Combination;
import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.types.Range;
import com.eureton.warmaodds.util.Util;

public class UtilServiceImpl implements UtilService {

	private static final String TAG = UtilServiceImpl.class.getSimpleName();

	private final RangeService mRangeService;
	private final ModifierService mModifierService;
	private final FormatterService mFormatterService;
	private final DiceService mDiceService;

	@Inject
	public UtilServiceImpl(
		RangeService rangeService,
		ModifierService modifierService,
		FormatterService formatterService,
		DiceService diceService
	) {
		mRangeService = rangeService;
		mModifierService = modifierService;
		mFormatterService = formatterService;
		mDiceService = diceService;
	}
	
	@Override
	public InputMeta createMeta(Input input) {
		for (AttackStats s : input.attacks) mModifierService.decorate(input, s);
		
		return new InputMeta(input, createEffects(input));
	}

	@Override
	public float killProbability(InputMeta meta, String effect) {
		return killProbability(
			meta,
			effect,
			mRangeService.getFull(meta),
			null
		);
	}
	
	@Override
	public float killProbability(InputMeta meta, String effect, Range range,
			WarmachineService.ProgressListener listener) {
		float pKill = 0.f;
		Combination c =
				new Combination(meta.mInput.attacks.length, range);
		ProgressReporter r = new ProgressReporter(listener);
		String h = "COMBINATIONS";
		
		if (BuildConfig.DEBUG && effect != null) h += " (" + effect + ")";
		if (BuildConfig.DEBUG) Util.logCollectionStart(TAG, h);
		long start = System.currentTimeMillis();
		while (c.advance(meta)) {
			r.report(c.getAdvanceCount());
			float p = c.getKillProbability(meta);
			if (BuildConfig.DEBUG) logCombination(c, String.valueOf(p));
			pKill += p;
		}
		long end = System.currentTimeMillis();
		if (BuildConfig.DEBUG) Util.logCollectionEnd(TAG, h, end - start);
		
		return pKill;
	}

	private List<Effect> createEffects(Input input) {
		List<Effect> effects = new LinkedList<Effect>();

		if (input.tough) effects.add(new Tough(this));

		return effects;
	}
	
	private void logCombination(Combination combination, String value) {
		if (BuildConfig.DEBUG) Log.d(TAG, String.format(
			"| %s: %s",
			mFormatterService.combination(combination),
			value
		));
	}
	
	private static class ProgressReporter {
		
		private final WarmachineService.ProgressListener mListener;
		private int mPreviousCount;
		
		private ProgressReporter(WarmachineService.ProgressListener listener) {
			mListener = listener;
			mPreviousCount = -1;
		}
		
		private void report(int count) {
			if (mListener != null) {
				if (mPreviousCount < count) {
					mListener.onProgress(0);
					mPreviousCount = count;
				}
			}
		}
	}
}

