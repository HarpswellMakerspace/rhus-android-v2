package org.calflora.observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;

import android.content.Context;
import android.content.Intent;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlantSelectorActivity extends BaseActivity {

	Cursor c;
	Map<String, Drawable> plantImages = new HashMap<String, Drawable>();
	Boolean scientificName = true;
	
	
	class SearchFieldAndCursorAdapter extends CursorAdapter {

	    private final LayoutInflater mInflater;
		
		public SearchFieldAndCursorAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater=LayoutInflater.from(context);
		}


		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			if(getItemViewType(cursor.getPosition()) == 0){
				return;
			}
			
			// TODO Auto-generated method stub
			 TextView plantName =(TextView)view.findViewById(R.id.col1);
			 String nameValue;
			 if(scientificName){
				 nameValue = cursor.getString(1);
			 } else {
				 nameValue = cursor.getString(2);
			 }
			 plantName.setText(nameValue);

			 ImageView thumbnail = (ImageView)view.findViewById(R.id.list_item_image_view);
			 if(plantImages.get(cursor.getString(1)) != null){
				 thumbnail.setImageDrawable(plantImages.get(cursor.getString(1)) );
			 } else {
				 Drawable plantThumbnail;
				 try {
					 plantThumbnail = Observer.instance.getThumbnailForPlant(cursor.getString(3));
					 thumbnail.setImageDrawable(plantThumbnail);
				 } catch (IOException e) {
					 thumbnail.setImageDrawable(null);
				 }
			 }

		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view;
			if(getItemViewType(cursor.getPosition()) == 0){
				view=mInflater.inflate(R.layout.list_item_search,parent,false); 
				
				EditText searchField = (EditText) view.findViewById(R.id.search_field);
				searchField.setText(searchText);
				searchField.addTextChangedListener(filterTextWatcher);
			} else {
				view=mInflater.inflate(R.layout.list_item_single_image,parent,false); 
			}	
	
			return view;
		}
		
		
		@Override
		public int getViewTypeCount() {                 

		    return 2;
		}

		@Override
		public int getItemViewType(int position) {

		    if(position == 0){
		    	return 0;
		    } else {
		    	return 1;
		    }
		}


	}
	
	SearchFieldAndCursorAdapter adapter;
	MergeCursor projectPlantsCursor;
	MergeCursor allPlantsCursor;
	
	
	private MatrixCursor getSearchFieldCursor(){
		String[] columnNames = {"_id", "type"};
		MatrixCursor matrix = new MatrixCursor(columnNames);
		Object[] value = {999, "search"};
		matrix.addRow(value);
		return matrix;
	}
	
	private MergeCursor getProjectPlantsCursor(CharSequence constraint){
		// TODO put this into a static method
		Cursor projectPlantsCursor = Observer.plantsListDatabase.query("plist", 
				new String[] { "rowid _id", "taxon", "common", "photoid" }, 
				"taxon like ?", new String[] { constraint.toString()+"%" }, null, null, null); 

		while(projectPlantsCursor.moveToNext()){
			Drawable plantThumbnail;
			try {
				plantThumbnail = Observer.instance.getThumbnailForPlant(projectPlantsCursor.getString(3));
				plantImages.put(projectPlantsCursor.getString(1), plantThumbnail);
			} catch (IOException e) {
			}
		}

		Cursor [] cursors = {getSearchFieldCursor(),projectPlantsCursor};
		MergeCursor mergedCursor = new MergeCursor( cursors );

		return mergedCursor;
	
	}
	
	private MergeCursor getAllPlantsCursor(){
		Cursor cursor = Observer.allPlantsListDatabase.query("plist", 
				new String[] { "rowid _id", "taxon", "common", "photoid" }, 
				null, null, null, null, null); 

		cursor.moveToFirst();
		Cursor [] cursors = {getSearchFieldCursor(),cursor};
		MergeCursor mergedCursor = new MergeCursor( cursors );

		return mergedCursor;
	
	}
	
	String searchText;
	

	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	    	searchText = String.valueOf(s);
	        adapter.getFilter().filter(s);
	    }

	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);

		final ListView lv = (ListView)findViewById(R.id.plantSelectionList);
		
		
		projectPlantsCursor = getProjectPlantsCursor("");
		allPlantsCursor = getAllPlantsCursor();
		
		adapter = new SearchFieldAndCursorAdapter(this, projectPlantsCursor);
        lv.setAdapter(adapter);
		
        
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				// Search for states whose names begin with the specified letters.
				//Cursor cursor = database.query("streets", new String[] { "rowid _id", "street"}, selection, selectionArgs, groupBy, having, orderBy, limit)
				//database.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit)
				Cursor cursor =  getProjectPlantsCursor(constraint);
						
				return cursor;
			}
		});
		
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent("org.calflora.observer.action.NEWOBSERVATION");
				adapter.getCursor().moveToPosition(arg2);
				intent.putExtra(Observer.NEW_PLANT_TAXON, adapter.getCursor().getString(1) );
				setResult(RESULT_OK, intent);
				finish();

			}
		});
		
		
		Button unknownButton = (Button) findViewById(R.id.unknown_button);
		unknownButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent("org.calflora.observer.action.NEWOBSERVATION");
				intent.putExtra(Observer.NEW_PLANT_TAXON, "unknown" );
				setResult(RESULT_OK, intent);
				finish();				
			}
			
		});
		
		RadioGroup rg = (RadioGroup) findViewById(R.id.plant_name_selector);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.scientific_plants_radio){
					scientificName = true;
				} else {
					scientificName = false;
				}
				adapter.notifyDataSetChanged();
				
			}
		});
		
		RadioGroup plrg = (RadioGroup) findViewById(R.id.plant_list_selector);
		plrg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if(checkedId == R.id.project_plants_radio){
					adapter = new SearchFieldAndCursorAdapter(getBaseContext(), projectPlantsCursor);
					lv.setAdapter(adapter);				
				} else {
					adapter = new SearchFieldAndCursorAdapter(getBaseContext(), allPlantsCursor);
					lv.setAdapter(adapter);	
				}
				lv.invalidate();
				//adapter.notifyDataSetChanged();
				
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
