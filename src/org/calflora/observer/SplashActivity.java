package org.calflora.observer;

import org.calflora.observer.model.Organization;

import android.os.Bundle;
import android.os.Handler;
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

		ImageView imageView = (ImageView) findViewById(R.id.splashImage);
		imageView.setImageResource(R.drawable.poppy_logo);

		Organization o = Observer.instance.getOrganization();
		if(o != null){
			
			if (!o.getName().contentEquals("Independent") ){

				imageView.setImageResource(R.drawable.logo);

			} 

		} 

		mHandler.postDelayed(new Runnable() { 
			public void run() { 
				//if(getIntent().filterEquals(new Intent("org.calflora.observer.action.SPLASH_THEN_PROJECTS"))){
				Intent intent = new Intent("org.calflora.observer.action.PROJECTS");
				startActivity(intent);
				/*	} else {

	        	}*/
				finish();
			} 
		},2000);
	}




}
