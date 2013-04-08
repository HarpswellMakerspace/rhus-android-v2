package org.calflora.observer.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteOpenObservationsHelper extends SQLiteOpenHelper {
	
	private static final String SCRIPT_CREATE_DATABASE =
			"create table " + "observations" + " ("
			+ "id" + " integer primary key autoincrement, "
			+ "json_db_key" + " text not null"
			+ "uploaded" + " boolean);" ;
	
	public SQLiteOpenObservationsHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		 db.execSQL(SCRIPT_CREATE_DATABASE);		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * 
	 * ADAPTER CODE 
	private static final String DATABASE_NAME = "observations";
	private static final int DATABASE_VERSION = 1;

	public SQLiteAdapter openToRead() throws android.database.SQLException {
		sqLiteHelper = new SQLiteHelper(context, MYDATABASE_NAME, null, MYDATABASE_VERSION);
		sqLiteDatabase = sqLiteHelper.getReadableDatabase();
		return this;
	}
	*/

}
