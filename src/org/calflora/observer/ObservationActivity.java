package org.calflora.observer;

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
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class ObservationActivity extends Activity implements
		ActionBar.TabListener {
	
	private ObservationSummaryFragment observationSummaryFragment;
	private ObservationAssessmentFragment observationAssessmentFragment;


	protected void done(){
		
		Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_observation);
		
		observationSummaryFragment = new ObservationSummaryFragment();
		observationAssessmentFragment = new ObservationAssessmentFragment();
	
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//actionBar.setBackgroundDrawable(R.drawable.map);
		//Drawable d = getApplicationContext().getResources().getDrawable(R.drawable.map);
		//actionBar.setBackgroundDrawable(d);
	
		actionBar.addTab(actionBar.newTab()
				.setText("Observation")
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setText("Assessment")
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
        			public void onClick(View v){

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
		
		
		// TODO: respond to edit intent
		Intent intent = getIntent();
		String taxon = intent.getStringExtra(Observer.NEW_PLANT_TAXON);
		Plant plant = Project.getPlant(taxon);
		
		// TODO: combine into constructor..
		Observer.currentObservation = new Observation();
		Observer.currentObservation.plant = plant;
		Location lastLocation = Observer.getInstance().getLastLocation();
		Observer.currentObservation.location = lastLocation;

		
		TextView commonName = (TextView)findViewById(R.id.common_name);
		commonName.setText(plant.getCommon() );
		TextView taxonName = (TextView)findViewById(R.id.taxon);
		taxonName.setText(plant.getTaxon() );
		
		
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
			//	transaction.replace(R.id.observation_fragment_container,observationAssessmentFragment );
				transaction.commit();
			break;
			
		case 2:
			// Treatment
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

	@Override
	public void onBackPressed() {
		onCancel();
		return;
	}
	
	protected void onCancel(){
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
