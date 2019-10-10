package com.eureton.warmaodds.repositories.impl;

import javax.inject.Inject;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.repositories.InputRepository;
import com.eureton.warmaodds.services.DefaultsService;

public class MemoryInputRepository implements InputRepository {

	private static final String TAG =
			MemoryInputRepository.class.getSimpleName();
	
	private Input mInput;

	@Inject
	public MemoryInputRepository(DefaultsService defaultsService) {
		mInput = defaultsService.input();
	}
	
	@Override
	public Input get() {
		if (BuildConfig.DEBUG) Log.d(TAG, "get");

		return mInput;
	}

	@Override
	public void save(Input input) {
		if (BuildConfig.DEBUG) Log.d(TAG, "save");

		mInput = input;
	}
}

