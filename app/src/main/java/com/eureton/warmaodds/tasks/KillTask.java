package com.eureton.warmaodds.tasks;

import java.util.concurrent.Future;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.util.Util;
import com.eureton.warmaodds.views.UiView;

public class KillTask extends Task {
	
	private static final String TAG = KillTask.class.getSimpleName();

	private final WarmachineService mWarmachineService;
	private UiView mUiView;
	private Input mIn;
	private Output mOut;
	private long mStart;
	private long mEnd;
	private boolean mIsCancelled;
	
	public KillTask(WarmachineService warmachineService) {
		mWarmachineService = warmachineService;
	}

	public KillTask withUiView(UiView uiView) {
		if (mStatus == Status.RUNNING && mUiView == null && uiView != null) {
			uiView.showProgress();
		}
		mUiView = uiView;

		return this;
	}
	
	public void cancel() { mIsCancelled = true; }

	@Override
	protected void run() {
		setup();
		
		try {
			process();
		} finally {
			cleanup();
		}
	}
	
	private void setup() {
		mIn = mState.input;
		mOut = mState.output;
		mStart = System.currentTimeMillis();
		if (mUiView != null) {
			mUiView.disableUi();
			mUiView.showProgress();
			mUiView.setProgressStatus(0);
		}
		mIsCancelled = false;
	}
	
	private void process() {
		try {
			Future<Float> f =
					mWarmachineService.killProbability(mIn, new Listener());
					
			while (!f.isDone()) {
				if (mIsCancelled) {
					f.cancel(true);
					throw new CancellationException();
				}
				Thread.sleep(50);
			}
			mOut.kill = f.get();
			mEnd = System.currentTimeMillis();
		} catch (ExecutionException | InterruptedException e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}
	
	private void cleanup() {
		if (mUiView != null) {
			mUiView.hideProgress();
			mUiView.enableUi();
		}
		if (!mIsCancelled) {
			Util.log(TAG, "KILL", mOut.kill * 100, mEnd - mStart);
		} else if (BuildConfig.DEBUG) Log.d(TAG, "Cancelled");
	}
	
	private class Listener implements WarmachineService.ProgressListener {
					
		@Override
		public void onProgress(int status) {
			Util.log(TAG, "PROGRESS", status, 0);
			if (mUiView != null) mUiView.setProgressStatus(status);
		}
	}
}

