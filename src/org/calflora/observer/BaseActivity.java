package org.calflora.observer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;

public class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		

		if(Observer.instance.getOrganization() != null){
			ImageView homeIcon = (ImageView) findViewById(android.R.id.home);
			homeIcon.setImageResource(R.drawable.logo);
		}
	}

	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(Observer.instance.getOrganization() != null){
			ImageView homeIcon = (ImageView) findViewById(android.R.id.home);
			homeIcon.setImageResource(R.drawable.logo);
		}
	}
	
	/*
	 * This doesn't work, because onRestart is being called every time the activity is launched.
	 * It's unclear how to know if it's been destoryed, and whether this has anything to do with use wanting
	 * to display the splash screen.
	@Override
	protected void onRestart() {
		super.onRestart();
		
		if(Observer.instance.getOrganization() != null){
			Intent intent = new Intent("org.calflora.observer.action.SPLASH");
			startActivity(intent);
		}
	}
	*/



	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_help:
	            showHelp();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void showHelp(){

		Intent intent = new Intent("org.calflora.observer.action.HELP");
		startActivity(intent);	

	}
}
