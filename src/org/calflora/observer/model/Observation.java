package org.calflora.observer.model;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import net.smart_json_databsase.JSONEntity;

import org.calflora.observer.Observer;
import org.json.JSONException;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import android.content.Context;
import android.os.Environment;

public class Observation {

	public double latitude;
	public double longitude;

	// TODO Plant should be refactored..
	// This should be a big old String, String map!
	public Plant plant;
	
	public ArrayList<Attachment> attachments;
	
	public static Observation loadObservationFromEntity(JSONEntity entity) throws JSONException, JsonParseException, JsonMappingException, IOException{
		Observation o = new Observation();
		o.latitude = entity.getDouble("latitude");
		o.longitude = entity.getDouble("longitude");
		o.plant.setTaxon(entity.getString("taxon"));
		
		Collection<Integer> collection = entity.hasMany("attachments");  // TODO This should really return the objects themselves
		for(Integer i : collection){
			JSONEntity attachmentEntity = Observer.database.fetchById(i);
			Attachment a = Observer.mapper.readValue(attachmentEntity.getData().toString(), Attachment.class);
			o.attachments.add(a);
		}
		
		return o;

	}
	
	public Observation(){
		plant = new Plant();
		attachments = new ArrayList<Attachment>();
	}
	
	public void addAttachment(String name, byte[] bytes, String MIMEType, Context context) throws IOException{
		
		removeAttachment(name);
		
		Attachment attachment = new Attachment();
		
		String uniqueId = UUID.randomUUID().toString();
		
		// TODO where does the file get stored?
		FileOutputStream outputStream = context.openFileOutput(uniqueId, Context.MODE_PRIVATE);
		outputStream.write(bytes);
		outputStream.close();
				
		attachment.localPath = uniqueId;
		attachment.name = name;
		attachment.MIMEType = MIMEType;
		
		attachments.add(attachment);
		
	}
	
	public void removeAttachment(String name){
		for(Attachment a : attachments){
			if(a.name == name){
				attachments.remove(a);
				break;
			}
		}
	}
	
	public String getAttachmentPath(String name, Context context) throws FileNotFoundException{
		
		String localPath = null;
		for(Attachment a : attachments){
			if(a.name == name){
				localPath = a.localPath;
				break;
			}
		}
		if(localPath == null){
			throw new FileNotFoundException();
		}
		
		return localPath;
		
	}
	
	public void storeObservation() throws JSONException{
		
		JSONEntity dataPoint = new JSONEntity();
		
		// And insert into JSON Datastore
		// TODO: JSON Database should be moved to Project
		dataPoint.put("type", "observation");
		dataPoint.put("latitude", latitude);
		dataPoint.put("longitude", longitude);
		dataPoint.put("taxon", plant.getTaxon());
		dataPoint.put("uploaded", 0);

		//Store the attachments
		for(Attachment a : attachments){
			// TODO thie process could be automated within smart json database
			JSONEntity attachmentEntity = a.getJSONEntity();
			int attachmentId = Observer.database.store(attachmentEntity);
			dataPoint.addIdToHasMany("attachments", attachmentId);
		}
		
		Observer.database.store(dataPoint);
		
	}
	
	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
}
