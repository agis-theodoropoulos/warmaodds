package com.eureton.warmaodds.tasks;

import com.eureton.warmaodds.models.State;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.functions.Func4;

abstract public class Task implements Observable.OnSubscribe<State> {
	
	protected State mState;
	protected Status mStatus;
	
	public Task withState(State state) {
		mState = state;

		return this;
	}
	
	@Override
	public void call(Subscriber<? super State> subscriber) {
		try {
			mStatus = Status.RUNNING;
			run();
			
			subscriber.onNext(mState);
			mStatus = Status.STOPPED_OK;
			subscriber.onCompleted();
		} catch (Exception e) {
			mStatus = Status.STOPPED_ERROR;
			subscriber.onError(e);
		}
	}

	abstract protected void run();

	protected enum Status {
		STOPPED_OK,
		STOPPED_ERROR,
		RUNNING
	}
	
	public interface Listener extends Observer<State> { }
	
	public interface Merger2 extends Func2<State, State, State> { }
	
	public interface Merger3 extends Func3<State, State, State, State> { }
	
	public interface Merger4 extends
			Func4<State, State, State, State, State> { }
}
