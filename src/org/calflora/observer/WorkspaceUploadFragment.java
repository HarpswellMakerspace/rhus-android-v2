package org.calflora.observer;

import java.util.Collection;
import java.util.Iterator;

import org.json.JSONException;

import net.smart_json_databsase.JSONEntity;
import net.smart_json_databsase.SearchFields;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.app.Fragment;

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
	
	private void doUpload() {
		int currentPosition= 0;
		Collection<JSONEntity> entities = getEntities();
        int total = entities.size();
        Iterator<JSONEntity> iterator = entities.iterator();
        while (currentPosition<total) {
        	JSONEntity entity = iterator.next();
        	try {
				entity.put("uploaded", 1);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				continue;
			}
        	Observer.database.store(entity);
        	// TODO Update the item in the list with a checkmark
        	
            try {
                Thread.sleep(200);
                currentPosition++;
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }         
            int progress = (currentPosition * 100 / total );
            progressBar.setProgress( progress );
        }	
		
        Button uploadButton = (Button) getView().findViewById(R.id.upload_button);
        uploadButton.setText("Upload Complete");
        uploadButton.setEnabled(false);
        
        WorkspaceActivity activity = (WorkspaceActivity) getActivity();
        activity.updatePendingTotal();
	}

}
