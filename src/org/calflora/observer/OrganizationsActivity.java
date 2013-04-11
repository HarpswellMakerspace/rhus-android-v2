package org.calflora.observer;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.calflora.observer.api.APIResponseOrganization;
import org.calflora.observer.api.APIResponseOrganizations;
import org.calflora.observer.api.APIResponseSignIn;
import org.calflora.observer.api.IdNameItem;
import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class OrganizationsActivity extends ApiActivity {
	
	//static final String mockJson ="[{\"id\":\"42\", \"name\":\"Yosemite NP Invasive Plant Management\"},{\"id\":\"20\", \"name\":\"Presidio Trust Natural Resources Team\"},{\"id\":\"0\", \"name\":\"Independent\"}]";
	//static final String mockOrganizationJSON = "{\"id\":\"1\", \"name\":\"Yosemite NP Invasive Plant Management\", \"splashGraphic\":\"http://www.nps.gov/yose/naturescience/images/torch-web_1.jpg\",\"logoGraphic\":\"http://www.yosemiteconservancy.org/sites/all/themes/yosemite/images/logo.gif\",\"orgURL\":\"http://www.yosemiteconservancy.org/\",\"projects\":[{\"id\":\"pr1\", \"name\":\"Weed Inventory 2012\"},{\"id\":\"pr2\", \"name\":\"Weed Inventory 2013\"}]}";
	
	ArrayList<IdNameItem> organizations;
	List<Map<String,Object>> organizationsListData; 

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organizations);
		
        
        
	}
	
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.organizations, menu);
		return true;
	}


	public boolean organizationsRequestSent = false;

	@Override
	protected void onStart() {
		super.onStart();
		
		// Load Organizations into List
		// if online
		// load from remote
		// else 
		// load from JSON datastore

		if ( ! organizationsRequestSent ){
			
			organizationsRequestSent = true;
			
			mStatusMessageView.setText("Getting Organizations");

			class OrganizationsRequestListener implements RequestListener< APIResponseOrganizations > {
				@Override
				public void onRequestFailure( SpiceException e ) {

					showProgress(false);
					Toast.makeText( OrganizationsActivity.this, "Error during request: " + e.getMessage(), Toast.LENGTH_LONG ).show();
					e.printStackTrace();
				}

				@Override
				public void onRequestSuccess( APIResponseOrganizations response ) {

					showProgress(false);
					organizations = response.data;
					// TODO And we may want to cache this here
					populateList();


				}
			}

			showProgress(true);
			spiceManager.execute( Observer.observerAPI.getOrganizationsRequest(), JSON_CACHE_KEY, DurationInMillis.NEVER, new OrganizationsRequestListener() );

		}
	}
	
	protected void populateList(){
		
		ListView lv = (ListView)findViewById(R.id.organizationsListView);

		//TODO: Any of the above exceptions should be handled gracefully
		//Though the more salient error would be upon loading JSON remotely into the 
		
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		
		int i=1;
		for(IdNameItem o: organizations){
			map = new HashMap<String, String>();
			map.put("rowid", String.valueOf(i));
			map.put("col_1", (String) o.name);
			listData.add(map);
			i++;

		}
		
		String[] from = new String[] {"col_1"};
	    int[] to = new int[] { R.id.col1 };

		SimpleAdapter adapter = new SimpleAdapter( this, listData, R.layout.list_item_single, from, to);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long rowid) {
				
				IdNameItem organizationIdData = organizations.get(arg2);
				
				requestOrganizationDetail(organizationIdData.id);
				
			}


        });
	}

	public void requestOrganizationDetail(String id){
	
		class OrganizationRequestListener implements RequestListener< APIResponseOrganization > {
	        @Override
	        public void onRequestFailure( SpiceException e ) {
	        	
	        	// TODO remove bypass
				showProgress(false);
	        	
	            Toast.makeText( OrganizationsActivity.this, "Error during request: " + e.getMessage(), Toast.LENGTH_LONG ).show();
				e.printStackTrace();
	        }

	        @Override
	        public void onRequestSuccess( APIResponseOrganization response ) {

				// TODO This needs to be cached
				Observer.instance.setOrganization(response.data);	
				
				Intent intent = new Intent("org.calflora.observer.action.PROJECTS");
				startActivity(intent);
				showProgress(false);

	        }
	    }
		
		mStatusMessageView.setText("Getting Organization Details");
		showProgress(true);
		spiceManager.execute( Observer.observerAPI.getOrganizationRequest(id), JSON_CACHE_KEY, DurationInMillis.NEVER, new OrganizationRequestListener() );

		
	}
		
}
	