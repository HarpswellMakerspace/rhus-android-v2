package org.calflora.observer;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WorkspaceActivity extends Activity implements
		ActionBar.TabListener {

	private enum Tabs { MAP, LIST, UPLOAD };
	private Tabs selectedTab;
	
	private RelativeLayout pendingTab;
	private TextView pendingNumberLabel;
	
	private ObservationMapFragment observationMapFragment;
	private ObservationListFragment observationListFragment;
	private ObservationUploadFragment observationUploadFragment;
	
	
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
		
		observationMapFragment = new ObservationMapFragment();
		observationListFragment = new ObservationListFragment();
		observationUploadFragment = new ObservationUploadFragment();

		
		pendingTab = (RelativeLayout)getLayoutInflater().inflate(R.layout.tab_pending, null);		
		
		pendingNumberLabel = (TextView) pendingTab.findViewById(R.id.pending_number_label);

		pendingNumberLabel.setText("000"); //TODO: query the json database for not pending
	
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

		if(selectedTab != Tabs.MAP){
			FragmentManager fragmentManager = getFragmentManager();
			FragmentTransaction transaction = fragmentManager.beginTransaction();
			selectedTab = Tabs.MAP;
			transaction.replace(R.id.workspace_fragment_container, observationMapFragment);
			transaction.commit();
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.workspace, menu);
		return true;
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		//mViewPager.setCurrentItem(tab.getPosition());
		
		FragmentManager fragmentManager = getFragmentManager();
		FragmentTransaction transaction = fragmentManager.beginTransaction();

		// Replace whatever is in the fragment_container view with this fragment,
		// and add the transaction to the back stack
		switch (tab.getPosition()){
		case 0:
			selectedTab = Tabs.MAP;
			transaction.replace(R.id.workspace_fragment_container, observationMapFragment);
			break;
		case 1:
			selectedTab = Tabs.LIST;
			transaction.replace(R.id.workspace_fragment_container, observationListFragment);
			break;
		case 2:
			selectedTab = Tabs.UPLOAD;
			transaction.replace(R.id.workspace_fragment_container, observationUploadFragment);
			break;
		case 3:
			 Intent intent = new Intent("org.calflora.observer.action.PLANTSELECTOR");
	    	 startActivity(intent);
			break;
		}

		// Commit the transaction
		transaction.commit();
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
	
}
