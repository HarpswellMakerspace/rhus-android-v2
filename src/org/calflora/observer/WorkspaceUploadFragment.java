package org.calflora.observer;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import org.calflora.observer.api.APIResponseBase;
import org.calflora.observer.api.APIResponseOrganizations;
import org.calflora.observer.api.APIResponseProject;
import org.calflora.observer.api.APIResponseUpload;
import org.calflora.observer.model.Attachment;
import org.calflora.observer.model.Observation;
import org.calflora.observer.model.Project;
import org.json.JSONException;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

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
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.app.Fragment;

public class WorkspaceUploadFragment extends WorkspaceListFragment {

	ProgressBar progressBar;
	protected static final String JSON_CACHE_KEY = "CACHE_KEY";
	protected SpiceManager spiceManager = new SpiceManager( JacksonSpringAndroidSpiceService.class );
	
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
		spiceManager.start( getActivity() );
		
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
	
	@Override
	public void onStop() {
	      spiceManager.shouldStop();
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
        
        postEntityToServer();
	}
	
	private void doneUploading(){
		 WorkspaceActivity activity = (WorkspaceActivity) getActivity();
	     activity.updatePendingTotal();
	}
	
	private void postEntityToServer(){
		
		if(!iterator.hasNext()){
			doneUploading();
			return;
		}
		
		JSONEntity entity = iterator.next();

		
		Observation o = null;
		
		while (o == null ) {
			try {
				o = Observation.loadObservationFromEntity(entity);
				
			} catch (Exception e) {
				e.printStackTrace();
				currentPosition++;
				entity = null;
				if(iterator.hasNext()){
					entity = iterator.next();
				} else {
					doneUploading();
					return;
				}
			}
		}
			
		SpringAndroidSpiceRequest<APIResponseUpload> request = Observer.observerAPI.getUploadRequest(o);
		
		class UploadRequestListener implements RequestListener< APIResponseUpload > {
	        @Override
	        public void onRequestFailure( SpiceException e ) {
	        	
				//showProgress(false);
	            Toast.makeText( getActivity(), "Error during request: " + e.getMessage(), Toast.LENGTH_LONG ).show();
				e.printStackTrace();
	        }

	        @Override
	        public void onRequestSuccess( APIResponseUpload response ) {

	        	currentPosition++;
	            int progress = (currentPosition * 100 / totalObservations );
	            progressBar.setProgress( progress );
				//Do it again!
	        	//postEntityToServer();
						      
	        }
	    }
		
		spiceManager.execute( request, JSON_CACHE_KEY, DurationInMillis.NEVER, new UploadRequestListener() );

				
	}

}
