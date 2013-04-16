package org.calflora.observer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PlantSelectorActivity extends BaseActivity {

	Cursor c;
	List<Drawable> plantImages = new ArrayList<Drawable>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plant_selector);

		ListView lv = (ListView)findViewById(R.id.plantSelectionList);
		
		// TODO put this into a static method
		c = Observer.plantsListDatabase.query("plist", 
				  new String[] { "rowid _id", "taxon", "common", "photoid" }, 
				  null, null, null, null, null); 
		
		c.moveToFirst();
	    AssetManager assets = getBaseContext().getResources().getAssets();

		while(c.moveToNext()){
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
		
		
		class MyCustomAdapter extends CursorAdapter {

			Context context;
		    Cursor c;
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
				 String nameValue = c.getString(1);
				 plantName.setText(nameValue);
				 // mobileNo.setText(cursor.getString(cursor.getColumnIndex(TextMeDBAdapter.KEY_MOBILENO)));

				 ImageView thumbnail = (ImageView)view.findViewById(R.id.list_item_image_view);
				 AssetFileDescriptor asset = null;
				 String imagePath = "plant_images/" + c.getString(3).replace("'","")+".jpeg";
				 if(plantImages.get(position-1) != null){
					 thumbnail.setImageDrawable(plantImages.get(position-1) );
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

			//@Override
			/*
			public View newView(Context context, Cursor cursor, ViewGroup parent) {
				if(cursor.getPosition()==0){
					 final View view=mInflater.inflate(R.layout.list_item_search,parent,false); 
				     return view;	
				}
				
				 final View view=mInflater.inflate(R.layout.list_item_single_image,parent,false); 
			     return view;				
			}
			*/
			
		}
		String[] columnNames = {"id"};
		MatrixCursor matrix = new MatrixCursor(columnNames);
		String[] value = {"search"};
		matrix.addRow(value);
		
		
		Cursor [] cursors = {c, matrix};
		MergeCursor mergedCursor = new MergeCursor( cursors );
		
		
		MyCustomAdapter adapter = new MyCustomAdapter(this, c);
        lv.setAdapter(adapter);
		
		
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Intent intent = new Intent("org.calflora.observer.action.NEWOBSERVATION");
				c.moveToPosition(arg2);
				intent.putExtra(Observer.NEW_PLANT_TAXON, c.getString(1) );
				setResult(RESULT_OK, intent);
				finish();

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
