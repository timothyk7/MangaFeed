package com.manga.feed.browser;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.manga.feed.MLog;

public class mangaSQLiteHelper extends SQLiteOpenHelper {

	
	//flags that will be altered by query
    public static String TABLE_INFO = "MANGAPANDA"; //should be changed to represent which query
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_TITLE = "title";
	public static final String COLUMN_STATUS = "status";
	public static final String COLUMN_SITE = "site";

	private static final int DATABASE_VERSION = 1;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table "
	      + TABLE_INFO + "("+ COLUMN_ID +" , " + COLUMN_TITLE
	      + " , " + COLUMN_STATUS + " , " + COLUMN_SITE
	      + " );";
	
	
	public mangaSQLiteHelper(Context context, String name) {
		super(context, name+".db", null, DATABASE_VERSION);
		// TODO Auto-generated constructor stub
        TABLE_INFO = name;
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		// TODO Auto-generated method stub
		database.execSQL(DATABASE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	    MLog.i(mangaSQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
	    db.execSQL("DROP TABLE IF EXISTS " + TABLE_INFO);
	    onCreate(db);
	}

}
