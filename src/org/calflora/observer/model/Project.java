package org.calflora.observer.model;

import java.util.ArrayList;

import org.calflora.observer.Observer;

import android.database.Cursor;

public class Project {

	public String id;
	
	@Deprecated
	public String orgId; // @deprecated This var will removed in the near future.
	@Deprecated
	private String projectId;
	
	public double center_lat;
	public double center_lng;
	public String tilepackage;
	public int tilepackageSize;
	
	@Deprecated
	public ArrayList<Object> plantlist;
	
	public static Plant getPlant(String taxon) {
		
		Cursor c = Observer.plantsListDatabase.query("plist", 
				  new String[] { "taxon", "common", "nstatus", "lifeform", "crn", "family", "photoid"  }, 
				  "taxon = '"+taxon+"'", null, null, null, null); 
		c.moveToFirst();
		
		Plant plant = new Plant();
		plant.setTaxon(c.getString(0));
		plant.setCommon(c.getString(1));
		plant.setNstatus(c.getInt(2));
		plant.setLifeform(c.getString(3));
		plant.setFamily(c.getString(5));
		plant.setPhotoid(c.getString(6));
		return plant;
	}
	
	public Project() {
		super();
	}

	@Deprecated
	public String getProjectId() {
		return projectId;
	}

	@Deprecated
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	
}
