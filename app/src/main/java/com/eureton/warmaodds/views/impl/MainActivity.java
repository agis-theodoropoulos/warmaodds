package com.eureton.warmaodds.views.impl;

import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.android.Application;
import com.eureton.warmaodds.presenters.MainPresenter;
import com.eureton.warmaodds.util.Util;
import com.eureton.warmaodds.views.Attack;
import com.eureton.warmaodds.views.GlobalsView;
import com.eureton.warmaodds.views.Main;
import com.eureton.warmaodds.views.TotalsView;
import com.eureton.warmaodds.views.UiView;
import com.eureton.warmaodds.widgets.HtmlAwareTextView;
import com.eureton.warmaodds.widgets.ProgressDialog;
import com.eureton.warmaodds.widgets.SlidingPanelLayout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.NumberPicker;

public class MainActivity extends BaseActivity implements
		GlobalsView, UiView, Attack.AttackEventListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final String BOXES_KEY = TAG + "#boxes";
	private static final String FOCUS_KEY = TAG + "#focus";
	private static final String FURY_KEY = TAG + "#fury";
	private static final String TOUGH_KEY = TAG + "#tough";
	private static final String PROGRESS_KEY = TAG + "#progress";
	
	@Bind(R.id.BT_ADDATTACK) ImageButton mAddAttack;
	@BindInt(R.integer.default_boxes) int mBoxes;
	@BindInt(R.integer.default_focus) int mFocus;
	@BindInt(R.integer.default_fury) int mFury;
	@BindBool(R.bool.default_kds) boolean mKds;
	@BindBool(R.bool.default_tough) boolean mTough;
	private MainPresenter mPresenter;
	private AttacksFragment mAttacksFragment;
	private TotalsFragment mTotalsFragment;
	private ModifiersFragment mModifiersFragment;
	private ProgressDialog mProgressDialog;
	private TapListener mTapListener;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		boolean b = super.onCreateOptionsMenu(menu);

		setBoxes(String.valueOf(mBoxes));
		setFocus(String.valueOf(mFocus));
		setFury(String.valueOf(mFury));

		return b;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean res = super.onOptionsItemSelected(item);
		
		switch (item.getItemId()) {
		case R.id.menu_boxes: createPickerDialog(Attr.BOXES); break;
		case R.id.menu_focus: createPickerDialog(Attr.FOCUS); break;
		case R.id.menu_fury: createPickerDialog(Attr.FURY); break;
		case R.id.menu_reset: onMenuReset(item); break;
		case R.id.menu_kds: onMenuKds(item); break;
		case R.id.menu_tough: onMenuTough(item); break;
		}

		return res;
	}

	@Override
	public void enableUi() {
		runOnUiThread(new Runnable() {
		
			@Override
			public void run() { mAddAttack.setEnabled(true); }
		});
	}
	
	@Override
	public void disableUi() {
		runOnUiThread(new Runnable() {
		
			@Override
			public void run() { mAddAttack.setEnabled(false); }
		});
	}

	@Override
	public void showProgress() {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (BuildConfig.DEBUG) Log.d(TAG, "showProgress");
				if (!mProgressDialog.isAdded()) {
					mProgressDialog.show(getFragmentManager(),
							"ProgressDialog");
				}
			}
		});
	}

	@Override
	public void hideProgress() {
		runOnUiThread(new Runnable() {
			
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
		
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				if (BuildConfig.DEBUG) Log.d(TAG, "setProgressStatus");
				mProgressDialog.setProgress(finalStatus);
			}
		});
	}

	@Override
	public void setBoxes(String s) {
		final String finalText = s;

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setLabelledIconText(R.id.menu_boxes, finalText);
				if (BuildConfig.DEBUG) Log.d(TAG, "BOXES: " + finalText);
			}
		});
	}

	@Override
	public void setFocus(String s) {
		final String finalText = s;

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setLabelledIconText(R.id.menu_focus, finalText);
				if (BuildConfig.DEBUG) Log.d(TAG, "FOCUS: " + finalText);
			}
		});
	}

	@Override
	public void setFury(String s) {
		final String finalText = s;

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				setLabelledIconText(R.id.menu_fury, finalText);
				if (BuildConfig.DEBUG) Log.d(TAG, "FURY: " + finalText);
			}
		});
	}

	@Override
	public void setTough(boolean tough) {
		final boolean t = tough;

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mTough = t;
				setCheckboxStatus(R.id.menu_tough, t);
				if (BuildConfig.DEBUG) Log.d(TAG, "TOUGH: " + t);
			}
		});
	}

	@Override
	public void setKnockdownStationary(boolean knockdownStationary) {
		final boolean kds = knockdownStationary;

		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mKds = kds;
				setCheckboxStatus(R.id.menu_kds, kds);
				if (BuildConfig.DEBUG) Log.d(TAG, "KDS: " + kds);
			}
		});
	}

	@Override
	public void onTypeChanged(Attack view, int newValue) {
		mPresenter.typeChanged(mAttacksFragment.getAttackIndex(view), newValue);
	}

	@Override
	public void onMatChanged(Attack view, int newValue) {
		mPresenter.matChanged(mAttacksFragment.getAttackIndex(view), newValue);
	}
	
	@Override
	public void onDefChanged(Attack view, int newValue) {
		mPresenter.defChanged(mAttacksFragment.getAttackIndex(view), newValue);
	}
	
	@Override
	public void onPowChanged(Attack view, int newValue) {
		mPresenter.powChanged(mAttacksFragment.getAttackIndex(view), newValue);
	}
	
	@Override
	public void onArmChanged(Attack view, int newValue) {
		mPresenter.armChanged(mAttacksFragment.getAttackIndex(view), newValue);
	}
	
	@Override
	public void onAttackDiceChanged(Attack view, int newValue) {
		mPresenter.attackDiceChanged(mAttacksFragment.getAttackIndex(view),
				newValue);
	}
	
	@Override
	public void onDamageDiceChanged(Attack view, int newValue) {
		mPresenter.damageDiceChanged(mAttacksFragment.getAttackIndex(view),
				newValue);
	}

	@Override
	public void onRemoved(Attack view) {
		int index = mAttacksFragment.getAttackIndex(view);

		if (isWide() && index == mModifiersFragment.getIndex()) {
			trySetModifierIndex(index > 0 ? index - 1 : index + 1);
		}
		mPresenter.attackRemoved(index);
	}
	
	@OnClick(R.id.BT_ADDATTACK)
	public void onAttackAdded(View v) { mPresenter.attackAdded(); }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onCreate");
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);
		setCheckboxMenuItemIds(new int[] { R.id.menu_kds, R.id.menu_tough });
		configureDynamicLayout();
		initFragments();
		initProgressDialog();
		fetchPresenter();
		ButterKnife.bind(this);
		configureAttackTapListener();
	}

	@Override
	protected void onResume() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onResume");
		super.onResume();

		mPresenter.setAvailable(new Main(
			mAttacksFragment,
			mAttacksFragment.getAttackViews(),
			this,
			this,
			(TotalsView) mTotalsFragment
		));
		if (mAttacksFragment.getAttackCount() == 0) mPresenter.attackAdded();
		mTapListener.start();
	}

	@Override
	protected void onPause() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onPause");
		mPresenter.setUnavailable();
		mTapListener.stop();
		
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy");
		mPresenter.destroy();
		mTapListener.release();

		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onSaveInstanceState");
		outState.putInt(BOXES_KEY, mBoxes);
		outState.putInt(FOCUS_KEY, mFocus);
		outState.putInt(FURY_KEY, mFury);
		outState.putByte(TOUGH_KEY, (byte) (mTough ? 1 : 0));
		outState.putInt(PROGRESS_KEY, mProgressDialog.getProgress());
		mPresenter.save(outState);
		
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		if (BuildConfig.DEBUG) Log.d(TAG, "onRestoreInstanceState");
		super.onRestoreInstanceState(savedInstanceState);
		
		mBoxes = savedInstanceState.getInt(BOXES_KEY);
		mFocus = savedInstanceState.getInt(FOCUS_KEY);
		mFury = savedInstanceState.getInt(FURY_KEY);
		mTough = savedInstanceState.getByte(TOUGH_KEY) == 0 ? false : true;
		mProgressDialog.setProgress(savedInstanceState.getInt(PROGRESS_KEY));
		mPresenter.restore(savedInstanceState);
	}

	private void initFragments() {
		try {
			FragmentManager manager = getFragmentManager();

			mAttacksFragment = (AttacksFragment)
					manager.findFragmentById(R.id.FRAGMENT_MAIN);
			mTotalsFragment = (TotalsFragment)
					manager.findFragmentById(R.id.FRAGMENT_TOTALS);
			mModifiersFragment = (ModifiersFragment)
					manager.findFragmentById(R.id.FRAGMENT_MODIFIERS);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}

	private void initProgressDialog() {
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
	}

	private void fetchPresenter() {
		try {
			Application app = (Application) getApplication();

			mPresenter = app.getMainPresenter();
			mPresenter.create();
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}
	
	private void configureDynamicLayout() {
		final View totals = findViewById(R.id.FRAGMENT_TOTALS);

		totals.getViewTreeObserver().addOnGlobalLayoutListener(
			new ViewTreeObserver.OnGlobalLayoutListener() {
			
				@Override
				public void onGlobalLayout() {
					positionAddButton();
					
					totals.
							getViewTreeObserver().
							removeOnGlobalLayoutListener(this);
				}
			}
		);
	}

	private void positionAddButton() {
		int x = mAttacksFragment.getView().getRight() -
				mAddAttack.getWidth() -
				getResources().getDimensionPixelSize(R.dimen.add_btn_margin);

		Log.d(TAG, "positionAddButton() " + x);
		mAddAttack.setX(x);
	}

	private boolean tryOpenModifiers(float x, float y) {
		int index = mAttacksFragment.getAttackIndex(x, y);
		boolean found = (index != -1);

		if (BuildConfig.DEBUG) Log.d(
			TAG,
			String.format(Locale.US, "tap (%f, %f) in [%d]", x, y, index)
		);

		if (found) {
			if (!trySetModifierIndex(index)) {
				Intent intent = new Intent(this, ModifiersActivity.class);
				intent.putExtra(ModifiersActivity.INDEX_KEY, (byte) index);
				intent.putExtra(ModifiersActivity.ISWIDE_KEY,
						(byte) (isWide() ? 1 : 0));

				startActivity(intent);
			}
		}

		return found;
	}

	private boolean trySetModifierIndex(int index) {
		boolean isSet = false;

		if (isWide()) {
			mModifiersFragment.setIndex(index);
			mAttacksFragment.setSelection(index);
			isSet = true;
		}

		return isSet;
	}

	private boolean isWide() {
		return mModifiersFragment != null && mModifiersFragment.isInLayout();
	}

	private boolean isNarrow() { return !isWide(); }

	private void configureAttackTapListener() {
		mTapListener = new TapListener(this, mAttacksFragment.getView());
	}

	private DialogInterface.OnClickListener createDlgListener(View view,
			Attr attr) {
		final NumberPicker np =
				(NumberPicker) view.findViewById(getDlgPickerId(attr));
		final Attr finalAttr = attr;
		np.setValue(getAttrValue(attr));
		DialogInterface.OnClickListener listener =
				new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							update(finalAttr, np.getValue());
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							dialog.dismiss();
							break;
						}
					}
				};
		
		return listener;
	}
	
	private AlertDialog createPickerDialog(Attr attr) {
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(getDlgLayoutId(attr), null);
		DialogInterface.OnClickListener listener =
				createDlgListener(view, attr);
		AlertDialog.Builder builder =
				new AlertDialog.Builder(this, R.style.DialogTheme);
		AlertDialog dlg = builder.
				setTitle(getDlgTitleId(attr)).
				setMessage(getDlgMsgId(attr)).
				setNegativeButton(getDlgNegBtnCaptionId(attr), listener).
				setPositiveButton(getDlgPosBtnCaptionId(attr), listener).
				setView(view).
				show();

		return fixStyles(dlg);
	}
	
	private AlertDialog fixStyles(AlertDialog dlg) {
		Resources r = getResources();
		int divId = r.getIdentifier("titleDivider", "id", "android");
		View div = dlg.findViewById(divId);

		if (div != null) {
			div.setBackgroundColor(r.getColor(R.color.dialogs_title_divider));
		}

		return dlg;
	}

	private void update(Attr attr, int value) {
		switch (attr) {
		case BOXES:
			mBoxes = value;
			mPresenter.boxesChanged(value);
			break;
		case FOCUS:
			mFocus = value;
			mFury = 0;
			mPresenter.focusChanged(value);
			break;
		case FURY:
			mFury = value;
			mFocus = 0;
			mPresenter.furyChanged(value);
			break;
		}
	}

	private void onMenuReset(MenuItem item) {
		Resources r = getResources();
		mPresenter.attacksReset().addObserver(new Observer() {
			@Override
			public void update(Observable o, Object arg) {
				trySetModifierIndex(0);
			}
		});
	}

	private void onMenuKds(MenuItem item) {
		mKds = item.isChecked();
		mPresenter.kdsChanged(mKds);
	}

	private void onMenuTough(MenuItem item) {
		mTough = item.isChecked();
		mPresenter.toughChanged(mTough);
	}

	private int getDlgLayoutId(Attr attr) {
		int id = 0;
		
		switch (attr) {
		case BOXES: id = R.layout.dialog_boxes; break;
		case FOCUS: id = R.layout.dialog_focus; break;
		case FURY: id = R.layout.dialog_transfers; break;
		}
		
		return id;
	}
	
	private int getDlgPickerId(Attr attr) {
		int id = 0;
		
		switch (attr) {
		case BOXES: id = R.id.BOXES_NP_VALUE; break;
		case FOCUS: id = R.id.FOCUS_NP_VALUE; break;
		case FURY: id = R.id.TRANSFERS_NP_VALUE; break;
		}

		return id;
	}
	
	private int getAttrValue(Attr attr) {
		int val = 0;
		
		switch (attr) {
		case BOXES: val = mBoxes; break;
		case FOCUS: val = mFocus; break;
		case FURY: val = mFury; break;
		}

		return val;
	}
	
	private int getDlgTitleId(Attr attr) {
		int id = 0;
		
		switch (attr) {
		case BOXES: id = R.string.dialogs_boxes_title; break;
		case FOCUS: id = R.string.dialogs_focus_title; break;
		case FURY: id = R.string.dialogs_fury_title; break;
		}

		return id;
	}
	
	private int getDlgMsgId(Attr attr) {
		int id = 0;
		
		switch (attr) {
		case BOXES: id = R.string.dialogs_boxes_message; break;
		case FOCUS: id = R.string.dialogs_focus_message; break;
		case FURY: id = R.string.dialogs_fury_message; break;
		}

		return id;
	}
	
	private int getDlgNegBtnCaptionId(Attr attr) {
		int id = 0;
		
		switch (attr) {
		case BOXES: id = R.string.dialogs_boxes_neg_btn; break;
		case FOCUS: id = R.string.dialogs_focus_neg_btn; break;
		case FURY: id = R.string.dialogs_fury_neg_btn; break;
		}

		return id;
	}
	
	private int getDlgPosBtnCaptionId(Attr attr) {
		int id = 0;
		
		switch (attr) {
		case BOXES: id = R.string.dialogs_boxes_pos_btn; break;
		case FOCUS: id = R.string.dialogs_focus_pos_btn; break;
		case FURY: id = R.string.dialogs_fury_pos_btn; break;
		}

		return id;
	}
	
	private enum Attr {
		BOXES,
		FOCUS,
		FURY
	}

	private class TapListener extends GestureDetector.SimpleOnGestureListener {

		private GestureDetector mGestureDetector;
		private View.OnTouchListener mListener;
		private View mView;

		private TapListener(Context context, View view) {
			mGestureDetector = new GestureDetector(context, this);
			mListener = new View.OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent e) {
					return mGestureDetector.onTouchEvent(e);
				}
			};
			mView = view;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			tryOpenModifiers(e.getRawX(), e.getRawY());

			return true;
		}

		public void start() { mView.setOnTouchListener(mListener); }

		public void stop() { mView.setOnTouchListener(null); }

		public void release() {
			mListener = null;
			mGestureDetector = null;
			mView = null;
		}
	}
}

