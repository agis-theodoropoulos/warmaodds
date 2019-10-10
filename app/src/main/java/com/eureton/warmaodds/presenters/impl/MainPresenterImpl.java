package com.eureton.warmaodds.presenters.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Semaphore;

import javax.inject.Inject;

import android.os.Bundle;
import android.util.Log;

import rx.Observable;
import rx.Subscription;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.events.ModifiersComplete;
import com.eureton.warmaodds.models.AttackResults;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.presenters.MainPresenter;
import com.eureton.warmaodds.repositories.InputRepository;
import com.eureton.warmaodds.repositories.OutputRepository;
import com.eureton.warmaodds.services.DefaultsService;
import com.eureton.warmaodds.services.DiceService;
import com.eureton.warmaodds.services.FormatterService;
import com.eureton.warmaodds.services.TaskManager;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.tasks.AllButKillTask;
import com.eureton.warmaodds.tasks.KillTask;
import com.eureton.warmaodds.tasks.Task;
import com.eureton.warmaodds.tasks.Merger;
import com.eureton.warmaodds.views.AttackView;
import com.eureton.warmaodds.views.AttacksView;
import com.eureton.warmaodds.views.GlobalsView;
import com.eureton.warmaodds.views.Main;
import com.eureton.warmaodds.views.TotalsView;
import com.eureton.warmaodds.views.UiView;

public class MainPresenterImpl implements MainPresenter {

	private static final String TAG = MainPresenterImpl.class.getSimpleName();
	private static final String INPUT_KEY = TAG + "#input";
	private static final String OUTPUT_KEY = TAG + "#output";
	private static final int NO_INDEX = -1;

	private final TaskManager mTaskManager;
	private final WarmachineService mWarmachineService;
	private final FormatterService mFormatterService;
	private final DefaultsService mDefaultsService;
	private final InputRepository mInputRepository;
	private final OutputRepository mOutputRepository;
	private final Semaphore mSemaphore;

	private final AllButKillTask mAllButKillTask;
	private final KillTask mKillTask;
	private final Observable mAllButKillObservable;
	private final Observable mKillObservable;

	private final Merger mMerger;

	private final BoxesChangedCommit mBoxesChangedCommit;
	private final FocusChangedCommit mFocusChangedCommit;
	private final FuryChangedCommit mFuryChangedCommit;
	private final KdsChangedCommit mKdsChangedCommit;
	private final ToughChangedCommit mToughChangedCommit;
	private final MatChangedCommit mMatChangedCommit;
	private final DefChangedCommit mDefChangedCommit;
	private final PowChangedCommit mPowChangedCommit;
	private final ArmChangedCommit mArmChangedCommit;
	private final AttackDiceChangedCommit mAttackDiceChangedCommit;
	private final DamageDiceChangedCommit mDamageDiceChangedCommit;
	private final AttackAddedCommit mAttackAddedCommit;
	private final AttackRemovedCommit mAttackRemovedCommit;
	private final AttacksResetCommit mAttacksResetCommit;
	private boolean mCreated;
	private Main mMain;
	private AttacksView mAttacksView;
	private List<AttackView> mAttackViews;
	private GlobalsView mGlobalsView;
	private UiView mUiView;
	private TotalsView mTotalsView;
	private Input mInput;
	private Output mOutput;
	private Subscription mSubscription;
	private TaskObservable mTaskObservable;
	private int mLastIndex;
	
	@Inject
	public MainPresenterImpl(
		TaskManager taskManager,
		DiceService diceService,
		WarmachineService warmachineService,
		FormatterService formatterService,
		DefaultsService defaultsService,
		InputRepository inputRepository,
		OutputRepository outputRepository,
		Merger merger,
		Semaphore semaphore,
		Bus bus
	) {
		mTaskManager = taskManager;
		mWarmachineService = warmachineService;
		mFormatterService = formatterService;
		mDefaultsService = defaultsService;
		mInputRepository = inputRepository;
		mOutputRepository = outputRepository;
		mSemaphore = semaphore;

		mAllButKillTask = new AllButKillTask(mWarmachineService);
		mKillTask = new KillTask(mWarmachineService);
		mAllButKillObservable = Observable.create(mAllButKillTask);
		mKillObservable = Observable.create(mKillTask);

		mMerger = merger;

		mBoxesChangedCommit = new BoxesChangedCommit();
		mFocusChangedCommit = new FocusChangedCommit();
		mFuryChangedCommit = new FuryChangedCommit();
		mKdsChangedCommit = new KdsChangedCommit();
		mToughChangedCommit = new ToughChangedCommit();
		mMatChangedCommit = new MatChangedCommit();
		mDefChangedCommit = new DefChangedCommit();
		mPowChangedCommit = new PowChangedCommit();
		mArmChangedCommit = new ArmChangedCommit();
		mAttackDiceChangedCommit = new AttackDiceChangedCommit();
		mDamageDiceChangedCommit = new DamageDiceChangedCommit();
		mAttackAddedCommit = new AttackAddedCommit();
		mAttackRemovedCommit = new AttackRemovedCommit();
		mAttacksResetCommit = new AttacksResetCommit();
		bus.register(this);
		mLastIndex = NO_INDEX;
	}

	@Override
	public void create() {
		if (BuildConfig.DEBUG) Log.d(TAG, "create");

		mCreated = true;
	}

	@Override
	public void restore(Bundle state) {
		if (BuildConfig.DEBUG) Log.d(TAG, "restore");

		if (state != null && state.containsKey(INPUT_KEY)) {
			mInput = state.<Input>getParcelable(INPUT_KEY);
			mInputRepository.save(mInput);
		}
		if (state != null && state.containsKey(OUTPUT_KEY)) {
			mOutput = state.<Output>getParcelable(OUTPUT_KEY);
			mOutputRepository.save(mOutput);
		}
	}

	@Override
	public void setAvailable(Main view) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setAvailable");

		mInput = mInputRepository.get();
		mOutput = mOutputRepository.get();

		mAttacksView = view.attacksView;
		mAttackViews = view.attackViews;
		mGlobalsView = view.globalsView;
		mUiView = view.uiView;
		mTotalsView = view.totals;
		mKillTask.withUiView(mUiView);
		mMain = view;
		
		for (int i = 0; i < mInput.attacks.length; ++i) {
			if (mCreated) mAttacksView.add(mInput.attacks[i]);
			displayAttackKeys(i);
			displayAttackModificationStatus(i);
		}
		displayDamageKeys();
		displayCount();
		displayHitTotals();
		displayDamageTotals();
		displayBoxes();
		displayFocus();
		displayFury();
		displayTough();

		mCreated = false;
	}

	@Override
	public void setUnavailable() {
		if (BuildConfig.DEBUG) Log.d(TAG, "setUnavailable");

		mAttackViews = null;
		mGlobalsView = null;
		mTotalsView = null;
		mMain = null;
		mKillTask.withUiView(null);
	}

	@Override
	public void save(Bundle state) {
		if (BuildConfig.DEBUG) Log.d(TAG, "save");

		state.putParcelable(INPUT_KEY, mInput);
		state.putParcelable(OUTPUT_KEY, mOutput);
	}

	@Override
	public void destroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "destroy");
	}

	@Override
	public Main getView() { return mMain; }

	@Override
	public void boxesChanged(int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "boxesChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.boxes = newValue;

			submit(state, mBoxesChangedCommit);
		}
	}

	@Override
	public void focusChanged(int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "focusChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.focus = newValue;
			state.input.fury = 0;

			submit(state, mFocusChangedCommit);
		}
	}

	@Override
	public void furyChanged(int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "furyChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.fury = newValue;
			state.input.focus = 0;

			submit(state, mFuryChangedCommit);
		}
	}

	@Override
	public void kdsChanged(boolean newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "kdsChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.kds = newValue;

			submit(state, mKdsChangedCommit);
		}
	}

	@Override
	public void toughChanged(boolean newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "toughChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.tough = newValue;

			submit(state, mToughChangedCommit);
		}
	}

	@Override
	public void typeChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "typeChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].type = newValue;

			submit(state, mMatChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void matChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "matChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].mat = newValue;

			submit(state, mMatChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void defChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "defChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].def = newValue;

			submit(state, mDefChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void powChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "powChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].pow = newValue;

			submit(state, mPowChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void armChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "armChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].arm = newValue;

			submit(state, mArmChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void attackDiceChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "attackDiceChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].attackDice = newValue;

			submit(state, mAttackDiceChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void damageDiceChanged(int index, int newValue) {
		if (BuildConfig.DEBUG) Log.d(TAG, "damageDiceChanged -> " + newValue);

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.attacks[index].damageDice = newValue;

			submit(state, mDamageDiceChangedCommit.withIndex(index));
			mLastIndex = index;
		}
	}

	@Override
	public void attackAdded() {
		if (BuildConfig.DEBUG) Log.d(TAG, "attackAdded");

		if (mSemaphore.tryAcquire()) {
			int count = mInput.attacks.length;
			State state = new State(mInput, mOutput);
			AttackStats stats = (count > 0) ?
					new AttackStats(mInput.attacks[count - 1]) :
					mDefaultsService.attackStats();
			AttackResults results = (count > 0) ?
					new AttackResults(mOutput.attackResults.get(count - 1)) :
					mDefaultsService.attackResults();

			state.input.add(stats);
			state.output.attackResults.add(results);

			submit(state, mAttackAddedCommit.withIndex(count));
		}
	}

	@Override
	public void attackRemoved(int index) {
		if (BuildConfig.DEBUG) Log.d(TAG, "attackRemoved");

		if (mSemaphore.tryAcquire()) {
			State state = new State(mInput, mOutput);
			state.input.remove(index);
			state.output.attackResults.remove(index);

			submit(state, mAttackRemovedCommit.withIndex(index));
		}
	}

	@Override
	public java.util.Observable attacksReset() {
		if (BuildConfig.DEBUG) Log.d(TAG, "attacksReset");

		if (mSemaphore.tryAcquire()) {
			AttackStats s = mDefaultsService.attackStats();
			int count = mInput.attacks.length;
			if (count > 0) s.copyTargetStatsFrom(mInput.attacks[count - 1]);
			State state =
				new State(mDefaultsService.input(), mDefaultsService.output());

			state.input.add(s);
			state.input.boxes = mInput.boxes;
			state.input.focus = mInput.focus;
			state.input.fury = mInput.fury;
			state.input.kds = mInput.kds;
			state.input.tough = mInput.tough;
			state.output.attackResults.add(mDefaultsService.attackResults());

			submit(state, mAttacksResetCommit);
			mTaskObservable = new TaskObservable();
		}

		return mTaskObservable;
	}

	@Override
	public void cancel() {
		if (BuildConfig.DEBUG) Log.d(TAG, "cancel");

		if (!mSemaphore.tryAcquire()) {
			mKillTask.cancel();

			if (mLastIndex != NO_INDEX) {
				AttackStats s = mInput.attacks[mLastIndex];

				mAttackViews.get(mLastIndex).setStats(s.type, s.mat, s.def,
						s.attackDice, s.pow, s.arm, s.damageDice);
				mLastIndex = NO_INDEX;
			}
			mGlobalsView.setTough(mInput.tough);
			mGlobalsView.setKnockdownStationary(mInput.kds);
		}
	}

	@Subscribe
	public void modifiersComplete(ModifiersComplete e) {
		if (BuildConfig.DEBUG) Log.d(TAG, "modifiersComplete(" + e.index + ")");

		if (mMain != null) {
			displayAttackKeys(e.index);
			displayAttackModificationStatus(e.index);
			displayDamageKeys();
			displayHitTotals();
			displayDamageTotals();
			displayCount();
		}
	}

	private void submit(State state, Commit commit) {
		// have tasks work on different data to avoid concurrency issues
		State state1 = state;
		State state2 = new State(state);

		mAllButKillTask.withState(state1);
		mKillTask.withState(state2);

		mSubscription = mTaskManager.submitParallel(
			mAllButKillObservable,
			mKillObservable,
			mMerger,
			commit
		);
	}

	private void displayBoxes() {
		mGlobalsView.setBoxes(mFormatterService.boxes(mInput.boxes));
	}
	
	private void displayFocus() {
		mGlobalsView.setFocus(mFormatterService.focus(mInput.focus));
	}
	
	private void displayFury() {
		mGlobalsView.setFury(mFormatterService.transfers(mInput.fury));
	}
	
	private void displayTough() {
		mGlobalsView.setTough(mInput.tough);
	}
	
	private void displayCount() {
		mTotalsView.setCount(mFormatterService.count(mInput.attacks.length));
	}
	
	private void displayAttackKeys(int index) {
		AttackView v = mAttackViews.get(index);
		AttackResults r = mOutput.attackResults.get(index);

		v.setHit(mFormatterService.probability(r.hit));
		v.setCritical(mFormatterService.probability(r.critical));
	}
	
	private void displayAttackModificationStatus(int index) {
		AttackStats s = mInput.attacks[index];
		AttackView v = mAttackViews.get(index);

		v.setModified(s.hasModifiers());
	}
	
	private void displayHitTotals() {
		mTotalsView.setAttackAny(mFormatterService.probability(mOutput.hit));
		mTotalsView.setCriticalAny(
				mFormatterService.probability(mOutput.critical));
		mTotalsView.setAttackAll(mFormatterService.probability(mOutput.hitAll));
		mTotalsView.setCriticalAll(
				mFormatterService.probability(mOutput.criticalAll));
	}

	private void displayDamageTotals() {
		mTotalsView.setKill(mFormatterService.probability(mOutput.kill));
	}

	private void displayDamageKeys() {
		for (int i = 0; i < mInput.attacks.length; ++i) {
			AttackView v = mAttackViews.get(i);
			AttackResults r = mOutput.attackResults.get(i);

			v.setDamage(mFormatterService.damage(r.averageDamage));
		}
	}

	private abstract class Commit implements Task.Listener {

		@Override
		public void onCompleted() {
			if (mTaskObservable != null) {
				mTaskObservable.fire();
				mTaskObservable = null;
			}
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

		protected void commitState(State state) {
			state.input.copyTo(mInput);
			state.output.copyTo(mOutput);
		}

		private void release() {
			mSubscription.unsubscribe();
			mSemaphore.release();
		}
	}

	private class BoxesChangedCommit extends Commit {
		
		@Override
		public void onNext(State state) {
			commitState(state);
			displayHitTotals();
			displayDamageTotals();
			displayBoxes();
		}
	}

	private class FocusChangedCommit extends Commit {
		
		@Override
		public void onNext(State state) {
			commitState(state);
			displayDamageKeys();
			displayHitTotals();
			displayDamageTotals();
			displayFocus();
			displayFury();
		}
	}

	private class FuryChangedCommit extends Commit {
		
		@Override
		public void onNext(State state) {
			commitState(state);
			displayDamageKeys();
			displayHitTotals();
			displayDamageTotals();
			displayFury();
			displayFocus();
		}
	}

	private class KdsChangedCommit extends PowChangedCommit { }

	private class ToughChangedCommit extends PowChangedCommit { }

	private class MatChangedCommit extends Commit {
		
		private int mIndex;

		Commit withIndex(int index) {
			mIndex = index;

			return this;
		}

		@Override
		public void onNext(State state) {
			commitState(state);
			displayAttackKeys(mIndex);
			displayHitTotals();
			displayDamageTotals();
		}
	}

	private class DefChangedCommit extends MatChangedCommit { }

	private class AttackDiceChangedCommit extends MatChangedCommit { }

	private class PowChangedCommit extends Commit {
		
		private int mIndex;
		
		Commit withIndex(int index) {
			mIndex = index;

			return this;
		}

		@Override
		public void onNext(State state) {
			commitState(state);
			displayDamageKeys();
			displayDamageTotals();
		}
	}

	private class ArmChangedCommit extends PowChangedCommit { }

	private class DamageDiceChangedCommit extends PowChangedCommit { }

	private class AttackAddedCommit extends Commit {
		
		private int mIndex;
		
		AttackAddedCommit withIndex(int index) {
			mIndex = index;

			return this;
		}

		@Override
		public void onNext(State state) {
			commitState(state);
			mAttacksView.add(mInput.attacks[mIndex]);
			displayAttackKeys(mIndex);
			displayAttackModificationStatus(mIndex);
			displayDamageKeys();
			displayHitTotals();
			displayDamageTotals();
			displayCount();
		}
	}

	private class AttackRemovedCommit extends Commit {
		
		private int mIndex;
		
		AttackRemovedCommit withIndex(int index) {
			mIndex = index;

			return this;
		}

		@Override
		public void onNext(State state) {
			commitState(state);
			mAttacksView.remove(mIndex);
			displayDamageKeys();
			displayHitTotals();
			displayDamageTotals();
			displayCount();
		}
	}

	private class AttacksResetCommit extends Commit {
		
		@Override
		public void onNext(State state) {
			commitState(state);
			mAttacksView.clear();
			mAttacksView.add(mInput.attacks[0]);
			displayAttackKeys(0);
			displayAttackModificationStatus(0);
			displayDamageKeys();
			displayHitTotals();
			displayDamageTotals();
			displayCount();
			displayBoxes();
			displayFocus();
			displayFury();
			displayTough();
		}
	}

	private class TaskObservable extends java.util.Observable {

		public void fire() {
			setChanged();
			notifyObservers(null);
		}
	}
}

