package org.calflora.observer;

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
import net.winterroot.rhus.util.DWHostUnreachableException;
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

			SpringAndroidSpiceRequest<APIResponseUpload> request = null;
			request = Observer.observerAPI.getUploadRequest(o, getActivity());
			
			final WorkspaceActivity activity = (WorkspaceActivity) getActivity();	
			
			//final Toast toast = Toast.makeText( activity, "Uploaded record" , Toast.LENGTH_LONG );

			class UploadRequestListener implements RequestListener< APIResponseUpload > {

				private JSONEntity observationEntity; // TODO Ultimately this should be class Observation,
				// but to make this convienient, we need to integrate Jackson into
				// smart json.
				
				private int index;

				public UploadRequestListener(JSONEntity observationEntity, int index){
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
				public void onRequestSuccess( APIResponseUpload response ) {

					if(response.status.equals("ERROR") ){
						Toast.makeText( activity, "Error during request: " + response.message, Toast.LENGTH_LONG ).show();
						return;

		        	}
					
					try {
						observationEntity.put("uploaded", 1);
						Observer.database.store(observationEntity);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//toast.show();
				    activity.updatePendingTotal();

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
			 
			//int rowIndex = 
			 activity.getSpiceManager().execute( request, null, DurationInMillis.NEVER, new UploadRequestListener(entity, indexInListView) );
			 indexInListView--;
		}
	}
	
	public void notifyListChanged(){
		adapter.notifyDataSetChanged();
	}

}
