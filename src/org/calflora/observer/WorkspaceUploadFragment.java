package org.calflora.observer;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.calflora.observer.api.APIResponseUpload;
import org.calflora.observer.model.Observation;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.exception.NoNetworkException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import net.smart_json_database.JSONEntity;
import net.smart_json_database.Order;
import net.smart_json_database.SearchFields;
import net.winterroot.android.wildflowers.R;
import net.winterroot.rhus.api.RhusApi;
import net.winterroot.rhus.api.RhusApiResponse;
import net.winterroot.rhus.util.DWHostUnreachableException;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class WorkspaceUploadFragment extends WorkspaceListFragment {

	ProgressBar progressBar;
	//ProgressDialog progressDialog;

	private Iterator<JSONEntity> uploadIterator;
	private int currentPosition;
	private int pendingObservations;
	private Button uploadButton;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_workspace_upload, container, false);
	}
	
	
	public void onStart() {
		super.onStart();
		
		uploadButton = (Button) getView().findViewById(R.id.upload_button);
		progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);

		uploadButton.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				doUpload();
			}


		}
				);

		uploadButton.setText("Start Upload");
		uploadButton.setEnabled(true);

	}
	
	public void onResume(){
		super.onResume();
		progressBar.setProgress( 0 );
		
		updateUploadButton();
		

	}
	
	@Override
	public void onStop() {
	      super.onStop();
	}
	
	private void updateUploadButton(){

		SearchFields search = SearchFields.Where("uploaded", 0);
		Collection<JSONEntity> entities =  Observer.database.fetchByFields(search);
		pendingObservations = entities.size();
		
		progressBar.setVisibility(View.GONE);
		if(pendingObservations == 0 ){
			uploadButton.setVisibility(View.GONE);
		} else {
			uploadButton.setVisibility(View.VISIBLE);
		    uploadButton.setText("Upload " + String.valueOf(pendingObservations) + " Observations" );
		    uploadButton.setEnabled(true);
		}
	}

	
	private void doUpload() {
		currentPosition= 0;
		
		SearchFields search = SearchFields.Where("uploaded", 0);
		Collection<JSONEntity> entities =  Observer.database.fetchByFields(search);
		uploadIterator = entities.iterator();
        pendingObservations = entities.size();
        
        uploadButton = (Button) getView().findViewById(R.id.upload_button);
        uploadButton.setText("Uploading " + String.valueOf(pendingObservations) + " Observations" );
        uploadButton.setEnabled(false);
        
        progressBar.setVisibility(View.VISIBLE);
        
        postEntitiesToServer(pendingObservations);
	}
	

	private void postEntitiesToServer(int totalNumberOfEntities){
		
		JSONEntity entity = null;

		
		// TODO late on we may want to do this in batches
		int indexInListView = totalNumberOfEntities - 1;
		while( uploadIterator.hasNext() ){
			entity = uploadIterator.next();

			Observation o = null;
			try {
				o = Observation.loadObservationFromEntity(entity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
				continue;
			} 
			
			if(o.record_sent == 1){
				if(o.thumbnail_sent == 1){
					uploadThumbnail(o, indexInListView);
				} else {
					uploadImage(o, indexInListView);
				}
				continue;
			}

			SpringAndroidSpiceRequest<RhusApiResponse> request = null;
			request = RhusApi.uploadObservationRequest(o);
			
			final WorkspaceActivity activity = (WorkspaceActivity) getActivity();	
			
			//final Toast toast = Toast.makeText( activity, "Uploaded record" , Toast.LENGTH_LONG );

			class RhusRequestListener implements RequestListener< RhusApiResponse > {

				private JSONEntity observationEntity; // TODO Ultimately this should be class Observation,
				// but to make this convienient, we need to integrate Jackson into
				// smart json.
				
				private int index;

				public RhusRequestListener(JSONEntity observationEntity, int index){
					this.observationEntity = observationEntity;
					this.index = index;
				}

				@Override
				public void onRequestFailure( SpiceException e ) {

					//showProgress(false);
					//if( e.getMessage().equals("Network is not available")  )
					if(e instanceof NoNetworkException){
						Observer.toast("Network connection is unavailable.  Cancelling upload",  activity);
						activity.getSpiceManager().cancelAllRequests();
						updateUploadButton();
					} else {
						Observer.unhandledErrorToast("Error during request: " + e.getMessage(), activity);
						e.printStackTrace();
					}
					
				}

				@Override
				public void onRequestSuccess( RhusApiResponse response ) {

					if(! response.ok ){
						Toast.makeText( activity, "Error during request: Not OK", Toast.LENGTH_LONG ).show();
						return;

		        	} else {
						Toast.makeText( activity, "Record Data Upload Succeeded", Toast.LENGTH_LONG ).show();
		        	}
					 
					
					try {
						
						observationEntity.put("record_sent", 1);
						observationEntity.put("documentId", response.id);
						observationEntity.put("revision", response.rev);
						Observer.database.store(observationEntity);
						
						Observation o = Observation.loadObservationFromEntity(observationEntity);
						//Now Upload the Attachments
						uploadThumbnail(o, index);

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (JsonMappingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				

				}


			}
			 
			 activity.getSpiceManager().execute( request, null, DurationInMillis.NEVER, new RhusRequestListener(entity, indexInListView) );
			 indexInListView--;
		}
	}
	
	public void notifyListChanged(){
		adapter.notifyDataSetChanged();
	}
	
	private void uploadThumbnail(final Observation o, final int index) {
		// TODO Auto-generated method stub
		FileInputStream inputStream = null;
		try {
			String path = o.getAttachmentPath("thumbnail", getActivity());
			inputStream = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			Toast.makeText( getActivity(), "Thumbnail Not Found", Toast.LENGTH_LONG ).show();
			e.printStackTrace();
			return;
		}
		
		SpringAndroidSpiceRequest<RhusApiResponse> request = null;
		request = RhusApi.uploadAttachmentRequest(o.documentId, o.revision, "thumb.jpg", inputStream);

		class RhusRequestListener implements RequestListener< RhusApiResponse > {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				Toast.makeText( getActivity(), "Failed to upload Thumbnail", Toast.LENGTH_LONG ).show();

			}

			@Override
			public void onRequestSuccess(RhusApiResponse arg0) {
				Toast.makeText( getActivity(), "Uploaded Thumbnail", Toast.LENGTH_LONG ).show();
				o.revision = arg0.rev;
				o.thumbnail_sent = 1;
				try {
					o.storeObservation();
					uploadImage(o, index);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		( (WorkspaceActivity) getActivity() ).getSpiceManager().execute( request, null, DurationInMillis.NEVER, new RhusRequestListener() );

			
	

	}
	
	private void uploadImage(final Observation o, final int index) {
		// TODO Auto-generated method stub
		FileInputStream inputStream = null;
		try {
			String path = o.getAttachmentPath("photo1", getActivity());
			inputStream = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			Toast.makeText( getActivity(), "Thumbnail Not Found", Toast.LENGTH_LONG ).show();
			e.printStackTrace();
			return;
		}
		
		SpringAndroidSpiceRequest<RhusApiResponse> request = null;
		request = RhusApi.uploadAttachmentRequest(o.documentId, o.revision, "medium.jpg", inputStream);

		class RhusRequestListener implements RequestListener< RhusApiResponse > {

			@Override
			public void onRequestFailure(SpiceException arg0) {
				Toast.makeText( getActivity(), "Failed to upload Photo", Toast.LENGTH_LONG ).show();

			}

			@Override
			public void onRequestSuccess(RhusApiResponse arg0) {
				Toast.makeText( getActivity(), "Uploaded Photo", Toast.LENGTH_LONG ).show();
				o.image_sent = 1;
				o.uploaded = 1;
				o.revision = arg0.rev;

				try {
					o.storeObservation();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				//toast.show();
			    ((WorkspaceActivity) getActivity()).updatePendingTotal();

				currentPosition++;
				int progress = (currentPosition * 100 / pendingObservations );
				progressBar.setProgress( progress );
				
				if(currentPosition == pendingObservations){
					
					updateUploadButton();
					
					SearchFields search = SearchFields.Where("uploaded", 0);
					Collection<JSONEntity> entities =  Observer.database.fetchByFields(search);
					pendingObservations = entities.size();
					if(pendingObservations == 0){
						Observer.toast("All pending observations have been uploaded!", getActivity());
						
					}
				}
				
				adapter.setRowUploaded(index);
				adapter.notifyDataSetChanged();

			}

		}
		( (WorkspaceActivity) getActivity() ).getSpiceManager().execute( request, null, DurationInMillis.NEVER, new RhusRequestListener() );

			
	

	}


}
