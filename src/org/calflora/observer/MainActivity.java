package org.calflora.observer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
	     SharedPreferences settings = getPreferences(MODE_PRIVATE);
	     String APIKey = settings.getString("APIKey", null);
	     if(APIKey != null){
	    	 //TODO: Revalidate API key ??
	    	 //Remember that they may be offline
	    	
	    	 //TODO: And check to see that they have an organization and project selected
	    	 //If so, we just take them them to the Dashboard or Map
	    	 Intent intent = new Intent("org.calflora.observer.action.MAPOVERVIEW");
	    	 startActivity(intent);
	    	 
	     } else {

	    	 Intent intent = new Intent("org.calflora.observer.action.LOGIN");
	    	 startActivity(intent);
	     }
	}
	
	
	

}
