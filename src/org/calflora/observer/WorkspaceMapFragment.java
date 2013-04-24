package org.calflora.observer;

import java.util.Collection;

import net.smart_json_database.JSONEntity;
import net.smart_json_database.SearchFields;

import org.calflora.map.OfflineMapTileProvider;
import org.json.JSONException;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import android.location.LocationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle bundle) {
	    View mapView = super.onCreateView(inflater, viewGroup, bundle);
	    RelativeLayout view = new RelativeLayout(getActivity());
	    view.addView(mapView, new RelativeLayout.LayoutParams(-1, -1));
	    // working with view
	    
	    ImageButton button = new ImageButton(getActivity());
		button.setImageResource(R.drawable.layers);
	    
	    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-1, -1);
	    params.rightMargin = 120;
	    params.bottomMargin = 50;
	    params.width = 100;
	    params.height= 100;
	    params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
	    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
	    //button.setPadding(0, 0, 0, 40);
	    view.addView(button, params);
	    
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
			map.addTileOverlay(new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "yosemiteoffice")));
			
			// Custom offline layer.
			//TileOverlayOptions tp = new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "true_marble_california"));
			//map.addTileOverlay(tp);

			LatLng latLng = new LatLng(Observer.instance.getProject().center_lat, Observer.instance.getProject().center_lng);
			// -119.784,37.6747 Yosemite
			latLng = new LatLng(37.6747, -119.784);
			map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 13) );

			addMarkersFromDatabase();
		} else {
			Observer.toast("Google Maps Not Available", getActivity());
		}
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
