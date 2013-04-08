package org.calflora.observer;

import org.json.JSONException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ObservationSummaryFragment extends Fragment {

	private View layout = null;
	private GoogleMap map;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if(layout == null) {
			layout = inflater.inflate(R.layout.fragment_observation_summary, container, false);
			
			
			FragmentManager fragmentManager = getFragmentManager();

			// We should just create the map fragment programmatically, to customize controls
			// http://developer.android.com/reference/com/google/android/gms/maps/GoogleMapOptions.html
			MapFragment mapFragment = (MapFragment) fragmentManager.findFragmentById(R.id.observation_map);
			map = mapFragment.getMap();
			map.setMapType(1);
			map.setMyLocationEnabled(true);
		
			// Custom offline layer.
			// map.addTileOverlay(new TileOverlayOptions().tileProvider(new MapTileProvider(getResources().getAssets())));
		    
			LatLng latLng = new LatLng( Observer.getInstance().getLastLocation().getLatitude(), Observer.getInstance().getLastLocation().getLongitude());
			map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 12) );
						
			MarkerOptions options = new MarkerOptions();
			options.position(latLng);
			map.addMarker(options);
		}
		return layout;
	}
}
