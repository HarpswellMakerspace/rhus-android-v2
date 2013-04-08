package org.calflora.observer;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ObservationSummaryFragment extends Fragment {

	private View layout = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		if(layout == null) {
			layout = inflater.inflate(R.layout.fragment_observation_summary, container, false);
		}
		return layout;
	}
}
