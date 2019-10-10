package com.eureton.warmaodds.views.impl;

import java.util.LinkedList;
import java.util.List;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.models.AttackStats;
import com.eureton.warmaodds.views.Attack;
import com.eureton.warmaodds.views.AttackView;
import com.eureton.warmaodds.views.AttacksView;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AttacksFragment extends Fragment implements AttacksView {
	
	private static final String TAG = AttacksFragment.class.getSimpleName();
	private static final String INDEX_KEY = TAG + "#index";
	private static final int NONE = -1;
	
	private final int[] mCoordinates;
	private List<Attack> mAttacks;
	private List<AttackView> mAttackViews;
	private ViewGroup mAttackContainer;
	private Attack.AttackEventListener mAttackEventListener;
	private LayoutInflater mInflater;
	private int mIndex;

	public AttacksFragment() {
		super();

		mCoordinates = new int[2];
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_attacks, container, false);
		
		mInflater = inflater;
		mAttackContainer = (ViewGroup) v.findViewById(R.id.LL_ATTACKS);

		mAttacks = new LinkedList<Attack>();
		mAttackViews = new LinkedList<AttackView>();
		mIndex = (savedInstanceState != null) ?
				savedInstanceState.getInt(INDEX_KEY, NONE) :
				NONE;
	
		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle state) {
		super.onActivityCreated(state);

		mAttackEventListener = (Attack.AttackEventListener) getActivity();
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);

		state.putInt(INDEX_KEY, mIndex);
	}
	
	@Override
	public void add(AttackStats attack) {
		View root = mInflater.inflate(R.layout.widget_attack, null, false);
		Attack view = (Attack) root;

		view.setStats(attack.type, attack.mat, attack.def, attack.attackDice,
				attack.pow, attack.arm, attack.damageDice);
		view.setAttackEventListener(mAttackEventListener);
		mAttackContainer.addView(root, mAttacks.size());
		mAttacks.add(view);
		mAttackViews.add(view);

		int count = mAttacks.size();
		switch (count) {
		case 1:
			view.setRemovable(false);
			if (mIndex == NONE) mIndex = 0;
			break;
		case 2:
			mAttacks.get(0).setRemovable(true);
			break;
		}
		if (mIndex < count) setSelection(mIndex);
	}

	@Override
	public boolean remove(int index) {
		boolean found = false;
				
		if (index >= 0 && index < mAttackContainer.getChildCount()) {
			mAttacks.remove(index);
			mAttackViews.remove(index);
			mAttackContainer.removeViewAt(index);
			if (mAttacks.size() == 1) mAttacks.get(0).setRemovable(false);
			found = true;
		}
		
		return found;
	}

	@Override
	public void clear() {
		mAttacks.clear();
		mAttackViews.clear();
		mAttackContainer.removeAllViews();
	}

	public int getAttackCount() { return mAttacks.size(); }
	
	public int getAttackIndex(Attack attack) {
		int i = 0;
		
		for (Attack a : mAttacks) {
			if (attack == a) break;
			i++;
		}
		if (i == mAttacks.size()) throw new IllegalArgumentException();
		
		return i;
	}
	
	public int getAttackIndex(float x, float y) {
		int i = 0;
		
		for (Attack a : mAttacks) {
			View v = (View) a;
			v.getLocationOnScreen(mCoordinates);
			int l = mCoordinates[0];
			int t = mCoordinates[1];
			int w = v.getWidth();
			int h = v.getHeight();

			if (l <= x && x <= l + w && t <= y && y <= t + h) break;
			i++;
		}
		if (i == mAttacks.size()) i = -1;
		
		return i;
	}

	public List<AttackView> getAttackViews() { return mAttackViews; }

	public void setSelection(int index) {
		if (index >= 0 && index < mAttacks.size()) {
			int i = 0;

			for (Attack a : mAttacks) {
				View v = ((View) a).findViewById(R.id.STATS_BODY);

				v.setSelected(i++ == index);
			}
			mIndex = index;
		}
	}
}

