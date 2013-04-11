package org.calflora.observer.model;

import java.util.List;

import org.calflora.observer.Observer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Organization {

	public String id;
	
	@Deprecated
	private String orgId; // TODO workaround until API change
	
	public String name;
	public String splashGraphic;
	public String logoGraphic;
	public String orgUrl;
	public List<ProjectStub> projects;

	
	public Organization() {
		super();
	}

	public void setOrgId(String value){
		orgId = value;
		id = value;
	}

	public String getOrgId() {
		return orgId;
	}
	
	
	public String getLogoGraphicPath(){
		return "organization_logo_" + id;
	}

	public String getSplashGraphicPath(){
		return "organization_splash_" + id;
	}

	
}
