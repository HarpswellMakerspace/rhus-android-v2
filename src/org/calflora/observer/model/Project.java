package org.calflora.observer.model;


import org.calflora.observer.Observer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import android.database.Cursor;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Project {

	public String id;
	
	@Deprecated
	public String orgId; // @deprecated This var will removed in the near future.
	@Deprecated
	private String projectId;
	
	public String name;
	@Deprecated
	public String projectName;
	
	
	public double center_lat;
	public double center_lng;
	public String tilepackage;
	public int tilepackageSize;
	
	public String plantlist;
	public String imgbundle;
	
	public static Plant getPlant(String taxon) {
		
		if(taxon == "unknown"){
			return null;
		}
		
		Cursor c = Observer.plantsListDatabase.query("plist", 
				  new String[] { "taxon", "common", "nstatus", "lifeform", "family", "photoid"  }, 
				  "taxon = ?", new String[]{ taxon } , null, null, null); 
		if(!c.moveToFirst()){
			return null;
		}
		
		Plant plant = new Plant();
		plant.setTaxon(c.getString(0));
		plant.setCommon(c.getString(1));
		plant.setNstatus(c.getInt(2));
		plant.setLifeform(c.getString(3));
		plant.setFamily(c.getString(4));
		plant.setPhotoid(c.getString(5));
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
	
	@Deprecated
	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String name) {
		this.name = name;
		this.projectName = name;
	}

}
