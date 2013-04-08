package org.calflora.observer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.smart_json_databsase.InitJSONDatabaseExcepiton;
import net.smart_json_databsase.JSONDatabase;

import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import android.app.Application;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Observer extends Application {
	
	private static final String DB_NAME = "px7.sqlite"; //Hard coded for testing
	
	public static Organization organization;
	public static Project project;
	public static ObjectMapper mapper = new ObjectMapper();
	public static JSONDatabase database;
	public static SQLiteDatabase plantsListDatabase;
	
	public static void toast(String message, Context context){
		
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
		
	}
	

	// Application overrides
	@Override
	public void onCreate() {
		try {
			Observer.database = JSONDatabase.GetDatabase(getApplicationContext());
		} catch (InitJSONDatabaseExcepiton e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			copyDataBase();  // TODO Shouldn't do this on every load, but OK for now.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Observer.plantsListDatabase = SQLiteDatabase.openDatabase(getApplicationContext().getDatabasePath(DB_NAME).getAbsolutePath(), null, SQLiteDatabase.OPEN_READONLY | SQLiteDatabase.NO_LOCALIZED_COLLATORS);
		
		super.onCreate();
	}
	
	 private void copyDataBase() throws IOException{
		 
	    	//Open your local db as the input stream
	    	InputStream myInput = getApplicationContext().getAssets().open(DB_NAME);
	 
	    	// Path to the just created empty db
	    	File outFileName = getApplicationContext().getDatabasePath(DB_NAME);
	 
	    	//Open the empty db as the output stream
	    	OutputStream myOutput = new FileOutputStream( outFileName.getAbsolutePath() );
	 
	    	//transfer bytes from the inputfile to the outputfile
	    	byte[] buffer = new byte[1024];
	    	int length;
	    	while ((length = myInput.read(buffer))>0){
	    		myOutput.write(buffer, 0, length);
	    	}
	 
	    	//Close the streams
	    	myOutput.flush();
	    	myOutput.close();
	    	myInput.close();
	 
	    }
	
}
