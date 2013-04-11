package org.calflora.observer.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIResponseSignIn extends APIResponseBase {
	// {"status":"OK", "data": "4RH0L0LM8"}	
	public String data;
}
