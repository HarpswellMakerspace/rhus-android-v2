package org.calflora.observer;

import net.smart_json_databsase.InitJSONDatabaseExcepiton;
import net.smart_json_databsase.JSONDatabase;

import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Observer extends Application {
	
	public static Organization organization;
	public static Project project;
	public static ObjectMapper mapper = new ObjectMapper();
	public static JSONDatabase database;

	
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
		super.onCreate();
	}
	
	
}
