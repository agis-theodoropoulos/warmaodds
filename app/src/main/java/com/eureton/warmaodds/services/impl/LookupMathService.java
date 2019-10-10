package com.eureton.warmaodds.services.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.eureton.warmaodds.services.MathService;

public class LookupMathService implements MathService {

	@Override
	public long factorial(int n) {
		long result = 1;
		
		switch (n) {
		case  0:
		case  1: result = 					1L; break;
		case  2: result =       			2L; break;
		case  3: result =       			6L; break;
		case  4: result =      			   24L; break;
		case  5: result =     			  120L; break;
		case  6: result =     			  720L; break;
		case  7: result =    			 5040L; break;
		case  8: result =   			40320L; break;
		case  9: result =  			   362880L; break;
		case 10: result = 			  3628800L; break;
		case 11: result = 			 39916800L; break;
		default: result = n * factorial(n - 1); break;
		}

		return result;
	}

	@Override
	public long msetPermutations(int n, int m) {
		return factorial(n) / factorial(m);
	}

	@Override
	public long msetPermutations(int n, int m1, int m2) {
		return factorial(n) / (factorial(m1) * factorial(m2));
	}

	@Override
	public long msetPermutations(int n, int m1, int m2, int m3) {
		return factorial(n) / (factorial(m1) * factorial(m2) * factorial(m3));
	}

	@Override
	public long msetPermutations(int n, int m1, int m2, int m3, int m4) {
		return factorial(n) /
				(factorial(m1) * factorial(m2) * factorial(m3) * factorial(m4));
	}

	@Override
	public long msetPermutations(int n, int m1, int m2, int m3, int m4,
			int m5) {
		return factorial(n) / (
			factorial(m1) *
			factorial(m2) *
			factorial(m3) *
			factorial(m4) *
			factorial(m5)
		);
	}

	@Override
	public long msetPermutations(int n, int m1, int m2, int m3, int m4, int m5,
			int m6) {
		return factorial(n) / (
			factorial(m1) *
			factorial(m2) *
			factorial(m3) *
			factorial(m4) *
			factorial(m5) *
			factorial(m6)
		);
	}

	@Override
	public long msetPermutations(int n, int[] m, int count) {
		long denom = 1;
		
		for (int i = 0; i < count; ++i) denom *= factorial(m[i]);

		return factorial(n) / denom;
	}

	@Override
	public long combinations(int n, int k) {
		long result = 1;
		
		if (k < n - k) k = n - k;

		for (int i = n; i > k; --i) result *= i;
		result /= factorial(n - k);

		return result;
	}

	@Override
	public long repCombinations(int n, int k) {
		return combinations(n + k - 1, k);
	}

	@Override
	public <T> CombiGenerator<T> createCombiGenerator(List<T> pool, int k) {
		return new StaticCombiGenerator<T>(
			pool,
			combinations(pool.size(), k),
			k
		);
	}

	@Override
	public <T> CombiGenerator<T> createMsetGenerator(List<T> pool, int n,
			int k) {
		return new StaticMultisetGenerator<T>(pool, n, k);
	}

	@Override
	public <T> CombiGenerator<T> createPermutationGenerator(List<T> pool,
			int n) {
		return new PermutationGenerator<T>(pool, (int) factorial(n), n);
	}

	@Override
	public float disjoin(List<Float> items) {
		float r = 0.f;
		int f = 1;
		
		for (int i = 1; i <= items.size(); ++i) {
			CombiGenerator<Float> g = createCombiGenerator(items, i);
			
			while (g.hasNext()) {
				List<Float> c = g.next();
				Iterator<Float> it = c.iterator();
				float p = 1.f;

				while (it.hasNext()) p *= it.next();
				r += f * p;
			}
			f *= -1;
		}
		
		return r;
	}
}

