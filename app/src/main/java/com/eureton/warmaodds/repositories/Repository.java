package com.eureton.warmaodds.repositories;

public interface Repository<T> {
	
	T get();
	void save(T t);
}

