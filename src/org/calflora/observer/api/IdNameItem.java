package org.calflora.observer.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class IdNameItem {
	public String id;
	public String name;
	
	@JsonIgnoreProperties("logoGraphic")
	public String logoGraphic;
}
