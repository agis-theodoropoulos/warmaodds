package com.eureton.warmaodds.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.eureton.warmaodds.BuildConfig;
import com.eureton.warmaodds.R;

public class HtmlAwareTextView extends TextView {
	
	private static final String TAG = HtmlAwareTextView.class.getSimpleName();

	private LinkClickListener mListener;

	public HtmlAwareTextView(Context context) {
		super(context);
	}
	
	public HtmlAwareTextView(Context context, AttributeSet attrs) {
		super(context, attrs);

		processAttributes(context, attrs);
	}

	public void setLinkClickListener(LinkClickListener listener) {
		mListener = listener;
	}

	private void processAttributes(Context context, AttributeSet attrs) {
		TypedArray a = null;

		try {
			a = context.getTheme().obtainStyledAttributes(attrs,
					R.styleable.HtmlAwareTextView, 0, 0);

			String t = a.getString(R.styleable.HtmlAwareTextView_htmlText);
			setHtmlText(t);
		} catch (Exception e) {
			if (BuildConfig.DEBUG) Log.e(TAG, e.getMessage(), e);
		} finally {
			if (a != null) a.recycle();
		}
	}

	private void setHtmlText(String html) {
		CharSequence cs = Html.fromHtml(html);
		SpannableStringBuilder ssb = new SpannableStringBuilder(cs);
		URLSpan[] urls = ssb.getSpans(0, cs.length(), URLSpan.class);   
		for (URLSpan span : urls) makeClickable(ssb, span);
		
		setText(ssb);
		setMovementMethod(LinkMovementMethod.getInstance());       
	}

	private void makeClickable(SpannableStringBuilder builder, URLSpan span) {
		int start = builder.getSpanStart(span);
		int end = builder.getSpanEnd(span);
		int flags = builder.getSpanFlags(span);
		final URLSpan s = span;
		ClickableSpan clickable = new ClickableSpan() {

			public void onClick(View view) {
				String url = s.getURL();

				if (mListener != null) mListener.onLinkClicked(url);
				Log.d(TAG, "onClick: " + url);
			}
		};

		builder.setSpan(clickable, start, end, flags);
		builder.removeSpan(span);
	}

	public interface LinkClickListener {

		void onLinkClicked(String url);
	}
}

