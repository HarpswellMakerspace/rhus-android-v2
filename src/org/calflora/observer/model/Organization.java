package org.calflora.observer.model;

import java.util.List;

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
	
	
}
