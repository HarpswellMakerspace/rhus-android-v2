package org.calflora.observer;


import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import net.winterroot.rhus.util.DWUtilities;

import org.calflora.map.OfflineMapTileProvider;
import org.calflora.observer.model.Observation;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
		// map.addTileOverlay(new TileOverlayOptions().tileProvider(new OfflineMapTileProvider(getResources().getAssets(), "yosemiteoffice")));

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

				Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				/*
				if (hasImageCaptureBug()) {
					i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/tmp")));
				} else {
					Uri uri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
					i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, uri);
				}
				*/	
				startActivityForResult(i, CAPTURE_PHOTO); 



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
					
					// Solution from : http://kevinpotgieter.wordpress.com/2011/03/30/null-intent-passed-back-on-samsung-galaxy-tab/

					// Describe the columns you'd like to have returned. Selecting from the Thumbnails location gives you both the Thumbnail Image ID, as well as the original image ID
					String[] projection = {
							MediaStore.Images.Thumbnails._ID,  // The columns we want
							MediaStore.Images.Thumbnails.IMAGE_ID,
							MediaStore.Images.Thumbnails.KIND,
							MediaStore.Images.Thumbnails.DATA};
					String selection = MediaStore.Images.Thumbnails.KIND + "="  + // Select only mini's
							MediaStore.Images.Thumbnails.MINI_KIND;

					String sort = MediaStore.Images.Thumbnails._ID + " DESC";

					// From stack overflow: At the moment, this is a bit of a hack, as I'm returning ALL images, and just taking the latest one. There is a better way to narrow this down I think with a WHERE clause which is currently the selection variable
					// TODO should use a Loader
					Cursor myCursor = getActivity().managedQuery(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, projection, selection, null, sort);

					long imageId = 0l;
					long thumbnailImageId = 0l;
					String thumbnailPath = "";

					try{
						myCursor.moveToFirst();
						imageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.IMAGE_ID));
						thumbnailImageId = myCursor.getLong(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails._ID));
						thumbnailPath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA));
					}
					finally{myCursor.close();}

					//Create new Cursor to obtain the file Path for the large image

					String[] largeFileProjection = {
							MediaStore.Images.ImageColumns._ID,
							MediaStore.Images.ImageColumns.DATA
					};

					String largeFileSort = MediaStore.Images.ImageColumns._ID + " DESC";
					myCursor = getActivity().managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, largeFileProjection, null, null, largeFileSort);
					String largeImagePath = "";

					try{
						myCursor.moveToFirst();

						//This will actually give you the file path location of the image.
						largeImagePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA));
					}
					finally{myCursor.close();}
					// These are the two URI's you'll be interested in. They give you a handle to the actual images
					Uri uriLargeImage = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(imageId));
					Uri uriThumbnailImage = Uri.withAppendedPath(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, String.valueOf(thumbnailImageId));

					// I've left out the remaining code, as all I do is assign the URI's to my own objects anyways...

					String photoFileName = DWUtilities.getRealPathFromURI(getActivity(), uriLargeImage);
					byte[] plantImageBytes = Observation.createFullImageBytes(photoFileName);
					
					// MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), photoFileName, "Image from Calflora", "Image from Calflora");
					Observer.currentObservation.addAttachment("photo1", plantImageBytes, "image/jpeg", getActivity()); 

					Button photoButton = (Button) getView().findViewById(R.id.plant_photo_image_button);

					/*
					if (photoButton != null)
						photoButton.setBackgroundDrawable(null);// free mem from last photo

					try {
						String thumbnailPath = Observer.currentObservation.getAttachmentPath("thumbnail", getActivity());
						if(thumbnailPath != null){
							Drawable d = Drawable.createFromPath(thumbnailPath);
							if(d != null){
								photoButton.setBackgroundDrawable(d);
							}
						}
					} catch (FileNotFoundException e){
						// Do nothing
					}
					*/
					
					photoButton.setText("");


				/*
					Uri u = null;
					if (hasImageCaptureBug()) {
						File fi = new File("/sdcard/tmp");
						try {
							u = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), fi.getAbsolutePath(), null, null));
							if (!fi.delete()) {
								Log.i("logMarker", "Failed to delete " + fi);
							}
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						}
					} else {
						u = intent.getData();
					}
					if( u == null) {
						throw new Exception("Image not found");
					}

					String photoFileName = DWUtilities.getRealPathFromURI(getActivity(), u);

			        //Bitmap photo = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), u);

					Button photoButton = (Button) getView().findViewById(R.id.plant_photo_image_button);

					//byte[] thumbBytes = Observation.createThubmnailBytes(photo);
					byte[] plantImageBytes = Observation.createFullImageBytes(photoFileName);


					//Observer.currentObservation.addAttachment("thumbnail", thumbBytes, "image/jpeg", getActivity());
					Observer.currentObservation.addAttachment("photo1", plantImageBytes, "image/jpeg", getActivity()); // TODO this is just for testing

					MediaStore.Images.Media.insertImage(getActivity().getContentResolver(), photoFileName, "Image from Calflora", "Image from Calflora");

					if (photoButton != null)
						photoButton.setBackgroundDrawable(null);// free mem from last photo

					try {
						String thumbnailPath = Observer.currentObservation.getAttachmentPath("thumbnail", getActivity());
						if(thumbnailPath != null){
							Drawable d = Drawable.createFromPath(thumbnailPath);
							if(d != null){
								photoButton.setBackgroundDrawable(d);
							}
						}
					} catch (FileNotFoundException e){
						// Do nothing
					}
				 */

				

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
