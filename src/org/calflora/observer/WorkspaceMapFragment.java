package org.calflora.observer;

import java.util.Collection;

import net.smart_json_database.JSONEntity;
import net.smart_json_database.SearchFields;

import org.calflora.map.OfflineMapTileProvider;
import org.json.JSONException;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.VisibleRegion;




public class WorkspaceMapFragment extends MapFragment {
	
	private static final String PREFS_NAME = null;
	private GoogleMap map;
	private LocationManager locationManager;
	private String provider;
	private Location lastLocation;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Clear saved span
	    SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        // Necessary to clear first if we save preferences onPause. 
        editor.clear();
        editor.putFloat("left", 0);
        editor.putFloat("top", 0);
        editor.putFloat("right", 0);
        editor.putFloat("bottom", 0);
        editor.commit();
	}
	
	ImageButton button;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
	    View mapView = super.onCreateView(inflater, viewGroup, bundle);
	    RelativeLayout view = new RelativeLayout(getActivity());
	    view.addView(mapView, new RelativeLayout.LayoutParams(-1, -1));
	    // working with view
	    
	    // button = new ImageButton(getActivity());
		// button.setImageResource(R.drawable.disk_tri);
	    /*
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
	    float density = getResources().getDisplayMetrics().density;
	    params.rightMargin = (int) (12 * density);
	    params.topMargin =  (int) (57 * density);
	    params.width = (int) (38*density);
	    params.height= (int) (38*density);
	    //params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    //button.setPadding(0, 0, 0, 40);
	    view.addView(button, params);
	    */
	    return view;
	}
	
	
	@Override
	public void onStart() {
		super.onStart();
		
		// http://developer.android.com/reference/com/google/android/gms/maps/GoogleMapOptions.html
		/*
		GoogleMapOptions options =  new GoogleMapOptions();
		options.zoomControlsEnabled(false);
		options.zoomGesturesEnabled(false);
		mapFragment = MapFragment.newInstance(options);

		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();
		transaction.add(R.id.observation_map_layout, mapFragment);
		transaction.commit();
		 */

		if(ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity() ) ){
			// TODO change to adding this map programatically, so we get the zoom level setting
			map = this.getMap();
			map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			//map.setMapType(GoogleMap.MAP_TYPE_NONE);

			map.setMyLocationEnabled(true);

			// Custom offline layer.
			//map.addTileOverlay(new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "yosemiteoffice")));

			// Custom offline layer.
			//TileOverlayOptions tp = new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "true_marble_california"));
			//map.addTileOverlay(tp);

			addMarkersFromDatabase();
			
		} else {
			Observer.toast("Google Maps Not Available", getActivity());
		}
		
		/*
		button.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				LatLng latLng = new LatLng(Observer.instance.getProject().center_lat, Observer.instance.getProject().center_lng);
				latLng = new LatLng(37.6747, -119.784);
				map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 13) );
			}
			
		});
		*/
		
	
	}

	
	@Override
	public void onPause() {
		super.onPause();
		
		VisibleRegion vr = map.getProjection().getVisibleRegion();
		double left = vr.latLngBounds.southwest.longitude;
		double top = vr.latLngBounds.northeast.latitude;
		double right = vr.latLngBounds.northeast.longitude;
		double bottom = vr.latLngBounds.southwest.latitude;
		
	    SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME,0);
        SharedPreferences.Editor editor = settings.edit();
        // Necessary to clear first if we save preferences onPause. 
        editor.clear();
        editor.putFloat("left", (float) left);
        editor.putFloat("top", (float) top);
        editor.putFloat("right", (float) right);
        editor.putFloat("bottom", (float) bottom);
        editor.commit();
	}

	
	
	@Override
	public void onResume() {
		super.onResume();
		
		if(ConnectionResult.SUCCESS == GooglePlayServicesUtil.isGooglePlayServicesAvailable(getActivity() ) ){

		    SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME,0);
		    float left = settings.getFloat("left", 0);
		    float top = settings.getFloat("top", 0);
		    float right = settings.getFloat("right", 0);
		    float bottom = settings.getFloat("bottom", 0);

		    if(left != 0 && top != 0 && right != 0 && bottom != 0){
		    	// if we saved a bounds, use it.
		    	LatLng southWest = new LatLng(bottom, left);
		    	LatLng northEast = new LatLng(top, right);
		    	LatLngBounds latLngBounds = new LatLngBounds(southWest, northEast);
		    	map.moveCamera( CameraUpdateFactory.newLatLngBounds(latLngBounds, 0) );
		    	
		    } else if(Observer.instance.getLastLocation() != null){	
				//if we have a geofix, move the current users last known locatioin
				Location lc = Observer.instance.getLastLocation();
				LatLng latLng = new LatLng(lc.getLatitude(), lc.getLongitude());
				map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 13) );

			} else {
				// and if we don't have either, move to the project center
				// -119.784,37.6747 Yosemite
				LatLng latLng = new LatLng(Observer.instance.getProject().center_lat, Observer.instance.getProject().center_lng);
				latLng = new LatLng(37.6747, -119.784);
				map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 13) );
			}
		}


	}

	public void addMarkersFromDatabase(){
		
		SearchFields search = SearchFields.Where("type", "observation");//.Where("uploaded", 0);
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
		} catch (JSONException e) {
			options.title("No Taxon Recorded");
		}
		
		try {
			int uploaded = p.getInt("uploaded");
			if(uploaded==1){
				options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
			} else {
				options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		options.position(latLng);
		map.addMarker(options);
	}


}
