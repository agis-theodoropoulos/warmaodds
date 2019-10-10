package com.eureton.warmaodds.services.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.services.TaskManager;
import com.eureton.warmaodds.tasks.Task;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ThreadPoolTaskManager implements TaskManager {
	private final BlockingQueue<Runnable> mQueue;
	private final ThreadPoolExecutor mExecutor;
	
	public ThreadPoolTaskManager() {
		int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

		mQueue = new LinkedBlockingQueue<Runnable>();
		mExecutor = new ThreadPoolExecutor(
			NUMBER_OF_CORES,
			NUMBER_OF_CORES,
			60L,
			TimeUnit.SECONDS,
			mQueue
		);
	}

	@Override
	public void submit(Runnable task) {
		mExecutor.execute(task);
	}

	@Override
	public <V> Future<V> submit(Callable<V> task) {
		return mExecutor.submit(task);
	}

	@Override
	public Subscription submit(Observable<State> task, Task.Listener listener) {
		return task.
				subscribeOn(Schedulers.from(mExecutor)).
				observeOn(AndroidSchedulers.mainThread()).
				subscribe(listener);
	}

	@Override
	public Subscription submitParallel(
		Observable<State> task1,
		Observable<State> task2,
		Task.Merger2 merger,
		Task.Listener listener
	) {
		return Observable.zip(
			task1.subscribeOn(Schedulers.from(mExecutor)),
			task2.subscribeOn(Schedulers.from(mExecutor)),
			merger
		).observeOn(AndroidSchedulers.mainThread()).subscribe(listener);
	}

	@Override
	public void shutdown() {
		mExecutor.shutdown();
	}
}

