package com.eureton.warmaodds.services;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import rx.Observable;
import rx.Subscription;

import com.eureton.warmaodds.models.State;
import com.eureton.warmaodds.tasks.Task;

public interface TaskManager {

	void submit(Runnable task);
	<V> Future<V> submit(Callable<V> task);
	Subscription submit(Observable<State> task, Task.Listener listener);
	Subscription submitParallel(
		Observable<State> task1,
		Observable<State> task2,
		Task.Merger2 merger,
		Task.Listener listener
	);
	void shutdown();
}

