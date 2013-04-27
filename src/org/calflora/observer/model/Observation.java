package org.calflora.observer.model;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;

import net.smart_json_database.JSONEntity;
import net.winterroot.rhus.util.RHImage;

import org.calflora.observer.Observer;
import org.json.JSONException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Observation {

	public double latitude;
	public double longitude;
	public boolean uploaded;
	public String date_added;
	public int timestamp_added;

	// TODO Plant should be refactored..
	// This should be a big old String, String map!
	public Plant plant;
	
	public ArrayList<Attachment> attachments;
	
	public static Observation loadObservationFromEntity(JSONEntity entity) throws JSONException, JsonParseException, JsonMappingException, IOException{
		Observation o = new Observation();
		
		
		//Observation o = Observer.mapper.readValue(entity.getData().toString(), Observation.class);
		// TODO The object maping for the Observation class need to be refactored so we can use Jackson here
		// and then the jackson line above should go into smart_json, as an additional method.
		
		o.latitude = entity.getDouble("latitude");
		o.longitude = entity.getDouble("longitude");
		
		try {
			o.plant.setTaxon(entity.getString("taxon"));
		} catch (Exception e){
			o.plant.setTaxon("unknown"); // TODO this really isn't the right idea.. taxon should be Observation level parameter, primary key
		}
		o.date_added = entity.getString("date_added");
		o.timestamp_added = entity.getInt("timestamp_added");
		
		Collection<Integer> collection = entity.hasMany("attachments");  // TODO This should really return the objects themselves
		for(Integer i : collection){
			JSONEntity attachmentEntity = Observer.database.fetchById(i);
			String attachmentString = attachmentEntity.getData().toString();
			Attachment a = Observer.mapper.readValue( attachmentString, Attachment.class);
			o.attachments.add(a);
		}
		
		return o;

	}
	/*
	public static byte[] createThubmnailBytes(String photoFileName){

		Bitmap thumb = RHImage.resizeBitMapImage(photoFileName, 140, 120, 90);
		return Observation.createThumbnail
	}

	
	public static byte[] createThumbnailBytes(Bitmap bitmap){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		thumb.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] thumbBytes = stream.toByteArray();
		return thumbBytes;
	}
	*/
	
	
	public static byte[] createFullImageBytes(Bitmap image){

		//image = RHImage.rotateImage(image);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] imageBytes = stream.toByteArray();
		return imageBytes;
		
	}
	
	public static byte[] createFullImageBytes(String photoFileName){

		Bitmap image = BitmapFactory.decodeFile(photoFileName);
		return createFullImageBytes(image);
		
		/*
		Bitmap image = RHImage.resizeBitMapImage(photoFileName, 1200, 1600, 90);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, stream);
		byte[] imageBytes = stream.toByteArray();
		return imageBytes;
		*/
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
		
		return context.getFilesDir() + "/" + localPath;
		
	}
	
	public void storeObservation() throws JSONException{
		
		JSONEntity dataPoint = new JSONEntity();
		
		// And insert into JSON Datastore
		// TODO: JSON Database should be moved to Project
		dataPoint.put("type", "observation");
		dataPoint.put("latitude", latitude);
		dataPoint.put("longitude", longitude);
		if(plant != null && plant.getTaxon() != null){
			dataPoint.put("taxon", plant.getTaxon());
		}
		SimpleDateFormat s = new SimpleDateFormat("yyyy-MM-dd hh:mm");
		String format = s.format(new Date());
		Long tsLong = System.currentTimeMillis()/1000;
		//String ts = tsLong.toString();
		dataPoint.put("date_added", format);
		dataPoint.put("timestamp_added", tsLong);
		dataPoint.put("uploaded", 0);
		int id = Observer.database.store(dataPoint);
		dataPoint = Observer.database.fetchById(id);
		
		//Store the attachments
		for(Attachment a : attachments){
			// TODO this process could be automated within smart json database
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
