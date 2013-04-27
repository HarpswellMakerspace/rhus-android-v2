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
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
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
	SearchFieldAndCursorAdapter adapter;

	SQLiteDatabase selectedDatabase;
	CharSequence cursorConstraint;
	
	class SearchFieldAndCursorAdapter extends CursorAdapter {

	    private final LayoutInflater mInflater;
		
		public SearchFieldAndCursorAdapter(Context context, Cursor c) {
			super(context, c);
			mInflater=LayoutInflater.from(context);
		
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			
			/*
			 * This was part of the search field within listview attempts
			if(getItemViewType(cursor.getPosition()) == 0){
				return;
			}
			*/
			
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
			/*
			if(getItemViewType(cursor.getPosition()) == 0){
				view = searchView;
			} else {
				view = mInflater.inflate(R.layout.list_item_single_image,parent,false); 
			}	*/
			
			view = mInflater.inflate(R.layout.list_item_single_image,parent,false); 

	
			return view;
		}
		
		
		/*
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
		*/

	}
	

	
	
	private MatrixCursor getSearchFieldCursor(){
		String[] columnNames = {"_id", "type"};
		MatrixCursor matrix = new MatrixCursor(columnNames);
		Object[] value = {999, "search"};
		matrix.addRow(value);
		return matrix;
	}
	
	
	private MergeCursor getPlantsCursor(SQLiteDatabase database, CharSequence constraint, Boolean scientificName){
		String selection;
		String column;
		if(scientificName){
			column = "taxon";
		} else {
			column = "common";
		}
		selection =  column+" like ? OR "+column+" like ?";

		String [] selectionArgs = new String [] { constraint.toString()+"%", "% "+constraint.toString()+"%" };

		Cursor cursor = database.query("plist", 
				new String[] { "rowid _id", "taxon", "common", "photoid" }, 
				selection, selectionArgs, null, null, column + " asc"); 
		Cursor [] cursors = {/*getSearchFieldCursor(), */ cursor};
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
	    	cursorConstraint = searchText;
	    	filter();
	    }

	};
	
	private void filter(){
        adapter.getFilter().filter(cursorConstraint);
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);
		
		cursorConstraint = "";

		final ListView lv = (ListView)findViewById(R.id.plantSelectionList);
		
		EditText searchField = (EditText) findViewById(R.id.selector_search_field);
		searchField.setText(searchText);
		searchField.addTextChangedListener(filterTextWatcher);
		
		selectedDatabase = Observer.plantsListDatabase;
		MergeCursor plantsCursor= getPlantsCursor( selectedDatabase, "", true);

		adapter = new SearchFieldAndCursorAdapter(this, plantsCursor);
        lv.setAdapter(adapter);
		
        
        adapter.setFilterQueryProvider(new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				
				Cursor cursor = getPlantsCursor( selectedDatabase, constraint, scientificName);
						
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
					selectedDatabase = Observer.plantsListDatabase;
					adapter.changeCursor(getPlantsCursor(selectedDatabase, cursorConstraint, scientificName));	
				} else {
					selectedDatabase = Observer.allPlantsListDatabase;
					adapter.changeCursor(getPlantsCursor(selectedDatabase, cursorConstraint, scientificName));	
				}
				lv.invalidate();
				adapter.notifyDataSetChanged();
				
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
