package org.calflora.observer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.calflora.observer.model.Project;
import org.calflora.observer.model.ProjectStub;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class ProjectsActivity extends Activity {
	
	static final String mockJSON ="{\"id\":\"projectID1\", \"center_lat\":37.52483,\"center_lng\":-122.409,\"tilepackage\":\"https://www.calflora.org/tilep/YosemiteBaseCache.tpk\",\"tilepackageSize\":1367509}";
	List<Map<String,Object>> projectsData; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_projects);
		
		TextView tv = (TextView)findViewById(R.id.projectsOrganizationLabel);
		tv.setText(Observer.organization.name);
		
		ListView lv = (ListView)findViewById(R.id.projectsListView);
		
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		
		int i=1;
		for(ProjectStub p: Observer.organization.projects){
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
				
				
				  //TODO: Exceptions should result in no advance
				Project project = null;
		    	InputStream is = new ByteArrayInputStream(mockJSON.getBytes());
		    	
		    	try {
		    		project = Observer.mapper.readValue(is, Project.class);
				} catch (JsonParseException e) {
					Observer.toast("Error loading...", getApplicationContext());
					e.printStackTrace();
					return;
				} catch (JsonMappingException e) {
					Observer.toast("Error loading...", getApplicationContext());
					e.printStackTrace();
					return;
				} catch (IOException e) {
					Observer.toast("Error loading...", getApplicationContext());
					e.printStackTrace();
					return;
				}
				
				Observer.project = project;
				//TODO: download and unzip the project resources
				
				
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				SharedPreferences.Editor editor = settings.edit();
				
				try{
					editor.putString("project", Observer.mapper.writeValueAsString(project));
					editor.commit();
				} catch (JsonProcessingException e) {
					Observer.toast("Error Writing JSON", getApplicationContext());
					e.printStackTrace();
					return;
				}
										
			
				Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
				startActivity(intent);
				
				
			}


        });
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.projects, menu);
		return true;
	}

}
