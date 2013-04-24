package org.calflora.observer;

import java.io.IOException;

import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ImageView;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		
	}

	

	@Override
	protected void onResume() {
		super.onResume();
		
	     SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
	     String APIKey = settings.getString(Observer.API_KEY_PREF, null);
	     String email = settings.getString(Observer.USER_EMAIL_PREF, null);

	     if(APIKey != null && email != null ){
	    	 //TODO: Revalidate API key ??
	    	 //Remember that they may be offline
	    	
	    	 //TODO: And check to see that they have an organization and project selected
	    	 //If so, we just take them them to the Dashboard or Map
	    	 
	    	 //First load the organization and project!
	    	 String projectJSON = settings.getString(Observer.PROJECT_PREFERENCE, null);
	    	 String organizationJSON = settings.getString(Observer.ORGANIZATION_PREFERENCE, null);
	    	 if(projectJSON == null || organizationJSON == null || projectJSON.equals("null") || organizationJSON.equals("null") ){
	    		 // Reselect organization and project
	    		 launchOrganizationActivity();
	    		 return;
	    	 }
	    	 
	    	 try {
	    		 Project project = Observer.mapper.readValue(projectJSON, Project.class);
	    		 Organization organization = Observer.mapper.readValue(organizationJSON, Organization.class);
	    		 Observer.instance.setProject(project);
	    		 Observer.instance.setOrganization(organization);
	    		 
	    		 
	    		String splashImagePath = Observer.instance.getOrganization().getSplashGraphicPath();
	    		if(splashImagePath != null){
	    			ImageView splashImageView = (ImageView) findViewById(R.id.splashImage);
	    			
	    			Drawable splashImage = null;
	    			try {
	    				splashImage = Drawable.createFromPath(Observer.instance.getOrganization().getSplashGraphicPath()); 
	    			} catch (Exception e){
	    				// no splash image available
	    				// should show teh default, actually getOrganization() shoudl just return this.
	    			}
					if(splashImage != null){
						splashImageView.setImageDrawable(splashImage);
					}
	    		}
	    		 
	    	 } catch (JsonParseException e) {
	    		 Observer.toast("Error loading cached organization/project", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchOrganizationActivity();
	    		 return;
	    	 } catch (JsonMappingException e) {
	    		 Observer.toast("Error loading cached organization/project - json mapping exception", getApplicationContext());
	    		 e.printStackTrace();
	    		 launchOrganizationActivity();
	    		 return;
	    	 } catch (IOException e) {
	    		 Observer.toast("Error loading cached organization/project", getApplicationContext());
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
