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
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

import net.smart_json_databsase.JSONEntity;
import net.smart_json_databsase.SearchFields;
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

	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_workspace_upload, container, false);
	}
	
	@Override
	public Collection<JSONEntity> getEntities(){
		SearchFields search = SearchFields.Where("uploaded", 0);
		Collection<JSONEntity> entities = Observer.database.fetchByFields(search);
		return entities;
	}
	
	
	
	public void onStart() {
		super.onStart();

		progressBar = (ProgressBar) getView().findViewById(R.id.progressBar);
		Button uploadButton = (Button) getView().findViewById(R.id.upload_button);

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
	}
	
	@Override
	public void onStop() {
	      super.onStop();
	}

	
	private Iterator<JSONEntity> iterator;
	private int currentPosition;
	private int totalObservations;
	private Button uploadButton;
	
	private void doUpload() {
		currentPosition= 0;
		Collection<JSONEntity> entities = getEntities();
        totalObservations = entities.size();
        iterator = entities.iterator();
        
        uploadButton = (Button) getView().findViewById(R.id.upload_button);
        uploadButton.setText("Uploading " + String.valueOf(totalObservations) + " Observations" );
        uploadButton.setEnabled(false);
        
        postEntitiesToServer();
	}
	

	private void postEntitiesToServer(){
		
		JSONEntity entity = null;
		
		// TODO late on we may want to do this in batches
		while( iterator.hasNext() ){
			entity = iterator.next();

			Observation o = null;
			try {
				o = Observation.loadObservationFromEntity(entity);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				
				e.printStackTrace();
				continue;
			} 

			SpringAndroidSpiceRequest<APIResponseUpload> request = Observer.observerAPI.getUploadRequest(o, getActivity());
			
			final WorkspaceActivity activity = (WorkspaceActivity) getActivity();	
			
			final Toast toast = Toast.makeText( activity, "Uploaded record" , Toast.LENGTH_LONG );

			class UploadRequestListener implements RequestListener< APIResponseUpload > {

				private JSONEntity observationEntity; // TODO Ultimately this should be class Observation,
				// but to make this convienient, we need to integrate Jackson into
				// smart json.

				public UploadRequestListener(JSONEntity observationEntity){
					this.observationEntity = observationEntity;
				}

				@Override
				public void onRequestFailure( SpiceException e ) {

					//showProgress(false);
					Toast.makeText( activity, "Error during request: " + e.getMessage(), Toast.LENGTH_LONG ).show();
					e.printStackTrace();
				}

				@Override
				public void onRequestSuccess( APIResponseUpload response ) {

					try {
						observationEntity.put("uploaded", 1);
						Observer.database.store(observationEntity);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					toast.show();
				    activity.updatePendingTotal();

					currentPosition++;
					int progress = (currentPosition * 100 / totalObservations );
					progressBar.setProgress( progress );
					
					if(currentPosition == totalObservations){
						uploadButton = (Button) getView().findViewById(R.id.upload_button);
						uploadButton.setText("Done Uploading!" );
						uploadButton.setEnabled(false);
					}

				}
			}
			 
			 activity.getSpiceManager().execute( request, null, DurationInMillis.NEVER, new UploadRequestListener(entity) );

		}
	}

}
