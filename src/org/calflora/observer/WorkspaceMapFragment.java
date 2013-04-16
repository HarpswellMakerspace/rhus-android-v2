package org.calflora.observer;

import java.util.Collection;

import net.smart_json_databsase.JSONEntity;
import net.smart_json_databsase.SearchFields;

import org.calflora.map.OfflineMapTileProvider;
import org.json.JSONException;


import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import android.location.LocationListener;
import android.widget.Toast;




public class WorkspaceMapFragment extends MapFragment {
	
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
		
		// TODO change to adding this map programatically, so we get the zoom level setting
		map = this.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		//map.setMapType(GoogleMap.MAP_TYPE_NONE);

		map.setMyLocationEnabled(true);
		
	
		// Custom offline layer.
		map.addTileOverlay(new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "yosemite_valley")));
	    
		LatLng latLng = new LatLng(Observer.instance.getProject().center_lat, Observer.instance.getProject().center_lng);
		map.moveCamera( CameraUpdateFactory.newLatLng(latLng) );

		addMarkersFromDatabase();
	}

	public void addMarkersFromDatabase(){
		
		SearchFields search = SearchFields.Where("type", "observation");
		Collection<JSONEntity> points = Observer.database.fetchByFields(search);
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
		try {
			options.title(p.getString("taxon"));
			options.snippet(p.getString("taxon"));
			int uploaded = p.getInt("uploaded");
			if(uploaded==1){
				options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		options.position(latLng);
		map.addMarker(options);
	}


}
