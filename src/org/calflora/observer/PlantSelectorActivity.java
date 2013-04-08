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
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlantSelectorActivity extends Activity {

	Cursor c;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);

		ListView lv = (ListView)findViewById(R.id.plantSelectionList);

		/*
		 * Demo Code
		 
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
		*/

		String[] from = new String[] {"taxon"};
		int[] to = new int[] { R.id.col1 };

		/*
		SimpleAdapter adapter = new SimpleAdapter( this, listData, R.layout.list_item_single, from, to);
		
		*/
		
		c = Observer.plantsListDatabase.query("plist", 
				  new String[] { "rowid _id", "taxon", "common" }, 
				  null, null, null, null, null); 
		
		lv.setAdapter(new SimpleCursorAdapter(getApplicationContext(), R.layout.list_item_single, c, from, to, 0 ));

		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent("org.calflora.observer.action.NEWOBSERVATION");
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
	
	protected class CustomCursorAdapter extends SimpleCursorAdapter  {
        private int layout; 
        private LayoutInflater inflater;
        private Context context;

        public CustomCursorAdapter (Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);
            this.layout = layout;
            this.context = context;
            inflater = LayoutInflater.from(context);

        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            //Log.i("NewView", newViewCount.toString());

            View v = inflater.inflate(R.layout.list_item_single, parent, false);

            return v;
        }

        /*
        @Override
        public void bindView(View v, Context context, Cursor c) {
                    //1 is the column where you're getting your data from
            String name = c.getString(1);
            /**
             * Next set the name of the entry.
             */
        /*
            TextView name_text = (TextView) v.findViewById(R.id.textView);
            if (name_text != null) {
                name_text.setText(name);
            }   
            */
        
        }
        

}
