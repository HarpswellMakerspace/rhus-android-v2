package org.calflora.observer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.calflora.observer.api.IdNameItem;
import org.calflora.observer.model.Plant;
import org.calflora.observer.model.Project;
import org.calflora.observer.model.ProjectStub;
import org.json.JSONException;

import net.smart_json_database.JSONEntity;
import net.smart_json_database.Order;
import net.smart_json_database.SearchFields;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.drawable.Drawable;

public class WorkspaceListFragment extends Fragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	protected ListView lv;
	
	class MyCustomAdaptor extends ArrayAdapter<JSONEntity>
	{
	    Context context;
	    int layoutResourceId;   
	    List<Integer> rowsUploaded = new ArrayList<Integer>();
	    
	    JSONEntity currentItem;
	    ArrayList<JSONEntity> data;
	    /** Called when the activity is first created. */
	    // TODO Auto-generated constructor stub
	    public MyCustomAdaptor(Context context, int layoutResourceId, ArrayList<JSONEntity> data) 
	    {
	        super(context,layoutResourceId,data);
	        this.layoutResourceId = layoutResourceId;
	        this.context=context;
	        this.data = data;
	    }
	    @Override
	    public View getView(int position, View convertView, ViewGroup parent)
	    {
	        View row = convertView;
	        MyStringReaderHolder holder;
	        
	        if(row==null)
	        {
	            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
	            row = inflater.inflate(layoutResourceId, parent,false);
	            
	            holder= new MyStringReaderHolder();
	            
	            holder.plantNameView =(TextView)row.findViewById(R.id.plant_name);
	            holder.plantImageView=(ImageView)row.findViewById(R.id.thumb_image_view);
	            holder.dateAddedView = (TextView)row.findViewById(R.id.date_added);
	            holder.backgroundView = (LinearLayout)row.findViewById(R.id.item_background);
	            
	            row.setTag(holder);
	        }
	        else
	        {
	            holder=(MyStringReaderHolder) row.getTag();
	        }
	        
	        currentItem = (JSONEntity) data.get(position);
	        //System.out.println("Position="+position);
	      
	        String taxon = "";
			try {
				taxon = currentItem.getString("taxon");
		        holder.plantNameView.setText(taxon);

			} catch (JSONException e) {
				holder.plantNameView.setText("Taxon not recorded");

			}
	       
	        try {
				holder.dateAddedView.setText(currentItem.getString("date_added"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				holder.dateAddedView.setText("Date not recorded");
			}
	        
	        try {
				if(currentItem.getInt("uploaded") != 1
				  && !rowsUploaded.contains( Integer.valueOf( position ) ) ){
					holder.backgroundView.setBackgroundColor( getResources().getColor(R.color.pending_observation_color));
				} else {
					holder.backgroundView.setBackgroundColor( getResources().getColor(R.color.white));

				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	        Plant plant = Project.getPlant(taxon);
	        if(plant != null){
	        	Drawable thumbnail = plant.getThumbnail(getActivity());
	        	holder.plantImageView.setImageDrawable(thumbnail);
	        } else {
	        	holder.plantImageView.setImageDrawable(null);
	        }
	        return row;
	    }
	    
	    class MyStringReaderHolder
	    {
	        TextView plantNameView;
	        ImageView plantImageView;
	        TextView dateAddedView;
	        LinearLayout backgroundView;
	    }
	    
	    public void setRowUploaded(int index){
	    	rowsUploaded.add(Integer.valueOf(index));
	    }
	}
	
	
	
	protected MyCustomAdaptor adapter;
	
	@Override
	public void onStart() {
		super.onStart();
		
		lv = (ListView) getView().findViewById(R.id.workspace_list_view);
		
		Collection<JSONEntity> points = getEntities();
		ArrayList<JSONEntity> listData = new ArrayList<JSONEntity>();
		for( JSONEntity p: points){
			listData.add(p);
		}
		
		
		adapter = new MyCustomAdaptor(getActivity(), R.layout.list_item_plant_observation, listData);
        lv.setAdapter(adapter);
        
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		return inflater.inflate(R.layout.fragment_workspace_list, container, false);
	}
	
	public Collection<JSONEntity> getEntities(){
		SearchFields search = SearchFields.Where("type", "observation");
		Collection<JSONEntity> entities = Observer.database.fetchByFields(search, new Order());
		return entities;
	}
}
