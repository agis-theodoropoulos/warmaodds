package com.eureton.warmaodds.services.impl;

import java.util.LinkedList;
import java.util.List;

import com.eureton.warmaodds.services.MathService.CombiGenerator;

public class StaticCombiGenerator<T> implements CombiGenerator<T> {
	private final List<T> mPool;
	private final int[][] mIndices;
	private int mIndex;

	StaticCombiGenerator(List<T> pool, long count, int k) {
		if (count > Integer.MAX_VALUE) throw new IllegalArgumentException();

		int bufSize = (int) count;
		int poolSize = pool.size();
		mPool = pool;
		mIndices = new int[bufSize][k];
		for (int i = 0; i < k; ++i) mIndices[0][i] = i;

		for (int i = 1; i < bufSize; ++i) {
			for (int j = k - 1; j >= 0; --j) {
				boolean flip = false;

				System.arraycopy(mIndices[i - 1], 0, mIndices[i], 0, k);
				mIndices[i][j]++;
				for (int l = j + 1; l < k; ++l) {
					mIndices[i][l] = mIndices[i][l - 1] + 1;
					if (mIndices[i][l] == poolSize) flip = true;
				}

				if (mIndices[i][j] < poolSize && !flip) break;
			}
		}
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
}
