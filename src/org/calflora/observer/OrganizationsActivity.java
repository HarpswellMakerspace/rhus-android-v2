package org.calflora.observer;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;


public class OrganizationsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_organizations);
		
		ListView lv = (ListView)findViewById(R.id.organizationsListView);
		
		/*
	     List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
	        for(int i = 0; i < 10; i++){
	            HashMap<String, String> map = new HashMap<String, String>();
	            map.put("rowid", "" + i);
	            map.put("col_1", "col_1_item_" + i);
	            map.put("col_2", "col_2_item_" + i);
	            map.put("col_3", "col_3_item_" + i);
	            fillMaps.add(map);
	        }
	      */
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;
		
		map = new HashMap<String, String>();

		map.put("rowid", "1");
		map.put("col_1", "Calflora");
		listData.add(map);
		
		map = new HashMap<String, String>();
		map.put("rowid", "2");
		map.put("col_1", "Yosemite");
		listData.add(map);

		map = new HashMap<String, String>();
		map.put("rowid", "3");
		map.put("col_1", "ESRI");
		listData.add(map);

		
		 String[] from = new String[] {"col_1"};
	     int[] to = new int[] { R.id.col1 };
	 


		SimpleAdapter adapter = new SimpleAdapter( this, listData, R.layout.list_item_single, from, to);
        lv.setAdapter(adapter);
        
        lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
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
