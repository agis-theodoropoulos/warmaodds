package com.eureton.warmaodds.services.impl;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;

class KillFuture implements Future<Float> {
	
	private static final String TAG = KillFuture.class.getSimpleName();
	
	private final List<Future<Float>> mFutures;
	private boolean mIsDone;
	private boolean mIsCancelled;
	private float mResult;

	public KillFuture(List<Future<Float>> futures) {
		if (futures == null) throw new IllegalArgumentException();
		mFutures = futures;
	}
	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		for (Future<Float> f : mFutures) f.cancel(true);
		mIsCancelled = true;
		
		return true;
	}
	
	@Override
	public Float get(long timeout, TimeUnit unit) {
		mResult = 0.f;
		
		try {
			for (Future<Float> f : mFutures) mResult += f.get(timeout, unit);
			mIsDone = true;
		} catch (CancellationException e) {
			if (BuildConfig.DEBUG) Log.i(TAG, "Cancelled");
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
		
		return mResult;
	}
	
	@Override
	public Float get() {
		mResult = 0.f;
		
		try {
			for (Future<Float> f : mFutures) mResult += f.get();
			mIsDone = true;
		} catch (CancellationException e) {
			if (BuildConfig.DEBUG) Log.i(TAG, "Cancelled");
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
		
		return mResult;
	}
	
	@Override
	public boolean isCancelled() { return mIsCancelled; }
	
	@Override
	public boolean isDone() {
		boolean d = true;
		
		if (!mIsDone && !mIsCancelled) {
			for (Future<Float> f : mFutures) d = d && f.isDone();
		}
		
		return d;
	}
}

