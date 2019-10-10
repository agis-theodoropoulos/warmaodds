package com.eureton.warmaodds.services.impl;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.eureton.warmaodds.services.MathService;

class PermutationGenerator<T> implements MathService.CombiGenerator<T> {
			
	private final List<T> mPool, mResults;
	private final int mSize, mN;
	private final int[] work;
	private final int[] dir;
	private int mIndex;

	PermutationGenerator(List<T> pool, int size, int n) {
		mPool = pool;
		mResults = new LinkedList<T>();
		mSize = size;
		mN = n;
		work = new int[n];
		dir = new int[n];
		
		reset();
	}

	@Override
	public boolean hasNext() { return mIndex < mSize; }

	@Override
	public List<T> next() {
		mResults.clear();
		
 		if (mIndex > 0 && this.hasMobile(work, dir)) {
			final int curMobile = findLargestMobile(work, dir);

			// swap, b = (a += b -= a) - b;
			final int movePos = curMobile + (dir[curMobile] == 0 ? -1 : 1);
			work[movePos] = (work[curMobile] += work[movePos] -= work[curMobile]) - work[movePos];
			dir[movePos] = (dir[curMobile] += dir[movePos] -= dir[curMobile]) - dir[movePos];

			// reverse direction
			for (int i = 0; i < mN; i++) {
				if (work[i] > work[movePos]) dir[i] = dir[i] == 0 ? 1 : 0;
			}
		}

		for (int i : work) mResults.add(mPool.get(i));
		mIndex++;

		return Collections.unmodifiableList(mResults);
	}

	@Override
	public void reset() {
		for (int i = 0; i < mN; i++) {
			work[i] = i;
			dir[i] = 0;
		}
		
		mIndex = 0;
	}
	
	private boolean hasMobile(final int[] work, final int[] dir) {
		for (int i = 0, n = work.length; i < n; i++) {
			if (isMobile(work, dir, i)) return true;
		}
		return false;
	}
	
	private boolean isMobile(final int[] work, final int[] dir, final int i) {
		// leftmost integer pointing to the left is not mobile
		// rightmost integer pointing to the right is not mobile
		if ((i == 0 && dir[i] == 0) || (i == work.length - 1 && dir[i] == 1)) {
			return false;
		}
		// An integer is mobile if, in the direction of its mobility, the
		// nearest integer is less than the current integer.
		if (i > 0 && dir[i] == 0 && work[i] > work[i - 1]) {
			return true;
		}
		if (i < work.length - 1 && dir[i] == 1 && work[i] > work[i + 1]) {
			return true;
		}
		if (i > 0 && i < work.length) {
			if ((dir[i] == 0 && work[i] > work[i - 1]) || (dir[i] == 1 && work[i] > work[i + 1])) {
				return true;
			}
		}

		return false;
	}
	
	private int findLargestMobile(final int[] work, final int[] dir) {
		int largest = -1;
		int pos = -1;
		for (int i = 0, n = work.length; i < n; i++) {
			if (this.isMobile(work, dir, i) && largest < work[i]) {
				largest = work[i];
				pos = i;
			}
		}
		return pos;
	}
}
