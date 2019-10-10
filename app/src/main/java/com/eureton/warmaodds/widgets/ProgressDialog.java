package com.eureton.warmaodds.widgets;

import butterknife.Bind;
import butterknife.ButterKnife;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ProgressDialog extends DialogFragment {

	private static final String TAG = ProgressDialog.class.getSimpleName();

	@Bind(R.id.PROGRESS_SPINNER) ProgressBar mSpinner;
	@Bind(R.id.PROGRESS_BAR) ProgressBar mBar;
	@Bind(R.id.PROGRESS_STATUS) TextView mStatus;
	private AlertDialog mDialog;
	private DialogInterface.OnCancelListener mCancelListener;
	private int mProgress;

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Activity a = getActivity();
		View v = a.getLayoutInflater().inflate(R.layout.dialog_progress, null);
		ButterKnife.bind(this, v);
		displayProgress(mProgress);

		Resources r = a.getResources();
		AlertDialog.Builder builder =
				new AlertDialog.Builder(a, R.style.DialogTheme);
		mDialog = builder.
				setTitle(r.getString(R.string.dialog_progress_title)).
				setView(v).
				setMessage(r.getString(R.string.dialog_progress_message)).
				create();
		mDialog.setCanceledOnTouchOutside(false);
		mDialog.setCancelable(true);

		styleProgressBar();

		return mDialog;
	}

	@Override
	public void onCancel(DialogInterface dialog) {
		if (mCancelListener != null) mCancelListener.onCancel(dialog);
	}

	@Override
	public void onStart() {
		super.onStart();

		styleTitleDivider();
	}

	public void setOnCancelListener(DialogInterface.OnCancelListener listener) {
		mCancelListener = listener;
	}

	public int getProgress() { return mProgress; }

	public void setProgress(int progress) {
		mProgress = progress;
		displayProgress(progress);
	}

	private void displayProgress(int progress) {
		if (mStatus != null) mStatus.setText(String.format("%3d%%", progress));
		if (mBar != null) mBar.setProgress(progress);
	}

	private void styleProgressBar() {
		mBar.getProgressDrawable().setColorFilter(
			getResources().getColor(R.color.dialog_title_progress_bar),
			PorterDuff.Mode.SRC_IN
		);
	}

	private void styleTitleDivider() {
		Resources r = getResources();
		int divId = r.getIdentifier("titleDivider", "id", "android");
		View v = mDialog.findViewById(divId);

		if (v != null) {
			v.setBackgroundColor(r.getColor(R.color.dialog_title_divider));
		}
	}
}

