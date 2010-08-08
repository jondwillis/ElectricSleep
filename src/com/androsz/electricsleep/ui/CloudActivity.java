package com.androsz.electricsleep.ui;

import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.androsz.electricsleep.R;

public class CloudActivity extends CustomTitlebarActivity {

	@Override
	protected int getContentAreaLayoutId() {
		// TODO Auto-generated method stub
		return R.layout.activity_cloud;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final ProgressBar progressWheel = (ProgressBar) super
				.findViewById(R.id.title_progress_1);

		WebView webView = (WebView) findViewById(R.id.webViewCloud);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.getSettings().setBlockNetworkImage(true);

		webView.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				int visibility = (progress == 100 ? View.GONE : View.VISIBLE);
				progressWheel.setVisibility(visibility);
			}
		});
		webView.setWebViewClient(new WebViewClient() {
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				Toast.makeText(CloudActivity.this, "Oh no! " + description,
						Toast.LENGTH_SHORT).show();
			}

			public void onPageFinished(WebView webView, String url) {
				progressWheel.setVisibility(View.GONE);
			}
		});

		webView.loadUrl("http://androsz.com/");
	}

}
