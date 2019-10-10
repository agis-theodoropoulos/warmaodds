package com.eureton.warmaodds.presenters;

import android.os.Bundle;

import com.eureton.warmaodds.types.Transient;

interface Presenter<T> extends Transient<T> {

	T getView();
}

