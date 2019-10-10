package com.eureton.warmaodds.android;

import javax.inject.Inject;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.factories.ProductionModule;
import com.eureton.warmaodds.presenters.MainPresenter;
import com.eureton.warmaodds.presenters.ModifiersPresenter;

import android.util.Log;
import dagger.ObjectGraph;

public class Application extends android.app.Application {

	private static final String TAG = Application.class.getSimpleName();

	@Inject MainPresenter mMainPresenter;
	@Inject ModifiersPresenter mModifiersPresenter;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
		try {
			ProductionModule module =
					new ProductionModule(getApplicationContext());
			ObjectGraph graph = ObjectGraph.create(module);

			mMainPresenter = graph.get(MainPresenter.class);
			mModifiersPresenter = graph.get(ModifiersPresenter.class);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}

	public MainPresenter getMainPresenter() { return mMainPresenter; }

	public ModifiersPresenter getModifiersPresenter() {
		return mModifiersPresenter;
	}
}

