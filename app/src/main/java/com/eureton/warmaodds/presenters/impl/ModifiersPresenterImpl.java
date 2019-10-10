package com.eureton.warmaodds.presenters.impl;

import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Subscription;

import com.squareup.otto.Bus;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.events.ModifiersComplete;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.presenters.ModifiersPresenter;
import com.eureton.warmaodds.repositories.InputRepository;
import com.eureton.warmaodds.repositories.OutputRepository;
import com.eureton.warmaodds.services.FormatterService;
import com.eureton.warmaodds.services.TaskManager;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.tasks.AllButKillTask;
import com.eureton.warmaodds.tasks.KillTask;
import com.eureton.warmaodds.tasks.Task;
import com.eureton.warmaodds.tasks.Merger;
import com.eureton.warmaodds.types.Constants.AttackModifier;
import com.eureton.warmaodds.types.Constants.DamageModifier;
import com.eureton.warmaodds.types.Constants.OnHitModifier;
import com.eureton.warmaodds.types.Constants.OnCritModifier;
import com.eureton.warmaodds.types.Constants.OnKillModifier;
import com.eureton.warmaodds.views.Modifiers;
import com.eureton.warmaodds.views.ModifiersView;
import com.eureton.warmaodds.views.TotalsView;
import com.eureton.warmaodds.views.UiView;

public class ModifiersPresenterImpl implements ModifiersPresenter {

	private static final String TAG =
			ModifiersPresenterImpl.class.getSimpleName();
	private static final String INDEX_KEY = TAG + "#index";

	private final TaskManager mTaskManager;
	private final InputRepository mInputRepository;
	private final OutputRepository mOutputRepository;
	private final FormatterService mFormatterService;
	private final AllButKillTask mAllButKillTask;
	private final KillTask mKillTask;
	private final Merger mMerger;
	private final Bus mBus;
	private final Observable mAllButKillObservable;
	private final Observable mKillObservable;
	private final ModifierChangedCommit mModifierChangedCommit;
	private final Semaphore mSemaphore;
	private ModifiersView mModifiersView;
	private UiView mUiView;
	private TotalsView mTotalsView;
	private Modifiers mView;
	private int mIndex;
	private Input mInput;
	private Output mOutput;
	private boolean mLatchDisplay;
	private Subscription mSubscription;
	
	@Inject
	public ModifiersPresenterImpl(
		TaskManager taskManager,
		InputRepository inputRepository,
		OutputRepository outputRepository,
		FormatterService formatterService,
		WarmachineService warmachineService,
		Merger merger,
		Semaphore semaphore,
		Bus bus
	) {
		mTaskManager = taskManager;
		mInputRepository = inputRepository;
		mOutputRepository = outputRepository;
		mFormatterService = formatterService;
		mMerger = merger;
		mSemaphore = semaphore;
		mBus = bus;
		mAllButKillTask = new AllButKillTask(warmachineService);
		mKillTask = new KillTask(warmachineService);
		mAllButKillObservable = Observable.create(mAllButKillTask);
		mKillObservable = Observable.create(mKillTask);
		mModifierChangedCommit = new ModifierChangedCommit();
	}

	@Override
	public void create() {
		if (BuildConfig.DEBUG) Log.d(TAG, "create");

		mIndex = 0;
		mLatchDisplay = false;
	}

	@Override
	public void restore(Bundle state) {
		if (BuildConfig.DEBUG) Log.d(TAG, "restore");

		if (state != null && state.containsKey(INDEX_KEY)) {
			mIndex = state.getInt(INDEX_KEY, -1);
			indexChanged(mIndex);
		}
	}

	@Override
	public void setAvailable(Modifiers view) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setAvailable");

		mView = view;
		mModifiersView = view.modifiersView;
		mUiView = view.uiView;
		mTotalsView = view.totalsView;
		mInput = mInputRepository.get();
		mOutput = mOutputRepository.get();
		mKillTask.withUiView(mUiView);

		if (mLatchDisplay) {
			if (BuildConfig.DEBUG) Log.d(TAG, "Display latched: " + mIndex);
			displayModifiers();
			displayTotals();
			mLatchDisplay = false;
		}
	}

	@Override
	public void setUnavailable() {
		if (BuildConfig.DEBUG) Log.d(TAG, "setUnavailable");

		mKillTask.withUiView(null);
		mModifiersView = null;
		mUiView = null;
		mTotalsView = null;
		mView = null;
		mInput = null;
		mOutput = null;
	}

	@Override
	public void save(Bundle state) {
		if (BuildConfig.DEBUG) Log.d(TAG, "save");

		state.putInt(INDEX_KEY, mIndex);
	}

	@Override
	public void destroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "destroy");
	}

	@Override
	public Modifiers getView() { return mView; }

	@Override
	public void indexChanged(int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "indexChanged -> " + newValue);

		mIndex = newValue;

		if (mView == null) mLatchDisplay = true;
		displayModifiers();
	}

	@Override
	public void attackModifierChanged(int index, AttackModifier newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "attackModifierChanged");

		State state = new State(mInput, mOutput);
		state.input.attacks[index].attackModifier = newValue;
		modifierChanged(state, index);
	}

	@Override
	public void damageModifierChanged(int index, DamageModifier newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "damageModifierChanged");

		State state = new State(mInput, mOutput);
		state.input.attacks[index].damageModifier = newValue;
		modifierChanged(state, index);
	}

	@Override
	public void onHitModifierChanged(int index, OnHitModifier newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onHitModifierChanged");

		State state = new State(mInput, mOutput);
		state.input.attacks[index].onHitModifier = newValue;
		modifierChanged(state, index);
	}

	@Override
	public void onCritModifierChanged(int index, OnCritModifier newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onCritModifierChanged");

		State state = new State(mInput, mOutput);
		state.input.attacks[index].onCritModifier = newValue;
		modifierChanged(state, index);
	}

	@Override
	public void onKillModifierChanged(int index, OnKillModifier newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onKillModifierChanged");

		State state = new State(mInput, mOutput);
		state.input.attacks[index].onKillModifier = newValue;
		modifierChanged(state, index);
	}

	@Override
	public void onModifiersCleared(int index) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onModifiersCleared");

		State state = new State(mInput, mOutput);
		AttackStats s = state.input.attacks[index];

		s.attackModifier = AttackModifier.NONE;
		s.damageModifier = DamageModifier.NONE;
		s.onHitModifier = OnHitModifier.NONE;
		s.onCritModifier = OnCritModifier.NONE;
		s.onKillModifier = OnKillModifier.NONE;

		modifierChanged(state, index);
	}

	@Override
	public void cancel() {
		if (BuildConfig.DEBUG) Log.d(TAG, "cancel");

		if (!mSemaphore.tryAcquire()) {
			mKillTask.cancel();
			displayModifiers();
		}
	}

	private void displayModifiers() {
		if (mInput != null && mIndex >= 0 && mIndex < mInput.attacks.length) {
			AttackStats s = mInput.attacks[mIndex];

			mModifiersView.setAttack(
					mFormatterService.attackModifier(s.attackModifier));
			mModifiersView.setDamage(
					mFormatterService.damageModifier(s.damageModifier));
			mModifiersView.setOnHit(
					mFormatterService.onHitModifier(s.onHitModifier));
			mModifiersView.setOnCritical(
					mFormatterService.onCritModifier(s.onCritModifier));
			mModifiersView.setOnKill(
					mFormatterService.onKillModifier(s.onKillModifier));
		}
	}

	private void displayTotals() {
		Log.w(TAG, "mOutput: " + (mOutput != null) + ", mTotalsView: " + (mTotalsView != null));
		if (
			mInput != null &&
			mIndex >= 0 &&
			mIndex < mInput.attacks.length &&
			mOutput != null &&
			mTotalsView != null
		) {
			mTotalsView.setCount(
					mFormatterService.count(mInput.attacks.length));
			mTotalsView.setAttackAny(
					mFormatterService.probability(mOutput.hit));
			mTotalsView.setCriticalAny(
					mFormatterService.probability(mOutput.critical));
			mTotalsView.setAttackAll(
					mFormatterService.probability(mOutput.hitAll));
			mTotalsView.setCriticalAll(
					mFormatterService.probability(mOutput.criticalAll));
			mTotalsView.setKill(mFormatterService.probability(mOutput.kill));
		}
	}

	private void modifierChanged(State state, int index) {
		if (mSemaphore.tryAcquire()) {
			mAllButKillTask.withState(state);
			mKillTask.withState(state);
			
			mSubscription = mTaskManager.submitParallel(
				mAllButKillObservable,
				mKillObservable,
				mMerger,
				mModifierChangedCommit.withIndex(index)
			);
		}
	}

	private class ModifierChangedCommit implements Task.Listener {
		
		private int mIndex;
		
		ModifierChangedCommit withIndex(int index) {
			mIndex = index;

			return this;
		}

		@Override
		public void onCompleted() {
			release();
			if (BuildConfig.DEBUG) Log.d(TAG, "Task complete");
		}

		@Override
		public void onError(Throwable e) {
			release();
			if (BuildConfig.DEBUG) {
				if (e instanceof CancellationException) {
					Log.i(TAG, "Cancelled");
				} else {
					Log.e(TAG, e.getMessage(), e);
				}
			}
		}

		@Override
		public void onNext(State state) {
			state.input.copyTo(mInput);
			state.output.copyTo(mOutput);
			displayModifiers();
			displayTotals();
			mBus.post(new ModifiersComplete(mIndex));
		}

		private void release() {
			mSubscription.unsubscribe();
			mSemaphore.release();
		}
	}
}

