package org.calflora.observer;

import java.util.Collection;

import net.smart_json_database.JSONEntity;
import net.smart_json_database.SearchFields;
import android.app.ActionBar;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.actionbarsherlock.view.*;

import android.widget.RelativeLayout;
import android.widget.TextView;

import com.octo.android.robospice.JacksonSpringAndroidSpiceService;
import com.octo.android.robospice.SpiceManager;

public class WorkspaceActivity extends BaseActivity implements
		ActionBar.TabListener {

	private enum Tabs { MAP, UPLOAD, ADD };
	private Tabs selectedTab;
	
	private RelativeLayout pendingTab;
	private TextView pendingNumberLabel;
	
	private WorkspaceMapFragment workspaceMapFragment;
	//private WorkspaceListFragment workspaceListFragment;
	private WorkspaceUploadFragment workspaceUploadFragment;
	
	protected static final String JSON_CACHE_KEY = "CACHE_KEY";
	protected SpiceManager spiceManager = new SpiceManager( JacksonSpringAndroidSpiceService.class );
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		//		GoogleMapOptions options =  new GoogleMapOptions();
		//		options.zoomControlsEnabled(false);
		//		options.zoomGesturesEnabled(false);
		//	mapFragment = MapFragment.newInstance(options);
		//  Need to override this factory class if we want to access the controls settings
		//		workspaceMapFragment = (WorkspaceMapFragment) WorkspaceMapFragment.newInstance(options);

		workspaceMapFragment = new WorkspaceMapFragment();
		//workspaceListFragment = new WorkspaceListFragment();
		workspaceUploadFragment = new WorkspaceUploadFragment();

		pendingTab = (RelativeLayout)getLayoutInflater().inflate(R.layout.tab_pending, null);		
	
		actionBar.addTab(actionBar.newTab()
				.setIcon(R.drawable.light_map)
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setCustomView(pendingTab)
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setIcon(R.drawable.light_add)
				.setTabListener(this)
				);

	}



	@Override
	protected void onStart() {
		super.onStart();
		spiceManager.start( this );
	}


	@Override
	protected void onStop() {
		spiceManager.shouldStop();
		super.onStop();
	}


	@Override
	protected void onResume() {
		super.onResume();
		
		if(selectedTab == Tabs.ADD){
			selectedTab = Tabs.MAP; 
		}

		updatePendingTotal();
	}
	
	public void updatePendingTotal(){
		pendingNumberLabel = (TextView) pendingTab.findViewById(R.id.pending_number_label);

		SearchFields search = SearchFields.Where("uploaded", 0);
		Collection<JSONEntity> entities = Observer.database.fetchByFields(search);
		pendingNumberLabel.setText(  String.valueOf(entities.size() ) ); //TODO: query the json database for pending
				
	}
	
	public void updateFragments(){
		
		updatePendingTotal();
		
		if(selectedTab == Tabs.MAP){
			getActionBar().setSelectedNavigationItem(0);
		} else {
			getActionBar().setSelectedNavigationItem(1);	
			
			workspaceUploadFragment.notifyListChanged();
		}

	
		
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.workspace, (Menu) menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		/*
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	            showSettings();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	    */
		return false;
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		// Called after creating an observation
		updateFragments();
	}
	
	protected void showSettings(){
		Intent intent = new Intent("org.calflora.observer.action.SETTINGS");
		startActivity(intent);	
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		//mViewPager.setCurrentItem(tab.getPosition());
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction;
		
		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		switch (tab.getPosition()){
		case 0:
		    transaction = fragmentManager.beginTransaction();
			selectedTab = Tabs.MAP;
			transaction.replace(R.id.workspace_fragment_container, workspaceMapFragment);
			transaction.commit();
			break;
			
		case 1:
			transaction = fragmentManager.beginTransaction();
			selectedTab = Tabs.UPLOAD;
			transaction.replace(R.id.workspace_fragment_container, workspaceUploadFragment);
			transaction.commit();
			break;
			
		case 2:
			if(Observer.getInstance().getLastLocation() == null){
				Observer.toast("Please wait for a geofix", getApplicationContext());
			}
			Intent intent = new Intent("org.calflora.observer.action.NEWOBSERVATION");
			startActivityForResult(intent, 0);	
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

		switch (tab.getPosition()){
		case 3:
			if(Observer.getInstance().getLastLocation() != null){
				Intent intent = new Intent("org.calflora.observer.action.PLANTSELECTOR");
				startActivity(intent);	
			} else {
				Observer.toast("Please wait for a geofix", getApplicationContext());
			}
			break;

		}
		
	}
	
	public SpiceManager getSpiceManager(){
		if(! spiceManager.isStarted()){
			spiceManager.start(this);
			Log.d("Spice Manager Restarting", "Spice Manager Restarting");
		}
		return spiceManager;
		
	}
}
