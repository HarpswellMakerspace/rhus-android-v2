package org.calflora.observer;

import java.util.Locale;

import net.smart_json_databsase.JSONEntity;

import org.json.JSONException;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class ObservationActivity extends FragmentActivity implements
		ActionBar.TabListener {

	
	protected void done(){
		
		Intent intent = new Intent("org.calflora.observer.action.WORKSPACE");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_observation);

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
        				//Map<String,Object> dataPoint = new HashMap<String,Object>();
        				JSONEntity dataPoint = new JSONEntity();
        				try {
							//dataPoint.put("latitude", lastLocation.getLatitude());
	        				//dataPoint.put("longitude", lastLocation.getLongitude());
        					dataPoint.put("latitude", 43);
	        				dataPoint.put("longitude", -112);
						} catch (JSONException e1) {
							Observer.toast("JSON Failed", getApplicationContext());
							e1.printStackTrace();
							return;
						}
        				
        				//And insert into JSON Datastore
        				//In the future this will got into 'Observer' as part of 'newObservation' for staging.
        				
        				int id = Observer.database.store(dataPoint);
        				done();
        			}
        		}
        		);
		
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
	//	mViewPager.setCurrentItem(tab.getPosition());
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
