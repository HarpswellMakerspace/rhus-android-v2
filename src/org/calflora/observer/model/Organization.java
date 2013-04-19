package org.calflora.observer.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties({"logoGraphicPath", "splashGraphicPath"})
public class Organization {

	public String id;	
	public String name;

	public String splashGraphic;
	public String logoGraphic;
	public String orgUrl;
	public List<ProjectStub> projects;

	public Organization() {
		super();
	}

	public void setOrgId(String value){
		id = value;
	}

	public String getOrgId() {
		return id;
	}
	
	public String getLogoGraphicPath(){
		return "organization_logo_" + id;
	}

	public String getSplashGraphicPath(){
		return "organization_splash_" + id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOrgName() {
		return name;
	}

	public void setOrgName(String name) {
		this.name = name;
	}
	
}
