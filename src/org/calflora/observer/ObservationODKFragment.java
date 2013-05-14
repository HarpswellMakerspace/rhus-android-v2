package org.calflora.observer;

import org.odk.collect.android.views.ODKView;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


// TODO starting point for ODK integration

public class ObservationODKFragment extends Fragment {

	private ODKView odkv;
	
	public ObservationODKFragment(ODKView odkv) {
		super();
		this.odkv = odkv;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		LinearLayout view = new LinearLayout(getActivity());		
    	view.addView(odkv);
    	return view;
    
	}

}
