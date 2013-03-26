package org.calflora.observer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.calflora.observer.model.Organization;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;


public class OrganizationsActivity extends Activity {
	
	static final String mockJson ="[{\"id\":\"42\", \"name\":\"Yosemite NP Invasive Plant Management\"},{\"id\":\"20\", \"name\":\"Presidio Trust Natural Resources Team\"},{\"id\":\"0\", \"name\":\"Independent\"}]";
	static final String mockOrganizationJSON = "{\"id\":\"1\", \"name\":\"Yosemite NP Invasive Plant Management\", \"splashGraphic\":\"http://www.nps.gov/yose/naturescience/images/torch-web_1.jpg\",\"logoGraphic\":\"http://www.yosemiteconservancy.org/sites/all/themes/yosemite/images/logo.gif\",\"orgURL\":\"http://www.yosemiteconservancy.org/\",\"projects\":[{\"id\":\"pr1\", \"name\":\"Weed Inventory 2012\"},{\"id\":\"pr2\", \"name\":\"Weed Inventory 2013\"}]}";
	
	List<Map<String,Object>> organizationsData; 

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organizations);
		
		ListView lv = (ListView)findViewById(R.id.organizationsListView);
		
		
		//if online
		//load from remote
		//else 
		//load from JSON datastore
		InputStream is = new ByteArrayInputStream(mockJson.getBytes());
	
		organizationsData = null;
		try {
			organizationsData =  Observer.mapper.readValue(is, List.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		//TODO: Any of the above exceptions should be handled gracefully
		//Though the more salient error would be upon loading JSON remotely into the 
		
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		
		int i=1;
		for(Map<String,Object> o: organizationsData){
			if(o.containsKey("name")){
				map = new HashMap<String, String>();
				map.put("rowid", String.valueOf(i));
				map.put("col_1", (String) o.get("name"));
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
					long rowid) {
				
		        //TODO: Exceptions should result in no advance
				Organization organization = null;
		    	InputStream is = new ByteArrayInputStream(mockOrganizationJSON.getBytes());
		    	
		    	try {
					organization = Observer.mapper.readValue(is, Organization.class);
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
				
				Observer.organization = organization;
				//TODO: download and unzip the organization resources
				
				Intent intent = new Intent("org.calflora.observer.action.PROJECTS");
				startActivity(intent);
				
				
			}


        });
        
        
    
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.organizations, menu);
		return true;
	}

}
