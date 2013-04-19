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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.Filter;
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
	
	
	class MyCustomAdapter extends CursorAdapter {

		Context context;
	    public Cursor c;
	    private final LayoutInflater mInflater;
		
		public MyCustomAdapter(Context context, Cursor c) {
			super(context, c);
			
			this.context = context;
			this.c = c;
			mInflater=LayoutInflater.from(context);
		}

		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view;
			if(position==0){
				view =mInflater.inflate(R.layout.list_item_search,parent,false); 
			} else {
				view=mInflater.inflate(R.layout.list_item_single_image,parent,false); 
			}	
	
			if(position == 0){
				//This is the search cell
				return view;
			}
			
			c.moveToPosition(position);
			
			
			// TODO Auto-generated method stub
			 TextView plantName =(TextView)view.findViewById(R.id.col1);
			 String nameValue;
			 if(scientificName){
				 nameValue = c.getString(1);
			 } else {
				 nameValue = c.getString(2);
			 }
			 plantName.setText(nameValue);

			 ImageView thumbnail = (ImageView)view.findViewById(R.id.list_item_image_view);
			 if(plantImages.get(c.getString(1)) != null){
				 thumbnail.setImageDrawable(plantImages.get(c.getString(1)) );
			 } else {
				 Drawable plantThumbnail;
				 try {
					 plantThumbnail = Observer.instance.getThumbnailForPlant(projectPlantsCursor.getString(3));
					 thumbnail.setImageDrawable(plantThumbnail);
				 } catch (IOException e) {
					 thumbnail.setImageDrawable(null);
				 }
			 }
			
			 
			 return view;

		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			// getView is overriding this..
			
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			// getView is overriding this.
			return null;
		}



		@Override
		public Filter getFilter() {
			return super.getFilter();
		}
		
		

	}
	MyCustomAdapter adapter;
	
	MergeCursor projectPlantsCursor;
	MergeCursor allPlantsCursor;
	
	
	private MatrixCursor getSearchFieldCursor(){
		String[] columnNames = {"id"};
		MatrixCursor matrix = new MatrixCursor(columnNames);
		String[] value = {"search"};
		matrix.addRow(value);
		return matrix;
	}
	
	private MergeCursor getProjectPlantsCursor(){
		// TODO put this into a static method
		Cursor projectPlantsCursor = Observer.plantsListDatabase.query("plist", 
				new String[] { "rowid _id", "taxon", "common", "photoid" }, 
				null, null, null, null, null); 



		projectPlantsCursor.moveToFirst();

		while(projectPlantsCursor.moveToNext()){
			Drawable plantThumbnail;
			try {
				plantThumbnail = Observer.instance.getThumbnailForPlant(projectPlantsCursor.getString(3));
				plantImages.put(projectPlantsCursor.getString(1), plantThumbnail);
			} catch (IOException e) {
			}
		}

		Cursor [] cursors = {projectPlantsCursor, getSearchFieldCursor()};
		MergeCursor mergedCursor = new MergeCursor( cursors );

		return mergedCursor;
	
	}
	
	private MergeCursor getAllPlantsCursor(){
		// TODO put this into a static method
		Cursor cursor = Observer.allPlantsListDatabase.query("plist", 
				new String[] { "rowid _id", "taxon", "common", "photoid" }, 
				null, null, null, null, null); 



		cursor.moveToFirst();
	

		/*
		 * TODO: all these drawables can't be held in memory.
		while(cursor.moveToNext()){
			AssetFileDescriptor asset = null;
			String imagePath = "plant_images/" + c.getString(3).replace("'","")+".jpeg";
			try {
				asset = assets.openFd(imagePath);
				Drawable plantThumbnail = Drawable.createFromStream(asset.createInputStream(), "");
				plantImages.add(plantThumbnail);
			} catch (IOException e) {
				plantImages.add(null);
			}

		}
		*/

		Cursor [] cursors = {cursor, getSearchFieldCursor()};
		MergeCursor mergedCursor = new MergeCursor( cursors );

		return mergedCursor;
	
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);

		final ListView lv = (ListView)findViewById(R.id.plantSelectionList);
		
		
		projectPlantsCursor = getProjectPlantsCursor();
		allPlantsCursor = getAllPlantsCursor();
		
		adapter = new MyCustomAdapter(this, projectPlantsCursor);
        lv.setAdapter(adapter);
		
		
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent("org.calflora.observer.action.NEWOBSERVATION");
				adapter.c.moveToPosition(arg2);
				intent.putExtra(Observer.NEW_PLANT_TAXON, adapter.c.getString(1) );
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
					adapter = new MyCustomAdapter(getBaseContext(), projectPlantsCursor);
					lv.setAdapter(adapter);				
				} else {
					adapter = new MyCustomAdapter(getBaseContext(), allPlantsCursor);
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
