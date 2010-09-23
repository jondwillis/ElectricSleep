package com.androsz.electricsleep.ui;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androsz.electricsleep.R;

public class CloudActivity extends CustomTitlebarActivity {

	private WebView webView;

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_cloud;
	}

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ProgressBar progressWheel = (ProgressBar) super
				.findViewById(R.id.title_progress_1);

		webView = (WebView) findViewById(R.id.webViewCloud);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBlockNetworkImage(true);

		webView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(final WebView view, final int progress) {
				final int visibility = progress == 100 ? View.GONE
						: View.VISIBLE;
				progressWheel.setVisibility(visibility);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageFinished(final WebView webView, final String url) {
				progressWheel.setVisibility(View.GONE);
			}

			@Override
			public void onReceivedError(final WebView view,
					final int errorCode, final String description,
					final String failingUrl) {
				Toast.makeText(CloudActivity.this, "Oh no! " + description,
						Toast.LENGTH_SHORT).show();
			}
		});

		webView.loadUrl("http://androsz.com/");
	}

	@Override
	protected void onDestroy() {
		if (webView != null) {
			webView.clearCache(true);
		}

		super.onDestroy();
	}

}
