package org.calflora.observer;

import java.io.IOException;

import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
	    	 String organizationJSON = settings.getString("organization", null);
	    	 if(projectJSON == null){
	    		 // Reselect organization and project
	    		 launchOrganizationActivity();
	    		 return;
	    	 }
	    	 
	    	 try {
	    		 Observer.instance.setProject(Observer.mapper.readValue(projectJSON, Project.class) );
	    		 Observer.instance.setOrganization(Observer.mapper.readValue(organizationJSON, Organization.class) );
	    	 } catch (JsonParseException e) {
	    		 Observer.toast("Error loading...", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchOrganizationActivity();
	    		 return;
	    	 } catch (JsonMappingException e) {
	    		 Observer.toast("Error loading...", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchOrganizationActivity();
	    		 return;
	    	 } catch (IOException e) {
	    		 Observer.toast("Error loading...", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchOrganizationActivity();
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
	
	public void launchOrganizationActivity(){
		 Intent intent = new Intent("org.calflora.observer.action.ORGANIZATIONS");
		 startActivity(intent);
	}
	
	

}
