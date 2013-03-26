package org.calflora.observer;

import org.calflora.observer.model.Organization;
import org.calflora.observer.model.Project;

import android.content.Context;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Observer {
	
	public static Organization organization;
	public static Project project;
	
	public static ObjectMapper mapper = new ObjectMapper();
	
	public static void toast(String message, Context context){
		
		int duration = Toast.LENGTH_SHORT;

		Toast toast = Toast.makeText(context, message, duration);
		toast.show();
		
	}
}
