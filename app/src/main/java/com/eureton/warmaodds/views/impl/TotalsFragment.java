package com.eureton.warmaodds.views.impl;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.util.Util;
import com.eureton.warmaodds.views.TotalsView;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class TotalsFragment extends Fragment implements TotalsView {
	
	private static final String TAG = TotalsFragment.class.getSimpleName();
	
	@Bind(R.id.RESULTS_TV_COUNT) TextView mCount;
	@Bind(R.id.RESULTS_TV_KILL) TextView mKill;
	@Bind(R.id.RESULTS_TV_ATTACK_ANY) TextView mHitAny;
	@Bind(R.id.RESULTS_TV_CRITICAL_ANY) TextView mCriticalAny;
	@Bind(R.id.RESULTS_TV_ATTACK_ALL) TextView mHitAll;
	@Bind(R.id.RESULTS_TV_CRITICAL_ALL) TextView mCriticalAll;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_totals, container,
				false);
		
		ButterKnife.bind(this, rootView);

		return rootView;
	}
	
	@Override
	public void onDestroyView() {
		ButterKnife.unbind(this);
		super.onDestroyView();
	}

	@Override
	public void setCount(String s) { set(mCount, s, "COUNT"); }

	@Override
	public void setAttackAny(String s) { set(mHitAny, s, "HIT_ANY"); }

	@Override
	public void setCriticalAny(String s) { set(mCriticalAny, s, "CRIT_ANY"); }

	@Override
	public void setAttackAll(String s) { set(mHitAll, s, "HIT_ALL"); }

	@Override
	public void setCriticalAll(String s) { set(mCriticalAll, s, "CRIT_ALL"); }

	@Override
	public void setKill(String s) { set(mKill, s, "KILL"); }

	private void set(TextView textView, String text, String name) {
		final TextView v = textView;
		final String s = text;
		final String t = name;

		runViewOperation(new Runnable() {
			
			@Override
			public void run() {
				v.setText(s);
				if (BuildConfig.DEBUG) Log.d(TAG, t + ": " + s);
			}
		});
	}

	private void runViewOperation(Runnable r) {
		if (Util.isUiThread()) {
			r.run();
		} else {
			getActivity().runOnUiThread(r);
		}
	}
}

