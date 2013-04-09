package org.calflora.observer.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.smart_json_databsase.JSONEntity;

import org.calflora.observer.Observer;
import org.json.JSONException;

import android.content.Context;
import android.location.Location;
import android.os.Environment;

public class Observation {

	public Location location;

	// TODO Plant should be refatored..
	public Plant plant;
	
	public ArrayList<Attachment> attachments;
	
	
	public Observation(){
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
		dataPoint.put("latitude", location.getLatitude());
		dataPoint.put("longitude", location.getLongitude());
		dataPoint.put("taxon", plant.getTaxon());
		
		Collection<JSONEntity> entities = new ArrayList<JSONEntity>();
		for(Attachment a : attachments){
			entities.add(a.getJSON());
		}
		dataPoint.put("attachments", entities);
		
		int id = Observer.database.store(dataPoint);
		
		//And now store the attachments
		
		
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
