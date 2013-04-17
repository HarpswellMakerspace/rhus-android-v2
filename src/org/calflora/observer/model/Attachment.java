package org.calflora.observer.model;

import net.smart_json_databsase.JSONEntity;

import org.json.JSONException;

public class Attachment extends java.lang.Object {

	public String type;
	public String name;
	public String localPath;
	public String MIMEType;
	
	public JSONEntity getJSONEntity(){
		
		JSONEntity entity = new JSONEntity();
		try {
			entity.put("type", "attachment");
			entity.put("name", name);
			entity.put("localPath", localPath);
			entity.put("MIMEType", MIMEType);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return entity;
		
	}
	


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public String getLocalPath() {
		return localPath;
	}


	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}


	public String getMIMEType() {
		return MIMEType;
	}


	public void setMIMEType(String mIMEType) {
		MIMEType = mIMEType;
	}
	
}
