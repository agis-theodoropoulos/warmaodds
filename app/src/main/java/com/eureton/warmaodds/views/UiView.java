package com.eureton.warmaodds.views;

public interface UiView {
	void enableUi();
	void disableUi();
	void showProgress();
	void hideProgress();
	void setProgressStatus(int status);
}

