package com.eureton.warmaodds.services;

import java.util.List;

public interface MathService {

	long factorial(int n);

	long msetPermutations(int n, int m);
	long msetPermutations(int n, int m1, int m2);
	long msetPermutations(int n, int m1, int m2, int m3);
	long msetPermutations(int n, int m1, int m2, int m3, int m4);
	long msetPermutations(int n, int m1, int m2, int m3, int m4, int m5);
	long msetPermutations(int n, int m1, int m2, int m3, int m4, int m5, int m6);
	long msetPermutations(int n, int[] m, int count);
	
	long combinations(int n, int k);
	long repCombinations(int n, int k);
	
	<T> CombiGenerator<T> createCombiGenerator(List<T> pool, int k);
	<T> CombiGenerator<T> createMsetGenerator(List<T> pool, int n, int k);
	<T> CombiGenerator<T> createPermutationGenerator(List<T> pool, int n);

	float disjoin(List<Float> items);
	
	public interface CombiGenerator<T> {
		
		boolean hasNext();
		List<T> next();
		void reset();
	}
}
