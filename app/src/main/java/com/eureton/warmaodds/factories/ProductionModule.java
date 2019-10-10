package com.eureton.warmaodds.factories;

import java.util.concurrent.Semaphore;

import javax.inject.Singleton;

import com.squareup.otto.Bus;

import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.presenters.MainPresenter;
import com.eureton.warmaodds.presenters.ModifiersPresenter;
import com.eureton.warmaodds.presenters.impl.MainPresenterImpl;
import com.eureton.warmaodds.presenters.impl.ModifiersPresenterImpl;
import com.eureton.warmaodds.repositories.InputRepository;
import com.eureton.warmaodds.repositories.OutputRepository;
import com.eureton.warmaodds.repositories.impl.MemoryInputRepository;
import com.eureton.warmaodds.repositories.impl.MemoryOutputRepository;
import com.eureton.warmaodds.services.DefaultsService;
import com.eureton.warmaodds.services.DiceService;
import com.eureton.warmaodds.services.FormatterService;
import com.eureton.warmaodds.services.MathService;
import com.eureton.warmaodds.services.ModifierService;
import com.eureton.warmaodds.services.RangeService;
import com.eureton.warmaodds.services.TaskManager;
import com.eureton.warmaodds.services.UtilService;
import com.eureton.warmaodds.services.WarmachineService;
import com.eureton.warmaodds.services.impl.DefaultsServiceImpl;
import com.eureton.warmaodds.services.impl.LookupDiceService;
import com.eureton.warmaodds.services.impl.LookupMathService;
import com.eureton.warmaodds.services.impl.ModifierServiceImpl;
import com.eureton.warmaodds.services.impl.PercentageFormatter;
import com.eureton.warmaodds.services.impl.RangeServiceImpl;
import com.eureton.warmaodds.services.impl.ThreadPoolTaskManager;
import com.eureton.warmaodds.services.impl.UtilServiceImpl;
import com.eureton.warmaodds.services.impl.WarmachineServiceImpl;
import com.eureton.warmaodds.tasks.Merger;

import android.content.Context;
import dagger.Module;
import dagger.Provides;

@Module(injects = {
	MainPresenter.class,
	ModifiersPresenter.class
})
public class ProductionModule {

	private final Context mContext;
	
	public ProductionModule(Context context) { mContext = context; }

	@Provides
	Merger provideMerger() { return new Merger(); }
	
	@Singleton
	@Provides
	FormatterService provideFormatterService() {
		return new PercentageFormatter();
	}
	
	@Singleton
	@Provides
	TaskManager provideTaskManager() { return new ThreadPoolTaskManager(); }
	
	@Singleton
	@Provides
	MathService provideMathService() { return new LookupMathService(); }
	
	@Singleton
	@Provides
	DiceService provideDiceService() { return new LookupDiceService(); }
	
	@Singleton
	@Provides
	RangeService provideRangeService() { return new RangeServiceImpl(); }
	
	@Singleton
	@Provides
	InputRepository provideInputRepository(MemoryInputRepository repository) {
		return repository;
	}
	
	@Singleton
	@Provides
	OutputRepository provideOutputRepository(
			MemoryOutputRepository repository) {
		return repository;
	}
	
	@Singleton
	@Provides
	WarmachineService provideWarmachineService(
			WarmachineServiceImpl warmachineService) {
		return warmachineService;
	}

	@Provides
	Context provideContext() { return mContext; }
	
	@Singleton
	@Provides
	DefaultsService provideDefaultsService(
			DefaultsServiceImpl defaultsService) {
		return defaultsService;
	}
	
	@Singleton
	@Provides
	UtilService provideUtilService(UtilServiceImpl utilService) {
		return utilService;
	}
	
	@Singleton
	@Provides
	ModifierService provideModifierService(
			ModifierServiceImpl modifierService) {
		return modifierService;
	}
	
	@Provides
	@Singleton
	Bus provideBus() { return new Bus(); }
	
	@Provides
	@Singleton
	Semaphore provideSemaphore() { return new Semaphore(1); }

	@Provides
	MainPresenter provideMainPresenter(MainPresenterImpl presenter) {
		return presenter;
	}
	
	@Provides
	ModifiersPresenter provideMainPresenter(ModifiersPresenterImpl presenter) {
		return presenter;
	}
}

