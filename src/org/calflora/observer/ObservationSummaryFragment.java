package org.calflora.observer;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.winterroot.rhus.util.DWUtilities;
import net.winterroot.rhus.util.RHImage;

import org.calflora.map.OfflineMapTileProvider;
import org.calflora.observer.model.Observation;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;

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

	Boolean lockUI = false;
	Object lock = new Object();


	public boolean hasImageCaptureBug() {

		// list of known devices that have the bug
		ArrayList<String> devices = new ArrayList<String>();
		devices.add("android-devphone1/dream_devphone/dream");
		devices.add("generic/sdk/generic");
		devices.add("vodafone/vfpioneer/sapphire");
		devices.add("tmobile/kila/dream");
		devices.add("verizon/voles/sholes");
		devices.add("google_ion/google_ion/sapphire");

		return devices.contains(android.os.Build.BRAND + "/" + android.os.Build.PRODUCT + "/"
				+ android.os.Build.DEVICE);

	}

	@Override
	public void onStart() {
		super.onStart();

		// TODO Is this the right place for this in the lifecycle?
		map = mapFragment.getMap();
		map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
		map.setMyLocationEnabled(true);

		// Custom offline layer.
		map.addTileOverlay(new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "yosemiteoffice")));

		LatLng latLng = new LatLng( Observer.getInstance().getLastLocation().getLatitude(), Observer.getInstance().getLastLocation().getLongitude());
		map.moveCamera( CameraUpdateFactory.newLatLngZoom(latLng, 13) );

		MarkerOptions markerOptions = new MarkerOptions();
		markerOptions.position(latLng);
		markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW));
		map.addMarker(markerOptions);

		Button captureImageButton = (Button) getView().findViewById(R.id.plant_photo_image_button);
		captureImageButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){

				synchronized(lock) {

					if(lockUI){
						return;
					}

					if(!lockUI){
						lockUI = true;
					}

				}

				Intent intent = new Intent("org.calflora.observer.action.CAPTUREPHOTO");
				startActivityForResult(intent, CAPTURE_PHOTO);  



			}
		});


	}



	@Override
	public void onStop() {
		super.onStop();

		synchronized(lock) {
			lockUI = false;
		}
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

					ImageView photoThumb = (ImageView) getView().findViewById(R.id.plant_image_thumbnail);
					Button photoButton = (Button) getView().findViewById(R.id.plant_photo_image_button);

					Bitmap bitmap = BitmapFactory.decodeFile(photoFileName);
					Bitmap rotatedBitmap = RHImage.rotateImage(bitmap);
					
					// Get the full quality image bytes
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] plantImageBytes = stream.toByteArray();
					
					//Now resize a thumbnail
					Bitmap square;
					if (rotatedBitmap.getWidth() >= rotatedBitmap.getHeight()){

						square = Bitmap.createBitmap(
								rotatedBitmap, 
								rotatedBitmap.getWidth()/2 - rotatedBitmap.getHeight()/2,
								0,
								rotatedBitmap.getHeight(), 
								rotatedBitmap.getHeight()
								);

					} else {

						square = Bitmap.createBitmap(
								rotatedBitmap,
								0, 
								rotatedBitmap.getHeight()/2 - rotatedBitmap.getWidth()/2,
								rotatedBitmap.getWidth(),
								rotatedBitmap.getWidth() 
								);
					}

					Matrix matrix = new Matrix();
					
					// RESIZE THE BIT MAP
	
					// According to a variety of resources, this function should give us pixels from the dp of the screen
					// From http://stackoverflow.com/questions/4605527/converting-pixels-to-dp-in-android
					float targetHeight = DWUtilities.convertDpToPixel(80, getActivity());
					float targetWidth = DWUtilities.convertDpToPixel(80, getActivity());
					
					// However, the above pixel dimension are still too small to show in my 80dp image view
					// On the Nexus 4, a factor of 4 seems to get us up to the right size
					// No idea why.
					targetHeight *= 4;
					targetWidth *= 4;
										
					matrix.postScale( (float) targetHeight / square.getWidth(), (float) targetWidth / square.getHeight());
					Bitmap resizedBitmap = Bitmap.createBitmap(square, 0, 0, square.getWidth(), square.getHeight(), matrix, false); 
					
					// By the way, the below code also gives a full size, but blurry image
					// Bitmap resizedBitmap = Bitmap.createScaledBitmap(square, (int) targetWidth, (int) targetHeight, false);
					
					// NOTE the density of the image is not necessarily equivalent to the screen density
					
					// Get the bytes for storage
					stream = new ByteArrayOutputStream();
					resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
					byte[] thumbBytes = stream.toByteArray();

					// Add the attachments
					Observer.currentObservation.addAttachment("thumbnail", thumbBytes, "image/jpeg", getActivity());
					Observer.currentObservation.addAttachment("photo1", plantImageBytes, "image/jpeg", getActivity()); 


					// Update the UI
					if (photoButton != null)
						photoThumb.setImageDrawable(null);
					String thumbnailPath = Observer.currentObservation.getAttachmentPath("thumbnail", getActivity());
					if(thumbnailPath != null){
						Drawable d = Drawable.createFromPath(thumbnailPath);
						if(d != null){
							photoThumb.setImageDrawable(d);
						}
					}

					photoButton.setText("");

				}
				break;		

			}

		}

		catch (Throwable ex)
		{
			ex.printStackTrace();
			Observer.toast("trouble saving file: " + ex, getActivity());
		}
	}
}


