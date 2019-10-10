package com.eureton.warmaodds.views.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;
import com.eureton.warmaodds.widgets.IconStat;
import com.eureton.warmaodds.widgets.IconToggle;

import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

class BaseActivity extends Activity {

	private static final String TAG = BaseActivity.class.getSimpleName();
	
	private int[] mCheckboxMenuItemIds;
	private Set<Integer> mCheckboxMenuItemIdSet;
	private Menu mMenu;
	private View.OnClickListener mLabelledIconListener;
	private Map<IconStat, MenuItem> mLabelledIconMap;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (mCheckboxMenuItemIds != null) {
			for (int id : mCheckboxMenuItemIds) {
				initializeActionBarCheckbox(menu, id, false);
			}
		}
		setLabelledIconListeners(menu);
		mMenu = menu;
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean res = super.onOptionsItemSelected(item);
		
		if (mCheckboxMenuItemIdSet.contains(item.getItemId())) {
			setActionBarCheckBoxChecked(item, !item.isChecked());
			res = true;
		}
		
		return res;
	}
	
	@Override
	protected void onDestroy() {
		mLabelledIconMap.clear();

		super.onDestroy();
	}

	protected final void setCheckboxMenuItemIds(int[] ids) {
		mCheckboxMenuItemIds = ids;
		
		mCheckboxMenuItemIdSet = new HashSet<Integer>();
		for (int id : ids) mCheckboxMenuItemIdSet.add(id);
	}

	protected final void setCheckboxStatus(int menuItemId, boolean isChecked) {
		try {
			MenuItem i = mMenu.findItem(menuItemId);
			setActionBarCheckBoxChecked(i, isChecked);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}

	protected final void setLabelledIconText(int menuItemId, String text) {
		try {
			MenuItem i = mMenu.findItem(menuItemId);
			View v = i.getActionView();
			IconStat is = (IconStat) v;

			is.setText(text);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		}
	}
	
	private void initializeActionBarCheckbox(Menu menu, int menuItemId,
			boolean checked) {
		final MenuItem i = menu.findItem(menuItemId);
		final Activity a = this;
		IconToggle cb = tryGetCheckBox(i);
		
		setActionBarCheckBoxChecked(i, checked);
		if (cb != null) {
			cb.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) { a.onOptionsItemSelected(i); }
			});
		}
	}

	private void setLabelledIconListeners(Menu menu) {
		mLabelledIconListener = new LabelledIconListener();
		mLabelledIconMap = new HashMap<IconStat, MenuItem>();

		for (int i = 0; i < menu.size(); ++i) {
			MenuItem mi = menu.getItem(i);
			View v = mi.getActionView();

			if (v instanceof IconStat) {
				IconStat is = (IconStat) v;

				is.setOnClickListener(mLabelledIconListener);
				mLabelledIconMap.put(is, mi);
			}
		}
	}
	
	private void setActionBarCheckBoxChecked(MenuItem item, boolean checked) {
		if (item != null) {
			item.setChecked(checked);
			
			IconToggle cb = tryGetCheckBox(item);
			if (cb != null) cb.setChecked(checked);
		}
	}
	
	private static IconToggle tryGetCheckBox(MenuItem item) {
		IconToggle cb = null;
		
		try {
			cb = (IconToggle) item.
					getActionView().
					findViewById(R.id.action_item_checkbox);
		} catch (Exception e) { }
		
		return cb;
	}

	private class LabelledIconListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			MenuItem i = mLabelledIconMap.get(v);

			if (i != null) onOptionsItemSelected(i);
		}
	}
}

