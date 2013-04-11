package org.calflora.observer.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIResponseBase {
	public String status;
	public String code;
	public String message;
}
