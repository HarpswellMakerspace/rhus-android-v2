package org.calflora.observer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.smart_json_database.InitJSONDatabaseExcepiton;
import net.smart_json_database.JSONDatabase;

import org.calflora.observer.api.ObserverAPI;
import org.calflora.observer.model.Observation;
import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Observer extends Application implements LocationListener {
	
	public static final String API_KEY_PREF = "APIKey";

	public static final String USER_EMAIL_PREF = "UserEmail";

	private static final String DB_NAME = "px137.sqlite"; //Hard coded for testing, this is Yosemite
	//private static final String DB_NAME = "test.sqlite"; //Hard coded for testing, this is Yosemite
	private static final String ALL_CALIFORNIA_DB_NAME = "px0.sqlite"; //Hard coded for testing, this is Yosemite

	
	public static final String ORGANIZATION_PREFERENCE = "ORGANIZATION_PREFERENCE";
	public static final String PROJECT_PREFERENCE = "PROJECT_PREFERENCE";

	public static ObserverAPI observerAPI;
	public static SharedPreferences settings;	
	public static Observer instance;
	private static Organization organization;
	private static Project project;

	public static ObjectMapper mapper = new ObjectMapper();
	public static JSONDatabase database;
	public static SQLiteDatabase plantsListDatabase; // TODO: move to Project ?
	public static SQLiteDatabase allPlantsListDatabase; 

	
	public static Observation currentObservation; // The observation being worked on
													// TODO: this needs to be saved and restored if app is interrupted.
	
	private String username;
	private String APIKey;
	
	private LocationManager locationManager;
	private String provider;
	private Location lastLocation;
	private AssetManager assets;

	
	public static final String NEW_PLANT_TAXON = "org.calflora.observer.new_plant_taxon";
	
	public static Observer getInstance() {
		return instance;
	}
	
	public static void toast(String message, Context context){
		
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, message, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		
	}
	
	public static void unhandledErrorToast(String message, Context context){
		
		int duration = Toast.LENGTH_LONG;

		Toast toast = Toast.makeText(context, message, duration);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
		
	}
	
	public Drawable getThumbnailForPlant(String id) throws IOException{
		AssetFileDescriptor asset = null;
		String imagePath = "plant_images/" + id + ".jpeg";
		asset = assets.openFd(imagePath);
		Drawable plantThumbnail = Drawable.createFromStream(asset.createInputStream(), ""); 
		return plantThumbnail;
	}
	
	public String getAPIKey(){
		return APIKey;
	}
	

	// Application overrides
	@Override
	public void onCreate() {

		super.onCreate();
		
		assets = getBaseContext().getResources().getAssets();

		
		instance = this;
		observerAPI = new ObserverAPI();
		settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

		username = settings.getString(USER_EMAIL_PREF, null);
		APIKey = settings.getString(API_KEY_PREF, null);
		/*
		String projectJSON = settings.getString(Observer.PROJECT_PREFERENCE, null);
		String organizationJSON = settings.getString(Observer.ORGANIZATION_PREFERENCE, null);

		if(username != null && APIKey != null){
			if(organizationJSON != null){
				try {
					Organization o = Observer.mapper.readValue(organizationJSON, Organization.class);
					organization = o;
				} catch (Exception e) {
					organization = null;
				}
			}
			if(projectJSON != null){
				try {
					Project p = Observer.mapper.readValue(projectJSON, Project.class);
					project = p;
				} catch (Exception e) {
					project = null;
				}
			}
		}*/
		
		try {
			Observer.database = JSONDatabase.GetDatabase(getApplicationContext());
		} catch (InitJSONDatabaseExcepiton e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		if( sharedpreferences.getBoolean("firstLaunch", false)) {
			try {
				copyDataBase(DB_NAME);  // TODO Shouldn't do this on every load, but OK for now.
				copyDataBase(ALL_CALIFORNIA_DB_NAME);  // TODO Shouldn't do this on every load, but OK for now.
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sharedpreferences.edit().putBoolean("firstLaunch", true).commit();
		
		
		Observer.plantsListDatabase = SQLiteDatabase.openDatabase(getApplicationContext().getDatabasePath(DB_NAME).getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		Observer.allPlantsListDatabase = SQLiteDatabase.openDatabase(getApplicationContext().getDatabasePath(ALL_CALIFORNIA_DB_NAME).getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);

		
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
	    boolean enabledGPS = service
	                .isProviderEnabled(LocationManager.GPS_PROVIDER);
	    boolean enabledWiFi = service
	                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		 // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to 
        // go to the settings
        if (!enabledGPS) {
            Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        
        // Initialize the location fields
        if (location != null) {
            Toast.makeText(this, "Selected Provider " + provider,
                    Toast.LENGTH_SHORT).show();
            onLocationChanged(location);
        }
        
        // TODO: use only GPS provider
        //locationManager.requestLocationUpdates(provider, 60000, 5, this);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		
	}
	
	 private void copyDataBase(String dbName) throws IOException{
		 
	    	//Open your local db as the input stream
	    	InputStream myInput = getApplicationContext().getAssets().open(dbName);
	 
	    	// Path to the just created empty db
	    	File outFileName = getApplicationContext().getDatabasePath(dbName);
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream( outFileName.getAbsolutePath() );
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	 }
	

	 // Location Manager
	 @Override
	 public void onLocationChanged(Location location) {
		 lastLocation = location;
	 }

	 @Override
	 public void onProviderDisabled(String provider) {
		 // TODO Auto-generated method stub

	 }

	 @Override
	 public void onProviderEnabled(String provider) {
		 // TODO Auto-generated method stub

	 }

	 @Override
	 public void onStatusChanged(String provider, int status, Bundle extras) {
		 // TODO Auto-generated method stub

	 }
	 
	 public Location getLastLocation(){
		 return lastLocation;
	 }

	 public void setOrganization(Organization organizationObject) {
		organization = organizationObject;
		
		String organizationJSON = null;
		try {
			organizationJSON = Observer.mapper.writeValueAsString(organization);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//And set it in the preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Observer.ORGANIZATION_PREFERENCE, organizationJSON );
		boolean bCommitted = editor.commit();
		if (!bCommitted) 
	        throw new RuntimeException("(AndroidApplication) Unable to save new string.");

	}
	 
	public Organization getCachedOrganizationData(String id){
		String orgJSON = "{\"status\":\"OK\", \"data\":{ \"orgId\":42, \"splashGraphic\":\"http://www.nps.gov/yose/naturescience/images/torch-web_1.jpg\", \"logoGraphic\":\"http://www.yosemiteconservancy.org/sites/all/themes/yosemite/images/logo.gif\", \"orgUrl\":\"http://www.yosemiteconservancy.org/\", \"projects\": [ {\"id\":\"pr1\", \"name\":\"Weed Inventory 2012\"}, {\"id\":\"pr2\", \"name\":\"Weed Inventory 2013\"} ]}}";
		
		try {
			return Observer.mapper.readValue(orgJSON, Organization.class);
		} catch (Exception e){
			e.printStackTrace();
			return null;
		}
	}
	
	public Organization getOrganization(){
		return organization;
	}

	public void setUser(String email, String newAPIKey){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(API_KEY_PREF, newAPIKey);
		editor.putString(USER_EMAIL_PREF,  email);
		username = email;
		APIKey = newAPIKey;
		boolean bCommitted = editor.commit();
		if (!bCommitted) 
	        throw new RuntimeException("(AndroidApplication) Unable to save new string.");
		return;

	}
	
	public void forgetUser(){
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.remove(API_KEY_PREF);
		editor.remove(USER_EMAIL_PREF);
		editor.remove(PROJECT_PREFERENCE);
		editor.remove(ORGANIZATION_PREFERENCE);
		boolean bCommitted = editor.commit();
		if (!bCommitted) 
	        throw new RuntimeException("(AndroidApplication) Unable to save new string.");
		return;
	}
	

	 public void setProject(Project projectObject) {
		project = projectObject;
	
		String projectJSON = null;
		try {
			projectJSON = Observer.mapper.writeValueAsString(project);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//And set it in the preferences
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(Observer.PROJECT_PREFERENCE, projectJSON);
		boolean bCommitted = editor.commit();
		if (!bCommitted) 
	        throw new RuntimeException("(AndroidApplication) Unable to save new string.");

	}
	
	public Project getProject(){
		return project;
	}
	
	public String getCurrentUsername(){
		return username;
	}
	

}
