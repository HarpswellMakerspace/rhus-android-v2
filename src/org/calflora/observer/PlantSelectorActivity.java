package org.calflora.observer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.winterroot.android.wildflowers.R;
import net.winterroot.rhus.util.DataBaseHelper;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import com.actionbarsherlock.view.*;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

public class PlantSelectorActivity extends BaseActivity {

	protected static final String PROJECT_PLANTS_PREFS_KEY = "PROJECT_PLANTS_PREFS_KEY";
	protected static final String SCI_NAME_PREFS_KEY = "SCI_NAME_PREFS_KEY";
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
			
			// TODO Auto-generated method stub
			 TextView plantName =(TextView)view.findViewById(R.id.col1);
			 String nameValue;
			 if(scientificName){
				 nameValue = cursor.getString(1)+" ( "+cursor.getString(2)+" )";
			 } else {
				 nameValue = cursor.getString(2)+" ( "+cursor.getString(1)+" )";
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
			view = mInflater.inflate(R.layout.list_item_single_image,parent,false); 

	
			return view;
		}

	}
	
	
	private Cursor getPlantsCursor(SQLiteDatabase database, CharSequence constraint, Boolean scientificName){
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
		return cursor;
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

	EditText searchField;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);
		
		cursorConstraint = "";

		final ListView lv = (ListView)findViewById(R.id.plantSelectionList);
		
		searchField = (EditText) findViewById(R.id.selector_search_field);
		searchField.setText(searchText);
		searchField.addTextChangedListener(filterTextWatcher);
		
		SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		RadioButton commonNamesRadio = (RadioButton) findViewById(R.id.common_plants_radio);
		RadioButton scientificNamesRadio = (RadioButton) findViewById(R.id.scientific_plants_radio);
		RadioButton projectPlantsRadio = (RadioButton) findViewById(R.id.project_plants_radio);
		RadioButton allPlantsRadio = (RadioButton) findViewById(R.id.all_plants_radio);
		
		if(sharedpreferences.getBoolean(PlantSelectorActivity.SCI_NAME_PREFS_KEY, true)){
			commonNamesRadio.setChecked(false);
			scientificNamesRadio.setChecked(true);
		} else {
			commonNamesRadio.setChecked(true);
			scientificNamesRadio.setChecked(false);
		}
		

		if(sharedpreferences.getBoolean(PlantSelectorActivity.PROJECT_PLANTS_PREFS_KEY, true)){
			selectedDatabase = Observer.plantsListDatabase;
			projectPlantsRadio.setChecked(true);
			allPlantsRadio.setChecked(false);
		} else {
			selectedDatabase = Observer.allPlantsListDatabase;
			projectPlantsRadio.setChecked(false);
			allPlantsRadio.setChecked(true);
		}
		Cursor plantsCursor= getPlantsCursor( selectedDatabase, "", sharedpreferences.getBoolean(PlantSelectorActivity.SCI_NAME_PREFS_KEY, true));

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

				Intent intent = new Intent();
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
				Intent intent = new Intent();
				intent.putExtra(Observer.NEW_PLANT_TAXON, "unknown" );
				setResult(RESULT_OK, intent);
				finish();				
			}
			
		});
		
		Button customTaxonButton = (Button) findViewById(R.id.custom_taxon_button);
		customTaxonButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra(Observer.NEW_PLANT_TAXON, searchField.getText().toString() );
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
				
				SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				sharedpreferences.edit().putBoolean(PlantSelectorActivity.SCI_NAME_PREFS_KEY, scientificName).commit();
				
		        adapter.getFilter().filter(cursorConstraint);
				
			}
		});
		
		RadioGroup plrg = (RadioGroup) findViewById(R.id.plant_list_selector);
		plrg.setOnCheckedChangeListener(new OnCheckedChangeListener(){

			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				Boolean projectPlantsPref = false;
				if(checkedId == R.id.project_plants_radio){
					selectedDatabase = Observer.plantsListDatabase;
					adapter.changeCursor(getPlantsCursor(selectedDatabase, cursorConstraint, scientificName));	
					projectPlantsPref = true;
				} else {
					selectedDatabase = Observer.allPlantsListDatabase;
					adapter.changeCursor(getPlantsCursor(selectedDatabase, cursorConstraint, scientificName));	
				}
				
				SharedPreferences sharedpreferences = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				sharedpreferences.edit().putBoolean(PlantSelectorActivity.PROJECT_PLANTS_PREFS_KEY, projectPlantsPref).commit();
				
				lv.invalidate();
				adapter.notifyDataSetChanged();
				
			}
		});
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.plant_selector, (Menu) menu);
		return true;
	}    

}
