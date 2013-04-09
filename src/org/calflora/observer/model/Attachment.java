package org.calflora.observer.model;

import java.io.File;

import org.json.JSONException;

import net.smart_json_databsase.JSONEntity;

public class Attachment {

	public String name;
	public String localPath;
	public File file;
	public String MIMEType;
	
	
	public JSONEntity getJSON(){
		
		JSONEntity entity = new JSONEntity();
		try {
			entity.put("name", name);
			entity.put("MIMEType", MIMEType);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return entity;
		
	}
}
