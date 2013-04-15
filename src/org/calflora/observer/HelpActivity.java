package org.calflora.observer;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebView;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
	}

	@Override
	protected void onStart() {
		super.onStart();
		WebView webview = (WebView) findViewById(R.id.help_webview);
		//webview.loadUrl("http://www.calflora.org/phone");
		webview.loadUrl("http://www.calflora.org/about-cf.html");

	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

}
