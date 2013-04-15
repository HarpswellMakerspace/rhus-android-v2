package org.calflora.observer;


import java.io.ByteArrayOutputStream;

import net.winterroot.rhus.util.RHImage;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;

public class ObservationSummaryFragment extends Fragment {

	private View layout = null;
	private GoogleMap map;
	private MapFragment mapFragment;
	@SuppressWarnings("unused")
	private String TAG = "ObservationSummaryFragment";
	
	private int CAPTURE_PHOTO = 1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if(layout == null) {
			layout = inflater.inflate(R.layout.fragment_observation_summary, container, false);
			
			// We should just create the map fragment programmatically, to customize controls
			// http://developer.android.com/reference/com/google/android/gms/maps/GoogleMapOptions.html
			GoogleMapOptions options =  new GoogleMapOptions();
			options.zoomControlsEnabled(false);
			options.zoomGesturesEnabled(false);
			mapFragment = MapFragment.newInstance(options);
			
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			transaction.add(R.id.observation_map_layout, mapFragment);
			transaction.commit();

			
		

		}
		return layout;
	}

	
	
	@Override
	public void onStart() {
		super.onStart();
	
		// TODO Is this the right place for this in the lifecycle?
		map = mapFragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		map.setMyLocationEnabled(true);
	
		// Custom offline layer.
		// map.addTileOverlay(new TileOverlayOptions().tileProvider(new MapTileProvider(getResources().getAssets())));
	    
		LatLng latLng = new LatLng( Observer.getInstance().getLastLocation().getLatitude(), Observer.getInstance().getLastLocation().getLongitude());
		map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 13) );
					
		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		map.addMarker(markerOptions);
		
		Button captureImageButton = (Button) getView().findViewById(R.id.plant_photo_image_button);
		captureImageButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				
				 Intent intent = new Intent("org.calflora.observer.action.CAPTUREPHOTO");
				 startActivityForResult(intent, CAPTURE_PHOTO);	
				 
			}
		});
		
		
	}



	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}
	

	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(!(resultCode == Activity.RESULT_OK)){
			Observer.toast("Error capturing image: result code is not OK", getActivity());
			return;
		}
		
		
		try
		{
			switch (requestCode) {
			case 1: //CAPTURE_PHOTO
				if (resultCode == Activity.RESULT_OK)
				{
					Bundle data =  intent.getExtras();
					if(data == null){
						Observer.toast("Error capturing image", getActivity());
						return;
					}
					
					String photoFileName = data.getString(CapturePhotoActivity.PHOTO_FILE_NAME);		

					Button photoButton = (Button) getView().findViewById(R.id.plant_photo_image_button);
	
					Bitmap thumb = RHImage.resizeBitMapImage(photoFileName, 140, 120, 0);
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					thumb.compress(Bitmap.CompressFormat.PNG, 100, stream);
					byte[] thumbBytes = stream.toByteArray();
					
					Observer.currentObservation.addAttachment("thumbnail", thumbBytes, "image/jpeg", getActivity());
					Observer.currentObservation.addAttachment("file", thumbBytes, "image/jpeg", getActivity()); // TODO this is just for testing

					
					if (photoButton != null)
						photoButton.setBackgroundDrawable(null);// free mem from last photo
					photoButton.setBackgroundDrawable( new BitmapDrawable(thumb) );
					photoButton.setText("");
					
				}
				break;
			}
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			Observer.toast("trouble saving file" + ex, getActivity());
		}
	}

	
	
}
