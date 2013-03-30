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
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlantSelectorActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);

		ListView lv = (ListView)findViewById(R.id.plantSelectionList);

		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;

		int i=1;
		while(i<10){
	
				map = new HashMap<String, String>();
				map.put("rowid", String.valueOf(i));
				map.put("col_1", "Plant"+String.valueOf(i));
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
					long arg3) {

				Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
				startActivity(intent);


			}


		});



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.plant_selector, menu);
		return true;
	}

}
