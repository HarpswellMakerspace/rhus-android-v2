package org.calflora.observer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.winterroot.rhus.util.DWHostUnreachableException;

import org.calflora.observer.api.APIResponseOrganizations;
import org.calflora.observer.api.APIResponseProject;
import org.calflora.observer.model.Project;
import org.calflora.observer.model.ProjectStub;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.actionbarsherlock.view.*;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ProjectsActivity extends ApiActivity {
	
	List<Map<String,Object>> projectsData; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_projects);
		
		TextView tv = (TextView)findViewById(R.id.projectsOrganizationLabel);
		tv.setText(Observer.instance.getOrganization().name);
		
		ListView lv = (ListView)findViewById(R.id.projectsListView);
		
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		
		int i=1;
		for(ProjectStub p: Observer.instance.getOrganization().projects){
			if(p.name != null){
				map = new HashMap<String, String>();
				map.put("rowid", String.valueOf(i));
				map.put("col_1", (String) p.name);
				listData.add(map);
				i++;
			}
		}

		String[] from = new String[] {"col_1"};
		int[] to = new int[] { R.id.col1 };
		
		SimpleAdapter adapter = new SimpleAdapter( this, listData, R.layout.list_item_single, from, to);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
			
				ProjectStub p = Observer.instance.getOrganization().projects.get(arg2);
				getProjectDetails(p.id);
					
			}


        });
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.projects, (Menu) menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		
	}
	
	
	protected void getProjectDetails(String projectId){
		
		class ProjectRequestListener implements RequestListener< APIResponseProject > {
	        @Override
	        public void onRequestFailure( SpiceException e ) {

	        	showProgress(false);
	        	
	        	Observer.unhandledErrorToast("Error during request: " + e.getMessage(), ProjectsActivity.this);
				e.printStackTrace();
				
			
	        }

	        @Override
	        public void onRequestSuccess( APIResponseProject response ) {
	        	
	        	if(response.status.equals("ERROR")){
	        	    Toast.makeText( ProjectsActivity.this, "Error during request: " + response.message, Toast.LENGTH_LONG ).show();
	        	    showProgress(false);
	        	} else {
	        	
	        		Observer.instance.setProject(response.data);

	        		Intent intent = new Intent("org.calflora.observer.action.DOWNLOAD_PROJECT_ASSETS");
	        		startActivity(intent);
	        		finish();
	        	}
	        }
	    }
		
		mStatusMessageView.setText("Getting Project Details");
		showProgress(true);
		
		spiceManager.execute( Observer.observerAPI.getProjectRequest(projectId), JSON_CACHE_KEY, DurationInMillis.NEVER, new ProjectRequestListener() );
		

		
	}

}
