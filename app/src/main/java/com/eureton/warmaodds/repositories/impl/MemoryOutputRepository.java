package com.eureton.warmaodds.repositories.impl;

import javax.inject.Inject;

import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.models.Output;
import com.eureton.warmaodds.repositories.OutputRepository;
import com.eureton.warmaodds.services.DefaultsService;

public class MemoryOutputRepository implements OutputRepository {

	private static final String TAG =
			MemoryOutputRepository.class.getSimpleName();
	
	private Output mOutput;

	@Inject
	public MemoryOutputRepository(DefaultsService defaultsService) {
		mOutput = defaultsService.output();
	}
	
	@Override
	public Output get() {
		if (BuildConfig.DEBUG) Log.d(TAG, "get");

		return mOutput;
	}

	@Override
	public void save(Output output) {
		if (BuildConfig.DEBUG) Log.d(TAG, "save");

		mOutput = output;
	}
}

