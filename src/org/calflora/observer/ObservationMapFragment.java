package org.calflora.observer;

import java.util.Collection;

import net.smart_json_databsase.JSONEntity;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.Toast;




public class ObservationMapFragment extends MapFragment implements LocationListener {
	
	private GoogleMap map;
	private LocationManager locationManager;
	private String provider;
	private Location lastLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		
        
	}

	
	
	/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_observation_map, container, false);
	}
	*/
	
	@Override
	public void onStart() {
		super.onStart();
		
		map = this.getMap();
		map.setMapType(1);
		map.setMyLocationEnabled(true);
	
		map.addTileOverlay(new TileOverlayOptions().tileProvider(new MapTileProvider(getResources().getAssets())));
	    
		LatLng latLng = new LatLng(Observer.project.center_lat, Observer.project.center_lng);
		map.moveCamera( CameraUpdateFactory.newLatLng(latLng) );
		
		LocationManager service = (LocationManager) getActivity().getApplicationContext().getSystemService(Activity.LOCATION_SERVICE);
	    boolean enabledGPS = service
	                .isProviderEnabled(LocationManager.GPS_PROVIDER);
	    boolean enabledWiFi = service
	                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		
		 // Check if enabled and if not send user to the GSP settings
        // Better solution would be to display a dialog and suggesting to 
        // go to the settings
        if (!enabledGPS) {
            Toast.makeText(getActivity(), "GPS signal not found", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        
        locationManager = service;

        // Define the criteria how to select the locatioin provider -> use
        // default
        Criteria criteria = new Criteria();
        provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        
        // Initialize the location fields
        if (location != null) {
            Toast.makeText(getActivity(), "Selected Provider " + provider,
                    Toast.LENGTH_SHORT).show();
            onLocationChanged(location);
        } else {

            //do something
        }
		
		
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
			Observer.toast("Bad point data", getActivity().getApplicationContext());
			e.printStackTrace();
			return;
		}
		options.position(latLng);
		map.addMarker(options);
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
