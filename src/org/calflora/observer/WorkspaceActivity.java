package org.calflora.observer;

import java.util.Collection;

import net.smart_json_databsase.JSONEntity;
import net.smart_json_databsase.SearchFields;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WorkspaceActivity extends BaseActivity implements
		ActionBar.TabListener {

	private enum Tabs { MAP, LIST, UPLOAD, ADD };
	private Tabs selectedTab;
	
	private RelativeLayout pendingTab;
	private TextView pendingNumberLabel;
	
	private WorkspaceMapFragment workspaceMapFragment;
	private WorkspaceListFragment workspaceListFragment;
	private WorkspaceUploadFragment workspaceUploadFragment;
	
	
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	//ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_workspace);

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		//actionBar.setBackgroundDrawable(R.drawable.map);
		//Drawable d = getApplicationContext().getResources().getDrawable(R.drawable.map);
		//actionBar.setBackgroundDrawable(d);
		
		workspaceMapFragment = new WorkspaceMapFragment();
		workspaceListFragment = new WorkspaceListFragment();
		workspaceUploadFragment = new WorkspaceUploadFragment();

		
		pendingTab = (RelativeLayout)getLayoutInflater().inflate(R.layout.tab_pending, null);		
	
		actionBar.addTab(actionBar.newTab()
				.setIcon(R.drawable.light_map)
				.setTabListener(this)
				);
		actionBar.addTab(actionBar.newTab()
				.setIcon(R.drawable.light_list)
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
	protected void onResume() {
		super.onResume();
		
		if(selectedTab == Tabs.ADD){
			selectedTab = Tabs.MAP; 
		}
		
		if(selectedTab != Tabs.MAP){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			selectedTab = Tabs.MAP;
			transaction.replace(R.id.workspace_fragment_container, workspaceMapFragment);
			transaction.commit();
		}
		
		
		updatePendingTotal();


	}
	
	public void updatePendingTotal(){
		pendingNumberLabel = (TextView) pendingTab.findViewById(R.id.pending_number_label);

		SearchFields search = SearchFields.Where("uploaded", 0);
		Collection<JSONEntity> entities = Observer.database.fetchByFields(search);
		pendingNumberLabel.setText(  String.valueOf(entities.size() ) ); //TODO: query the json database for pending
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.workspace, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.menu_settings:
	            showSettings();
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
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
			selectedTab = Tabs.LIST;
			transaction.replace(R.id.workspace_fragment_container, workspaceListFragment);
			transaction.commit();
			break;
			
		case 2:
			transaction = fragmentManager.beginTransaction();
			selectedTab = Tabs.UPLOAD;
			transaction.replace(R.id.workspace_fragment_container, workspaceUploadFragment);
			transaction.commit();
			break;
			
		case 3:
			if(Observer.getInstance().getLastLocation() != null){
				selectedTab = Tabs.ADD;
				Intent intent = new Intent("org.calflora.observer.action.PLANTSELECTOR");
				startActivity(intent);	
			} else {
				Observer.toast("Please wait for a geofix", getApplicationContext());
			}
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
	
}
