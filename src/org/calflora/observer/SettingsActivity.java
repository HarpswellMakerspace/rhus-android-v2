package org.calflora.observer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class SettingsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		TextView tv;
		tv = (TextView)findViewById(R.id.current_user);
	    tv.setText( Observer.instance.getCurrentUsername());
	    
		tv = (TextView)findViewById(R.id.current_organization);
		String orgName = Observer.instance.getOrganization().name;
		if(orgName != null){
			tv.setText(orgName);
		} else {
			tv.setText("Organization Name Missing");
		}
		
		tv = (TextView)findViewById(R.id.current_project);
		String projectName = Observer.instance.getProject().name;
		if(projectName != null){
			tv.setText(projectName);
		} else {
			tv.setText("Project Name Missing");
		}
		
		ListView lv = (ListView)findViewById(R.id.settings_list);

		List<HashMap<String, String>> listData = new ArrayList<HashMap<String, String>>();
		
		listData.add(rowSetup("1", "Refresh Project Data"));
		listData.add(rowSetup("2", "Change Organization"));
		listData.add(rowSetup("3", "Change Project"));
		listData.add(rowSetup("4", "About"));
		listData.add(rowSetup("5", "Send Comments"));
		listData.add(rowSetup("6", "Log Out"));
		listData.add(rowSetup("7", "Prerelease version 2.A.01"));

		String[] from = new String[] {"col_1"};
		int[] to = new int[] { R.id.col1 };

		SimpleAdapter adapter = new SimpleAdapter( this, listData, R.layout.list_item_single, from, to);
		lv.setAdapter(adapter);

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {

				Intent intent;
				switch(arg2){
				case 0:
					break;
				case 1:
					intent = new Intent("org.calflora.observer.action.ORGANIZATIONS");
					startActivity(intent);
					break;
				case 2:
					intent = new Intent("org.calflora.observer.action.PROJECTS");
					startActivity(intent);
					break;
				case 3:
					break;
				case 4:
					break;
				case 5:
					Observer.instance.forgetUser();
					intent = new Intent("org.calflora.observer.action.LOGIN");
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
					finish();
					break;

				}


			}


		});

	}

	private HashMap<String, String> rowSetup(String id, String title){
		HashMap<String, String>map = new HashMap<String, String>();
		map.put("rowid", id);
		map.put("col_1", title);
		return map;

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

}
