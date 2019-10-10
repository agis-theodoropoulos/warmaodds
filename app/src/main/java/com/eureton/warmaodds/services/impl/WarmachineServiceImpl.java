package com.eureton.warmaodds.services.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Future;

import javax.inject.Inject;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.services.DiceService;
import com.eureton.warmaodds.services.FormatterService;
import com.eureton.warmaodds.services.MathService;
import com.eureton.warmaodds.services.ModifierService;
import com.eureton.warmaodds.services.RangeService;
import com.eureton.warmaodds.services.TaskManager;
import com.eureton.warmaodds.services.UtilService;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.types.Range;

public class WarmachineServiceImpl implements WarmachineService {
	
	private static final String TAG =
			WarmachineServiceImpl.class.getSimpleName();
			
	private final DiceService mDiceService;
	private final ModifierService mModifierService;
	private final MathService mMathService;
	private final UtilService mUtilService;
	private final RangeService mRangeService;
	private final TaskManager mTaskManager;
	private final FormatterService mFormatterService;

	@Inject
	public WarmachineServiceImpl(
		DiceService diceService,
		ModifierService modifierService,
		MathService mathService,
		UtilService utilService,
		TaskManager taskManager,
		RangeService rangeService,
		FormatterService formatterService
	) {
		mDiceService = diceService;
		mModifierService = modifierService;
		mMathService = mathService;
		mUtilService = utilService;
		mRangeService = rangeService;
		mTaskManager = taskManager;
		mFormatterService = formatterService;
	}
	
	@Override
	public float hitProbability(Input input, AttackStats attackStats) {
		if (!attackStats.isDecorated) {
			for (AttackStats s : input.attacks) {
				mModifierService.decorate(input, s);
			}
		}

		return attackStats.hitProbability;
	}

	@Override
	public float critProbability(Input input, AttackStats attackStats) {
		if (!attackStats.isDecorated) {
			for (AttackStats s : input.attacks) {
				mModifierService.decorate(input, s);
			}
		}

		return attackStats.critProbability;
	}

	@Override
	public float hitAny(Input input) {
		List<Float> ps = new LinkedList<Float>();
		for (AttackStats s : input.attacks) ps.add(hitProbability(input, s));

		return mMathService.disjoin(ps);
	}

	@Override
	public float critAny(Input input) {
		List<Float> ps = new LinkedList<Float>();
		for (AttackStats s : input.attacks) ps.add(critProbability(input, s));

		return mMathService.disjoin(ps);
	}

	@Override
	public float hitAll(Input input) {
		float p = 1.f;
		for (AttackStats s : input.attacks) p *= hitProbability(input, s);

		return p;
	}

	@Override
	public float critAll(Input input) {
		float p = 1.f;
		for (AttackStats s : input.attacks) p *= critProbability(input, s);

		return p;
	}

	@Override
	public Future<Float> killProbability(Input input,
			ProgressListener listener) {
		/*
		AttackStats[] atx = new AttackStats[5];
		atx[0] = new AttackStats(AttackStats.MAT, 6, 12,  9, 16, 2, 2,
			Constants.AttackModifier.NONE, Constants.DamageModifier.NONE,
			Constants.OnHitModifier.NONE, Constants.OnCritModifier.NONE,
			Constants.OnKillModifier.NONE);
		atx[1] = new AttackStats(AttackStats.MAT, 6, 12, 11, 16, 2, 2,
			Constants.AttackModifier.NONE, Constants.DamageModifier.NONE,
			Constants.OnHitModifier.NONE, Constants.OnCritModifier.NONE,
			Constants.OnKillModifier.NONE);
		atx[2] = new AttackStats(AttackStats.MAT, 6, 12, 14, 16, 2, 2,
			Constants.AttackModifier.NONE, Constants.DamageModifier.NONE,
			Constants.OnHitModifier.NONE, Constants.OnCritModifier.NONE,
			Constants.OnKillModifier.NONE);
		atx[3] = new AttackStats(AttackStats.MAT, 6, 12, 16, 16, 2, 2,
			Constants.AttackModifier.NONE, Constants.DamageModifier.NONE,
			Constants.OnHitModifier.NONE, Constants.OnCritModifier.NONE,
			Constants.OnKillModifier.NONE);
		atx[4] = new AttackStats(AttackStats.MAT, 6, 12, 19, 16, 2, 2,
			Constants.AttackModifier.NONE, Constants.DamageModifier.NONE,
			Constants.OnHitModifier.NONE, Constants.OnCritModifier.NONE,
			Constants.OnKillModifier.NONE);
		input = new Input(atx, 10, 0, 0, false, false);
		/**/

		InputMeta m = mUtilService.createMeta(input);
		List<Future<Float>> fs = new LinkedList<Future<Float>>();
		List<WarmachineWorker> ws = createWorkers(m,
				new ProxyListener(listener, m));
		if (BuildConfig.DEBUG) Log.i(TAG, mFormatterService.input(input));
		for (WarmachineWorker w : ws) fs.add(mTaskManager.submit(w));
		
		return new KillFuture(fs);
	}
	
	@Override
	public float averageDamage(Input input, AttackStats attackStats) {
		if (!attackStats.isDecorated) {
			for (AttackStats s : input.attacks) {
				mModifierService.decorate(input, s);
			}
		}

		return attackStats.averageDamage;
	}
	
	private List<WarmachineWorker> createWorkers(InputMeta meta,
			ProgressListener listener) {
		int wc = getWorkerCount(meta);
		List<WarmachineWorker> ws = new LinkedList<WarmachineWorker>();
		int i = 0;

		for (Range r : mRangeService.getRanges(meta, wc)) {
			int id = ++i;

			if (BuildConfig.DEBUG) Log.d(TAG, String.format(
				Locale.US,
				"Worker #%d range: %s",
				id,
				mFormatterService.range(r)
			));
			ws.add(new WarmachineWorker(
				id,
				meta,
				mUtilService,
				r,
				listener
			));
		}

		return ws;
	}
	
	private int getWorkerCount(InputMeta meta) {
		//return 1;
		//return Runtime.getRuntime().availableProcessors() * 2;
		return (meta.getCombinationCount() > 30) ?
				Runtime.getRuntime().availableProcessors() * 2 :
				1;
	}
	
	private class ProxyListener implements WarmachineService.ProgressListener {
		
		private final ProgressListener mListener;
		private final float mInterval;
		private int mCount;
		
		private ProxyListener(ProgressListener listener, InputMeta meta) {
			AttackStats s = meta.mInput.attacks[0];
			
			mListener = listener;
			mInterval = 100.f / mRangeService.getRangeSize(meta.mInput, s);
		}
		
		@Override
		public void onProgress(int status) {
			mListener.onProgress((int) (++mCount * mInterval));
		}
	}
}

