package com.eureton.warmaodds.views.impl;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.android.Application;
import com.eureton.warmaodds.presenters.ModifiersPresenter;
import com.eureton.warmaodds.types.Constants;
import com.eureton.warmaodds.views.Modifiers;
import com.eureton.warmaodds.views.ModifiersView;
import com.eureton.warmaodds.views.TotalsView;
import com.eureton.warmaodds.views.UiView;
import com.eureton.warmaodds.widgets.DiscreetSpinner;
import com.eureton.warmaodds.widgets.ProgressDialog;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ModifiersFragment extends Fragment
		implements ModifiersView, UiView {

	private static final String TAG = ModifiersFragment.class.getSimpleName();
	private static final String PROGRESS_KEY = TAG + "#progress";

	@Bind(R.id.MODIFIERS_SP_ATTACKROLL) DiscreetSpinner mAttack;
	@Bind(R.id.MODIFIERS_SP_DAMAGEROLL) DiscreetSpinner mDamage;
	@Bind(R.id.MODIFIERS_SP_ONHIT) DiscreetSpinner mOnHit;
	@Bind(R.id.MODIFIERS_SP_ONCRITICAL) DiscreetSpinner mOnCrit;
	@Bind(R.id.MODIFIERS_SP_ONKILL) DiscreetSpinner mOnKill;

	private ModifiersPresenter mPresenter;
	private ProgressDialog mProgressDialog;
	private int mIndex;
	private TotalsView mTotalsView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		fetchPresenter(savedInstanceState);
		initProgressDialog(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView =
				inflater.inflate(R.layout.fragment_modifiers, container, false);
		
		ButterKnife.bind(this, rootView);

		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		fetchTotalsView();
	}

	@Override
	public void onStart() {
		super.onStart();

		mPresenter.setAvailable(new Modifiers(this, this, mTotalsView));
	}

	@Override
	public void onStop() {
		mPresenter.setUnavailable();

		super.onStop();
	}
	
	@Override
	public void onDestroyView() {
		ButterKnife.unbind(this);

		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);

		state.putInt(PROGRESS_KEY, mProgressDialog.getProgress());
		mPresenter.save(state);
	}

	@Override
	public void onDestroy() {
		mPresenter.destroy();

		super.onDestroy();
	}

	@Override
	public void setAttack(int position) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setAttack -> " + position);

		mAttack.setSelection(position);
	}

	@Override
	public void setDamage(int position) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setDamage -> " + position);

		mDamage.setSelection(position);
	}

	@Override
	public void setOnHit(int position) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setOnHit -> " + position);

		mOnHit.setSelection(position);
	}

	@Override
	public void setOnCritical(int position) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setOnCritical -> " + position);

		mOnCrit.setSelection(position);
	}

	@Override
	public void setOnKill(int position) {
		if (BuildConfig.DEBUG) Log.d(TAG, "setOnKill -> " + position);

		mOnKill.setSelection(position);
	}

	@Override
	public void enableUi() { setUiEnabled(true); }
	
	@Override
	public void disableUi() { setUiEnabled(false); }

	@Override
	public void showProgress() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (BuildConfig.DEBUG) Log.d(TAG, "showProgress");
				mProgressDialog.show(getFragmentManager(), "ProgressDialog");
			}
		});
	}

	@Override
	public void hideProgress() {
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (BuildConfig.DEBUG) Log.d(TAG, "hideProgress");
				mProgressDialog.dismiss();
			}
		});
	}

	@Override
	public void setProgressStatus(int status) {
		final int finalStatus = status;
		
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (BuildConfig.DEBUG) Log.d(TAG, "setProgressStatus");
				mProgressDialog.setProgress(finalStatus);
			}
		});
	}

	@OnItemSelected({
		R.id.MODIFIERS_SP_ATTACKROLL,
		R.id.MODIFIERS_SP_DAMAGEROLL,
		R.id.MODIFIERS_SP_ONHIT,
		R.id.MODIFIERS_SP_ONCRITICAL,
		R.id.MODIFIERS_SP_ONKILL
	})
	public void itemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		switch (parent.getId()) {
		case R.id.MODIFIERS_SP_ATTACKROLL:
			mPresenter.attackModifierChanged(mIndex, getAttackModifier());
			break;
		case R.id.MODIFIERS_SP_DAMAGEROLL:
			mPresenter.damageModifierChanged(mIndex, getDamageModifier());
			break;
		case R.id.MODIFIERS_SP_ONHIT:
			mPresenter.onHitModifierChanged(mIndex, getOnHitModifier());
			break;
		case R.id.MODIFIERS_SP_ONCRITICAL:
			mPresenter.onCritModifierChanged(mIndex, getOnCritModifier());
			break;
		case R.id.MODIFIERS_SP_ONKILL:
			mPresenter.onKillModifierChanged(mIndex, getOnKillModifier());
			break;
		}
	}
	
	@OnClick(R.id.MODIFIERS_BT_CLEAR)
	public void onClear(View view) { mPresenter.onModifiersCleared(mIndex); }

	public int getIndex() { return mIndex; }

	public void setIndex(int index) {
		mIndex = index;
		mPresenter.indexChanged(index);
	}

	public void fetchTotalsView() {
		Activity a = getActivity();
		boolean n = a.
				getIntent().
				getByteExtra(ModifiersActivity.ISWIDE_KEY, (byte) -1) == 0;

		if (n) mTotalsView = (TotalsView) getActivity().
				getFragmentManager().
				findFragmentById(R.id.FRAGMENT_TOTALS);
		if (BuildConfig.DEBUG) Log.d(TAG, "is narrow? " + n);
	}

	private void fetchPresenter(Bundle state) {
		Application app = (Application) getActivity().getApplication();

		mPresenter = app.getModifiersPresenter();
		mPresenter.create();
		mPresenter.restore(state);
	}

	private Constants.AttackModifier getAttackModifier() {
		Constants.AttackModifier m = Constants.AttackModifier.NONE;

		switch (mAttack.getSelectedItemPosition()) {
		case 0: m = Constants.AttackModifier.NONE; break;
		case 1: m = Constants.AttackModifier.REROLL; break;
		case 2: m = Constants.AttackModifier.DISCARD_LOWEST; break;
		case 3: m = Constants.AttackModifier.DISCARD_HIGHEST; break;
		//case 4: m = Constants.AttackModifier.DISCARD_ANY_1; break;
		//case 5: m = Constants.AttackModifier.REROLL_1S_AND_2S; break;
		//case 6: m = Constants.AttackModifier.ADD_2_DISCARD_ANY_2; break;
		}

		return m;
	}

	private Constants.DamageModifier getDamageModifier() {
		Constants.DamageModifier m = Constants.DamageModifier.NONE;

		switch (mDamage.getSelectedItemPosition()) {
		case 0: m = Constants.DamageModifier.NONE; break;
		case 1: m = Constants.DamageModifier.REROLL; break;
		case 2: m = Constants.DamageModifier.DISCARD_LOWEST; break;
		//case 3: m = Constants.DamageModifier.DISCARD_ANY_1; break;
		//case 4: m = Constants.DamageModifier.REROLL_1S_AND_2S; break;
		}

		return m;
	}

	private Constants.OnHitModifier getOnHitModifier() {
		Constants.OnHitModifier m = Constants.OnHitModifier.NONE;

		switch (mOnHit.getSelectedItemPosition()) {
		case 0: m = Constants.OnHitModifier.NONE; break;
		case 1: m = Constants.OnHitModifier.DOUBLE_DAMAGE; break;
		case 2: m = Constants.OnHitModifier.MIN_1_DAMAGE; break;
		case 3: m = Constants.OnHitModifier.NO_ROLL_1_DAMAGE; break;
		case 4: m = Constants.OnHitModifier.NO_ROLL_3_DAMAGE; break;
		case 5: m = Constants.OnHitModifier.D3_PLUS_3_DAMAGE; break;
		case 6: m = Constants.OnHitModifier.KNOCKDOWN; break;
		}

		return m;
	}

	private Constants.OnCritModifier getOnCritModifier() {
		Constants.OnCritModifier m = Constants.OnCritModifier.NONE;

		switch (mOnCrit.getSelectedItemPosition()) {
		case 0: m = Constants.OnCritModifier.NONE; break;
		case 1: m = Constants.OnCritModifier.EXTRA_DIE; break;
		case 2: m = Constants.OnCritModifier.KNOCKDOWN; break;
		//case 2: m = Constants.OnCritModifier.HALVE_ARM; break;
		//case 3: m = Constants.OnCritModifier.RFP; break;
		}

		return m;
	}

	private Constants.OnKillModifier getOnKillModifier() {
		Constants.OnKillModifier m = Constants.OnKillModifier.NONE;

		switch (mOnKill.getSelectedItemPosition()) {
		case 0: m = Constants.OnKillModifier.NONE; break;
		case 1: m = Constants.OnKillModifier.NO_TOUGH; break;
		}

		return m;
	}

	private void initProgressDialog(Bundle state) {
		mProgressDialog = new ProgressDialog();
		mProgressDialog.setRetainInstance(true);
		mProgressDialog.setOnCancelListener(
			new DialogInterface.OnCancelListener() {
				
				@Override
				public void onCancel(DialogInterface dialog) {
					if (BuildConfig.DEBUG) Log.d(TAG, "onCancel()");
					mPresenter.cancel();
				}
			}
		);
		if (state != null && state.containsKey(PROGRESS_KEY)) {
			mProgressDialog.setProgress(state.getInt(PROGRESS_KEY));
		}
	}

	private void setUiEnabled(boolean enabled) {
		final boolean e = enabled;

		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mAttack.setEnabled(e);
				mDamage.setEnabled(e);
				mOnHit.setEnabled(e);
				mOnCrit.setEnabled(e);
				mOnKill.setEnabled(e);
			}
		});
	}
}

