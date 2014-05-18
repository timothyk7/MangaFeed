package com.manga.feed.browser;

import java.util.*;

import com.manga.feed.MangaInfoHolder;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class mangaDatabase {
	
	// Database fields
	  private SQLiteDatabase database;
	  private mangaSQLiteHelper dbHelper;
	  private String name; //table for database
	  private String[] allColumns = { mangaSQLiteHelper.COLUMN_ID, mangaSQLiteHelper.COLUMN_TITLE,
			  mangaSQLiteHelper.COLUMN_STATUS, mangaSQLiteHelper.COLUMN_SITE };
	  
	  public mangaDatabase(Context context, String name) {
		    dbHelper = new mangaSQLiteHelper(context,name);
		    this.name = name;
	  }

	  public void open() throws SQLException {
	    database = dbHelper.getWritableDatabase();
	  }

	  public void close() {
	    dbHelper.close();
	  }
	  
	  /*
	   * Create manga that will go into the database
	   */
	  public void createManga(String title, String status, String site) {
	    ContentValues values = new ContentValues(); //put values to be stored in query
	    values.put(mangaSQLiteHelper.COLUMN_TITLE, title);
	    values.put(mangaSQLiteHelper.COLUMN_STATUS, status);
	    values.put(mangaSQLiteHelper.COLUMN_SITE, site);
	    long insertId = database.insert(name, null,
	        values);
	    database.query(name,
	        allColumns, mangaSQLiteHelper.COLUMN_ID + " = " + insertId, null,
	        null, null, null);
	  }
	  
	  /*
	   * Helper that will create the manga and assign an unique id
	   */
	  private MangaInfoHolder cursorToManga(Cursor cursor) {
		    MangaInfoHolder manga = new MangaInfoHolder();
		    //initialize data in the manga 
		    manga.setId(cursor.getLong(0));
		    manga.setTitle(cursor.getString(1));
		    manga.setStatus(cursor.getString(2));
		    manga.setSite(cursor.getString(3));
		    return manga;
	  }
	  
	  /*
	   * Delete the manga by using unique id
	   */
	  public void deleteManga(MangaInfoHolder manga) {
		    long id = manga.getId();
		    database.delete(name, mangaSQLiteHelper.COLUMN_ID+ " = " + id, null);
	 }
	  
	  public ArrayList<MangaInfoHolder> getAllMangas() {
		    ArrayList<MangaInfoHolder> mangas = new ArrayList<MangaInfoHolder>();

		    Cursor cursor = database.query(name,
		        allColumns, null, null, null, null, null);

		    cursor.moveToFirst();
		    while (!cursor.isAfterLast()) {
		      MangaInfoHolder manga = cursorToManga(cursor);
		      mangas.add(manga);
		      cursor.moveToNext();
		    }
		    // make sure to close the cursor
		    cursor.close();
		    Collections.sort(mangas);
		    return mangas;
	}
	  

}
