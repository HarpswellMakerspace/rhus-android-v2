package org.calflora.observer.model;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.smart_json_database.JSONEntity;

import org.calflora.observer.Observer;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.json.JSONException;

import android.content.Context;
import android.os.Environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL) 
public class Observation {
	
	public JSONEntity entity = null;

	// Instance variables for all observations
	public double latitude;
	public double longitude;
	public int uploaded;
	public int record_sent = 0;
	public int thumbnail_sent = 0;
	public int image_sent = 0;
	public String date_added;
	public int timestamp_added;
	public String documentId = "";
	public String revision = "";

	// Dynamic lists of observation specific data
	public Map<String, Object> fields;	
	public List<Attachment> attachments;
	
	public static Observation loadObservationFromEntity(JSONEntity entity) throws JSONException, JsonParseException, JsonMappingException, IOException{
		Observation o = new Observation();
		
		o.entity = entity;
		
		// Observation o = Observer.mapper.readValue(entity.getData().toString(), Observation.class);
		// TODO ? The object mapping for the Observation class need to be refactored so we can use Jackson here
		// and then the Jackson line above should go into smart_json, as an additional method.
		// ALL these fields could be stuffed into the jackson mapper if we wanted..
		
		o.latitude = entity.getDouble("latitude");
		o.longitude = entity.getDouble("longitude");
		o.date_added = entity.getString("date_added");
		o.timestamp_added = entity.getInt("timestamp_added");
		o.uploaded = entity.getInt("uploaded");
		o.record_sent = entity.getInt("record_sent");
		o.revision = entity.getString("revision");
		o.documentId = entity.getString("documentId");
		o.thumbnail_sent = entity.getInt("thumbnail_sent");
		o.image_sent = entity.getInt("image_sent");


		List<String> ivars = Arrays.asList("type", "latitude", "longitude", "date_added", "timestamp_added", "uploaded");
		
		Collection<String> keys = entity.dataKeys();
		for(String key : keys){
			if(ivars.contains(key)){
				continue;
			}
			o.fields.put(key, entity.getString(key));
		}
		
		Collection<Integer> attachmentIds = entity.hasMany("attachments");
		for(Integer i : attachmentIds){
			JSONEntity attachmentEntity = Observer.database.fetchById(i);
			String attachmentString = attachmentEntity.getData().toString();
			Attachment a = Observer.mapper.readValue( attachmentString, Attachment.class);
			o.attachments.add(a);
		}
		
		return o;

	}
	
	public Observation(){
		fields = new HashMap<String, Object>();
		attachments = new ArrayList<Attachment>();
	}
	
	public void setField(String key, String value){
		fields.put(key, value);
	}
	
	public void setAttachment(String name, byte[] bytes, String MIMEType, Context context) throws IOException{
		
		removeAttachment(name, context);
		
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
	
	public void removeAttachment(String name, Context context){
		for(Attachment a : attachments){
			if(a.name == name){
				attachments.remove(a);
				context.deleteFile(a.localPath);
				break;
			}
		}
	}
	
	public void removeAllAttachments(Context context) {
		List<Attachment> shallowList = new ArrayList<Attachment>(attachments);
		for(Attachment a : shallowList){
			attachments.remove(a);
			context.deleteFile(a.localPath);
		}
	}
	
	public String getAttachmentPath(String name, Context context) throws FileNotFoundException{
		
		String localPath = null;
		for(Attachment a : attachments){
			if(a.name.contentEquals(name)){
				localPath = a.localPath;
				break;
			}
		}
		if(localPath == null){
			throw new FileNotFoundException();
		}
		
		return context.getFilesDir() + "/" + localPath;
		
	}
	
	public void storeObservation() throws JSONException{
		
		if(entity == null){
			entity = new JSONEntity();
		}
		
		// Prepare values
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String format = s.format(new Date());
		Long tsLong = System.currentTimeMillis()/1000;
		
		// TODO: JSON Database should be moved to Project
		entity.put("type", "observation");
		entity.put("latitude", latitude);
		entity.put("longitude", longitude);
		entity.put("date_added", format);
		entity.put("timestamp_added", tsLong);
		entity.put("uploaded", uploaded);
		entity.put("record_sent", record_sent);
		entity.put("documentId", documentId);
		entity.put("revision", revision);
		entity.put("thumbnail_sent", thumbnail_sent);
		entity.put("image_sent", image_sent);

		for(String key : fields.keySet()){
			String value = (String) fields.get(key);
			if(value != null){
				entity.put(key, value);
			}
		}
		
		int id = Observer.database.store(entity);
		entity = Observer.database.fetchById(id);
		
		//Store the attachments
		for(Attachment a : attachments){
			// TODO this process could be automated within smart json database
			JSONEntity attachmentEntity = a.getJSONEntity();
			int attachmentId = Observer.database.store(attachmentEntity);
			entity.addIdToHasMany("attachments", attachmentId);
		}
		
		Observer.database.store(entity);
		
	}
	
	public Map<String, Object> getFields(){
		return fields;
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
