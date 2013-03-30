package org.calflora.observer;

import java.io.IOException;

import org.calflora.observer.model.Project;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.os.Bundle;
import android.preference.PreferenceManager;
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
		
	     SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	     String APIKey = settings.getString("APIKey", null);
	     if(APIKey != null){
	    	 //TODO: Revalidate API key ??
	    	 //Remember that they may be offline
	    	
	    	 //TODO: And check to see that they have an organization and project selected
	    	 //If so, we just take them them to the Dashboard or Map
	    	 
	    	 //First load the organization and project!
	    	 String projectJSON = settings.getString("project", null);
	    	 if(projectJSON == null){
	    		 launchLoginActivity();
	    		 return;
	    	 }
	    	 
	    	 // TODO: This exception handling should go in parent activity class 
	    	 try {
	    		 Observer.project = Observer.mapper.readValue(projectJSON, Project.class);
	    	 } catch (JsonParseException e) {
	    		 Observer.toast("Error loading...", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchLoginActivity();
	    		 return;
	    	 } catch (JsonMappingException e) {
	    		 Observer.toast("Error loading...", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchLoginActivity();
	    		 return;
	    	 } catch (IOException e) {
	    		 Observer.toast("Error loading...", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchLoginActivity();
	    		 return;
	    	 }


	    	 Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
	    	 startActivity(intent);
	    	 
	     } else {

	    	 launchLoginActivity();
	    	
	     }
	}
	
	public void launchLoginActivity(){
		 Intent intent = new Intent("org.calflora.observer.action.LOGIN");
    	 startActivity(intent);
	}
	
	
	

}
