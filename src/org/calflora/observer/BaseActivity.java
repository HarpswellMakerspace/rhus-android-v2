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
