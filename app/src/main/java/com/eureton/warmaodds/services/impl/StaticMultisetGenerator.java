package com.eureton.warmaodds.services.impl;

import java.util.LinkedList;
import java.util.List;

import com.eureton.warmaodds.services.MathService;

class StaticMultisetGenerator<T> implements MathService.CombiGenerator<T> {
			
	private final List<T> mPool;
	private final int[][] mIndices;
	private int mIndex;

	StaticMultisetGenerator(List<T> pool, int n, int k) {
		mPool = pool;
		
		int count = pow(n, k);
		mIndices = new int[count][k];
		for (int j = 0; j < k; ++j) {
			int l = 0;
			
			for (int i = 0; i < n && l < count; ++i) {
				for (int m = 0; m < pow(n, j); ++m) mIndices[l++][j] = i;
			}
		}
		
		reset();
	}

	@Override
	public boolean hasNext() { return mIndex < mIndices.length; }

	@Override
	public List<T> next() {
		List<T> combi = new LinkedList<T>();
		
		for (int i : mIndices[mIndex]) combi.add(mPool.get(i));
		mIndex++;

		return combi;
	}

	@Override
	public void reset() { mIndex = 0; }
	
	private int pow(int b, int e) {
		if (e < 0) throw new IllegalArgumentException();
		
		int res = b;
		
		if (e == 0) {
			res = 1;
		} else {
			for (int i = 1; i < e; ++i) res *= b;
		}
		
		return res;
	}
}
