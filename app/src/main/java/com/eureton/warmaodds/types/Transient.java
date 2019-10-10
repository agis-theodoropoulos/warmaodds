package com.eureton.warmaodds.types;

import android.os.Bundle;

public interface Transient<T> {

	void create();
	void restore(Bundle state);
	void setAvailable(T t);

	void setUnavailable();
	void save(Bundle state);
	void destroy();
}

