package org.calflora.observer;

import java.util.Locale;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.FragmentManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WorkspaceActivity extends Activity implements
		ActionBar.TabListener {

	RelativeLayout pendingTab;
	TextView pendingNumberLabel;
	
	ObservationMapFragment plantMapFragment;
	
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
		
		plantMapFragment = new ObservationMapFragment();
		
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
		
		plantMapFragment = new ObservationMapFragment();

		

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
		transaction.replace(R.id.workspace_fragment_container, plantMapFragment);

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

	

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	/*
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
	/*
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_workspace_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}
	*/

}
