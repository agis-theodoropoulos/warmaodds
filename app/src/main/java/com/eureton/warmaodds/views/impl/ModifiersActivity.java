package com.eureton.warmaodds.views.impl;

import butterknife.Bind;
import butterknife.BindInt;
import butterknife.BindBool;
import butterknife.ButterKnife;
import butterknife.OnClick;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.widgets.DiscreetSpinner;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.CheckBox;

public class ModifiersActivity extends Activity {

	private static final String TAG = ModifiersActivity.class.getSimpleName();
	public static final String INDEX_KEY = TAG + "#index";
	public static final String ISWIDE_KEY = TAG + "#iswide";

	private ModifiersFragment mModifiersFragment;
	private int mIndex;

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_modifiers);
		parseArgs();
		initFragments();
		lockOrientation();
		ButterKnife.bind(this);
	}

	private void parseArgs() {
		Intent intent = getIntent();
		mIndex = intent.getByteExtra(INDEX_KEY, (byte) -1);

		if (BuildConfig.DEBUG) {
			if (mIndex == -1) Log.w(TAG, "Index not found");
			Log.i(TAG, "Index: " + mIndex);
		}
	}
	
	private void initFragments() {
		try {
			FragmentManager fm = getFragmentManager();

			mModifiersFragment = (ModifiersFragment)
					fm.findFragmentById(R.id.FRAGMENT_MODIFIERS);
			mModifiersFragment.setIndex(mIndex);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}

	/*
	 *	When viewed on tablets in portrait mode, this activity must not
	 *	allow transition to landscape mode
	 */
	private void lockOrientation() {
		Resources r = getResources();

		if (r.getBoolean(R.bool.device_tablet_portrait)) {
			setRequestedOrientation(
				ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
			);
		}
	}
}

