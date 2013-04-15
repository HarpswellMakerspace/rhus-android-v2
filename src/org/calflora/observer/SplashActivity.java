package org.calflora.observer;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.ImageView;

public class SplashActivity extends BaseActivity {

	private Handler mHandler = new Handler(); 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.splash, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		if(Observer.instance.getOrganization() != null){
			ImageView imageView = (ImageView) findViewById(R.id.splashImage);
			imageView.setImageResource(R.drawable.logo);
			
		}
		
		mHandler.postDelayed(new Runnable() { 
	        public void run() { 
	        	Intent intent = new Intent("org.calflora.observer.action.PROJECTS");
				startActivity(intent);
	        	finish();
	        } 
	    },2000);
	}
	
	

	
}
