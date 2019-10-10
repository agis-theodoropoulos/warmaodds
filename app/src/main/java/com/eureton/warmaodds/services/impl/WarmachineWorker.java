package com.eureton.warmaodds.services.impl;

import java.util.List;
import java.util.concurrent.Callable;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.services.UtilService;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.types.Combination;
import com.eureton.warmaodds.types.InputMeta;
import com.eureton.warmaodds.types.Range;

class WarmachineWorker implements Callable<Float> {
	
	private final String TAG;
	private final InputMeta mMeta;
	private final UtilService mUtilService;
	private final WarmachineService.ProgressListener mListener;
	private final Range mRange;
	
	WarmachineWorker(int id, InputMeta meta, UtilService utilService,
			Range range, WarmachineService.ProgressListener listener) {
		TAG = WarmachineWorker.class.getSimpleName() + "#" + id;
		mMeta = meta;
		mUtilService = utilService;
		mRange = range;
		mListener = listener;
	}

	@Override
	public Float call() {
		float p = mUtilService.killProbability(mMeta, TAG, mRange, mListener);

		if (BuildConfig.DEBUG) Log.i(TAG, "KILL: " + p);
		
		return p;
	}
}

