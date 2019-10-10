package com.eureton.warmaodds.util;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

public final class Util {
	
	public static boolean LOG_WARMACHINE_WORKER = false;
	public static final int KD = -1;
	public static final int ND = -3;
	public static final int AUT = -4;
	public static final int ANY = -2;
	public static final int MISS = 0;
	public static final float TOUGH = 2.f / 6;
	public static final float NOT_TOUGH = 1.f - TOUGH;

	public static boolean isUiThread() {
		return Looper.myLooper() == Looper.getMainLooper();
	}
		
	public static void log(String tag, String key, float value,
			long duration) {
		if (BuildConfig.DEBUG) Log.i(tag, String.format(
			Locale.US,
			"setting %s to %.2f [%d ms]",
			key,
			value,
			duration
		));
	}
	
	public static void log(String tag, String key, int index, float value,
			long duration) {
		if (BuildConfig.DEBUG) Log.i(tag, String.format(
			Locale.US,
			"setting %s[%d] to %.2f [%d ms]",
			key,
			index,
			value,
			duration
		));
	}
	
	public static void logCollectionStart(String tag, String heading) {
		if (BuildConfig.DEBUG) Log.i(
			tag,
			String.format(Locale.US, ",--> %s", heading)
		);
	}
	
	public static void logCollectionStart(String tag, String heading,
			int count) {
		if (BuildConfig.DEBUG) Log.i(
			tag,
			String.format(Locale.US, ",--> %s (%d)", heading, count)
		);
	}
	
	public static void logCollectionEnd(String tag, String heading,
			long duration) {
		if (BuildConfig.DEBUG) Log.i(
			tag,
			String.format(Locale.US, "`--> END %s (%dms)", heading, duration)
		);
	}

	public static Intent getBrowserIntent(Context context, String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		Log.d("MainActivity", "URL: " + url);

		return i;
	}
}

