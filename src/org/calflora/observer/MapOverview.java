package org.calflora.observer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import net.smart_json_databsase.InitJSONDatabaseExcepiton;
import net.smart_json_databsase.JSONDatabase;
import net.smart_json_databsase.JSONEntity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.MapFragment;

public class MapOverview extends Activity implements LocationListener {

	private static int ZOOM = 9;
	
	private GoogleMap map;
	private LocationManager locationManager;
	private String provider;
	private Location lastLocation;
	
	public MapOverview() {
		super();
		/*
		this.mContext = mContext;
	    String context=Context.LOCATION_SERVICE;
	    lm = (LocationManager)mContext.getSystemService(context);
	    lm.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER,MINIMUM_TIME_BETWEEN_UPDATES,MINIMUM_DISTANCE_CHANGE_FOR_UPDATES,this);
*/
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map_overview);
		map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
		map.setMapType(1);
		map.setMyLocationEnabled(true);
	
		map.addTileOverlay(new TileOverlayOptions().tileProvider(new MapTileProvider(getResources().getAssets())));
	    
		LatLng latLng = new LatLng(Observer.project.center_lat, Observer.project.center_lng);
		map.moveCamera( CameraUpdateFactory.newLatLng(latLng) );
		
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
        } else {

            //do something
        }
        
        
        ((Button) findViewById(R.id.addPointButton)).setOnClickListener(
        		new OnClickListener(){
        			public void onClick(View v){
        				//Map<String,Object> dataPoint = new HashMap<String,Object>();
        				JSONEntity dataPoint = new JSONEntity();
        				try {
							dataPoint.put("latitude", lastLocation.getLatitude());
	        				dataPoint.put("longitude", lastLocation.getLongitude());
						} catch (JSONException e1) {
							Observer.toast("JSON Failed", getApplicationContext());
							e1.printStackTrace();
							return;
						}
        				
        				//And insert into JSON Datastore
        				//In the future this will got into 'Observer' as part of 'newObservation' for staging.
        				
        				int id = Observer.database.store(dataPoint);
        				addMarker(dataPoint);
        				
        			}
        		}
        		);
	}
	
	
	
	
	@Override
	protected void onStart() {
		super.onStart();
		addMarkersFromDatabase();
	}

	public void addMarkersFromDatabase(){
		Collection<JSONEntity> points = Observer.database.fetchAllEntities();
		for( JSONEntity p: points){
			addMarker(p);
		}
		
	}
	
	public void addMarker(JSONEntity p){ // TODO: Probably want to pass a Observation type, not a JSONEntity
		MarkerOptions options = new MarkerOptions();
		LatLng latLng;
		try {
			latLng = new LatLng(p.getDouble("latitude"), p.getDouble("longitude"));
		} catch (JSONException e) {
			Observer.toast("Bad point data", getApplicationContext());
			e.printStackTrace();
			return;
		}
		options.position(latLng);
		map.addMarker(options);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.map_overview, menu);
		return true;
	}

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

}
