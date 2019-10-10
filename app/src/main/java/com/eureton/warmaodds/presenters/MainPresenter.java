package com.eureton.warmaodds.presenters;

import java.util.Observable;

import com.eureton.warmaodds.models.Input;
import com.eureton.warmaodds.views.Main;

public interface MainPresenter extends Presenter<Main> {
	void boxesChanged(int newValue);
	void focusChanged(int newValue);
	void furyChanged(int newValue);
	void kdsChanged(boolean newValue);
	void toughChanged(boolean newValue);
	void typeChanged(int index, int newValue);
	void matChanged(int index, int newValue);
	void defChanged(int index, int newValue);
	void powChanged(int index, int newValue);
	void armChanged(int index, int newValue);
	void attackDiceChanged(int index, int newValue);
	void damageDiceChanged(int index, int newValue);
	void attackAdded();
	void attackRemoved(int index);
	Observable attacksReset();
	void cancel();
}

