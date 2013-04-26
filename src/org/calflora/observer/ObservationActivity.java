package org.calflora.observer;

import java.io.IOException;

import org.calflora.observer.model.Observation;
import org.calflora.observer.model.Plant;
import org.calflora.observer.model.Project;
import org.json.JSONException;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ObservationActivity extends Activity implements
		ActionBar.TabListener {
	
	private static final int SELECT_PLANT = 1001;
	private ObservationSummaryFragment observationSummaryFragment;
	private ObservationAssessmentFragment observationAssessmentFragment;
	private ObservationTreatmentFragment observationTreatmentFragment;

	private ImageView plantThumbnailView;


	protected void done(){
		finish();

		/*
		Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		 */
	}
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_observation);
		
		observationSummaryFragment = new ObservationSummaryFragment();
		observationAssessmentFragment = new ObservationAssessmentFragment();
		observationTreatmentFragment = new ObservationTreatmentFragment();
		
		plantThumbnailView = (ImageView) findViewById(R.id.plant_thumbnail);
	
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		actionBar.addTab(actionBar.newTab()
				.setText("Observation")
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setText("Assessment")
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setText("Treatment")
				.setTabListener(this)
				);
		
		//actionBar.setHomeButtonEnabled(true);
		
		View homeIcon = findViewById(android.R.id.home);
		((View) homeIcon.getParent()).setVisibility(View.GONE);
		
		TextView titleView = (TextView) findViewById(R.id.actionSheetTitleView);
		titleView.setText("New Observation");
		
		ImageButton cancelButton = (ImageButton) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(
        		new OnClickListener(){
        			public void onClick(View v){
        				onCancel();
        			}
        		}
        		);
		

		
		ImageButton doneButton = (ImageButton) findViewById(R.id.doneButton);
		doneButton.setOnClickListener(
        		new OnClickListener(){
        			
        			Boolean storingObservation = false;
        		    Object lock = new Object();

        			public void onClick(View v){
	
        				synchronized (lock) {
        		
        					if(storingObservation){
            					return;
            				}
            				storingObservation = true;
            				          
        				}
        				
        				//Read data from fragments and store.
        				/*
        				 "locdesc":"between the big rock and the oak tree",
        				  "Habitat":"riparian",
        				  "Notes":"seems to be spreading downhill",
        				*/
        				
        				
        				
        				try {
        					Observer.currentObservation.storeObservation();
        				} catch (JSONException e1) {
        					Observer.toast("JSON Failed", getApplicationContext());
        					e1.printStackTrace();
        					return;
        				}

        				done();

        			}
        		}
        		);

		
		// TODO: combine into constructor..
		Observer.currentObservation = new Observation();
		Location lastLocation = Observer.getInstance().getLastLocation();
		Observer.currentObservation.latitude = lastLocation.getLatitude();
		Observer.currentObservation.longitude = lastLocation.getLongitude();	
		
		Button changePlantButton = (Button) findViewById(R.id.plant_change_button);
		changePlantButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Intent intent = new Intent("org.calflora.observer.action.PLANTSELECTOR");
				startActivityForResult(intent, SELECT_PLANT);				
			}
			
		
		});
	}



	private void loadPlant(String taxon) {
		Plant plant = Project.getPlant(taxon);
		Observer.currentObservation.plant = plant;

		TextView commonName = (TextView)findViewById(R.id.common_name);
		TextView taxonName = (TextView)findViewById(R.id.taxon);
		
		if(! taxon.equals("unknown")){
			commonName.setText(plant.getCommon() );
			taxonName.setText(plant.getTaxon() );
			AssetManager assets = getBaseContext().getResources().getAssets();
			AssetFileDescriptor asset = null;
			try {
				String imagePath = "plant_images/" + plant.getPhotoid().replace("'","")+".jpeg";
				asset = assets.openFd(imagePath);
				Drawable plantThumbnail = Drawable.createFromStream(asset.createInputStream(), "");
				plantThumbnailView.setImageDrawable(plantThumbnail);
			} catch (IOException e) {
				// TODO Show default image for plant
			}
		} else {
			//Placehold for unknown plant icon
			plantThumbnailView.setImageResource(R.drawable.calflora_observer_icon);
			commonName.setText("Unknown Taxon");
			//taxonName.setText(plant.getTaxon() );

		}
		
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.observation, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction;
		
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		switch (tab.getPosition()){
		case 0:
		    transaction = fragmentManager.beginTransaction();
			//selectedTab = Tabs.SUMMARY;
			transaction.replace(R.id.observation_fragment_container,observationSummaryFragment );
			transaction.commit();
			break;
			
		case 1:
			// Assessment, or ODK configured 2nd tab
		    transaction = fragmentManager.beginTransaction();
				//selectedTab = Tabs.SUMMARY;
				transaction.replace(R.id.observation_fragment_container,observationAssessmentFragment );
				transaction.commit();
			break;
			
		case 2:
			// Treatment, or ODK configured 3rd tab
		    transaction = fragmentManager.beginTransaction();
				//selectedTab = Tabs.SUMMARY;
				transaction.replace(R.id.observation_fragment_container,observationTreatmentFragment );
				transaction.commit();
			break;
			
	
		}
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(resultCode == Activity.RESULT_CANCELED){
			return;
			
		} else if(!(resultCode == Activity.RESULT_OK)){
			Observer.toast("Error getting plant: result code is not OK", this);
			return;
		}
		
		
		try
		{
			switch (requestCode) {
			case SELECT_PLANT: //SELECT_PLANT
				if (resultCode == Activity.RESULT_OK)
				{
					Bundle data =  intent.getExtras();
					if(data == null){
						Observer.toast("Error getting plant:", this);
						return;
					}
					
					String taxon = data.getString(Observer.NEW_PLANT_TAXON);
					loadPlant(taxon);
			
					
				}
				break;
			}
		}
		catch (Throwable ex)
		{
			ex.printStackTrace();
			Observer.toast("trouble choosing plant " + ex, this);
		}
	}
	
	@Override
	public void onBackPressed() {
		onCancel();
		return;
	}
	
	protected void onCancel(){
		@SuppressWarnings("unused")
		AlertDialog ad = new AlertDialog.Builder(this).setMessage(
				"Clicking OK will discard all your data for this entry.").setTitle(
						"Are you sure?").setCancelable(false)
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// User selects OK, cancel this observation
								done();
							}
						}).setNeutralButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// User selects Cancel, do nothing
							}
						}).show();	    
		return;
	}
	

}
