package org.calflora.observer.model;

import net.smart_json_databsase.JSONEntity;

import org.calflora.observer.Observer;
import org.json.JSONException;

import android.location.Location;

public class Observation {
	
	public Plant plant;
	public Location location;
	
	public void storeObservation() throws JSONException{
		
		JSONEntity dataPoint = new JSONEntity();
			
		dataPoint.put("latitude", location.getLatitude());
		dataPoint.put("longitude", location.getLongitude());
		dataPoint.put("taxon", plant.getTaxon());
		
		// And insert into JSON Datastore
		// TODO: JSON Database should be moved to Project
		int id = Observer.database.store(dataPoint);

	}
}
