package org.calflora.observer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.calflora.observer.model.ProjectStub;
import org.json.JSONException;

import net.smart_json_databsase.JSONEntity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.app.Fragment;

public class WorkspaceListFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	
	@Override
	public void onStart() {
		super.onStart();
		
		List<Map<String, String>> listData = new ArrayList<Map<String, String>>();
		Map<String, String> map = null;

		
		Collection<JSONEntity> points = Observer.database.fetchAllEntities();
		int i=1;

		for( JSONEntity p: points){
			try {
				if(p.getString("taxon") != null){
					map = new HashMap<String, String>();
					map.put("rowid", String.valueOf(i));
					map.put("col_1", (String) p.getString("taxon"));
					listData.add(map);
					i++;
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		String[] from = new String[] {"col_1"};
		int[] to = new int[] { R.id.col1 };
		
		SimpleAdapter adapter = new SimpleAdapter( getActivity(), listData, R.layout.list_item_single, from, to);
		View myView = getView();
		ListView lv = (ListView) myView.findViewById(R.id.workspace_list_view);
        lv.setAdapter(adapter);
		
	}





	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_workspace_list, container, false);
	}
}
