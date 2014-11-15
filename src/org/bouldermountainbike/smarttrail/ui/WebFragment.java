/**
 * GeoZen LLC
 * Copyright 2011. All rights reserved.
 */

package org.bouldermountainbike.smarttrail.ui;

import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.bouldermountainbike.smarttrail.R;

//import android.support.v4.app.Fragment;

/**
 * A fragment containing a {@link WebView} pointing to the I/O announcements
 * URL.
 */
public class WebFragment extends Fragment {

	private static final Pattern sSiteUrlPattern = Pattern
			.compile("bma\\.geozen\\.com\\/events");

	private WebView mWebView;
	private View mLoadingSpinner;

	private String mUrl;

	private String mTitle;

	public static final String EXTRA_URL = "org.bouldermountainbike.smarttrail.extra.URL";
	public static final String EXTRA_TITLE = "title";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		Bundle args = getArguments();
		mUrl = args.getString(EXTRA_URL);
		mTitle = args.getString(EXTRA_TITLE);

		// AnalyticsUtils.getInstance(getActivity()).trackPageView("/Alerts");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		getActivity().getActionBar().setNavigationMode(
				ActionBar.NAVIGATION_MODE_STANDARD);
		getActivity().getActionBar().setTitle(mTitle);

		ViewGroup root = (ViewGroup) inflater.inflate(
				R.layout.fragment_webview_with_spinner, null);

		// For some reason, if we omit this, NoSaveStateFrameLayout thinks we
		// are
		// FILL_PARENT / WRAP_CONTENT, making the progress bar stick to the top
		// of the activity.
		root.setLayoutParams(new ViewGroup.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));

		mLoadingSpinner = root.findViewById(R.id.loading_spinner);
		mWebView = (WebView) root.findViewById(R.id.webview);
		mWebView.setWebViewClient(mWebViewClient);

		mWebView.post(new Runnable() {
			public void run() {
				// mWebView.getSettings().setJavaScriptEnabled(true);
				mWebView.getSettings()
						.setJavaScriptCanOpenWindowsAutomatically(false);
				mWebView.loadUrl(mUrl);
			}
		});

		return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.refresh_menu_items, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case android.R.id.home:
			getActivity().onBackPressed();
			return true;

		default:
			return super.onOptionsItemSelected(item);

		}
		// if (item.getItemId() == R.id.menu_refresh) {
		// mWebView.reload();
		// return true;
		// }
		// return super.onOptionsItemSelected(item);
	}

	private WebViewClient mWebViewClient = new WebViewClient() {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			mLoadingSpinner.setVisibility(View.VISIBLE);
			mWebView.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			mLoadingSpinner.setVisibility(View.GONE);
			mWebView.setVisibility(View.VISIBLE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (sSiteUrlPattern.matcher(url).find()) {
				return false;
			}

			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
			startActivity(intent);
			return true;
		}
	};
}
