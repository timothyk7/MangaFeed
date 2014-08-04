package com.manga.feed.browser;

import java.util.*;

import android.database.sqlite.SQLiteQueryBuilder;
import com.manga.feed.MangaInfoHolder;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class mangaDatabase {
	
    // Database fields
    private ArrayList<MangaInfoHolder> mangas;
    private SQLiteDatabase database;
    private mangaSQLiteHelper dbHelper;
    private String name; //table for database
    private String[] allColumns = { mangaSQLiteHelper.COLUMN_ID, mangaSQLiteHelper.COLUMN_TITLE,
          mangaSQLiteHelper.COLUMN_STATUS, mangaSQLiteHelper.COLUMN_SITE };

    private static String TABLE_INFO = "MANGAPANDA"; //should be changed to represent which query

    private HashMap<String, String> mAliasMap;

    public mangaDatabase(Context context, String name) {
        Log.i("mangaDatabase", "constructor");
        dbHelper = new mangaSQLiteHelper(context,name);
        this.name = name;
        // This HashMap is used to map table fields to Custom Suggestion fields
        mAliasMap = new HashMap<String, String>();

        // Unique id for the each Suggestions ( Mandatory )
        mAliasMap.put("_ID", mangaSQLiteHelper.COLUMN_ID + " as " + "_id" );

        // Text for Suggestions ( Mandatory )
        mAliasMap.put(SearchManager.SUGGEST_COLUMN_TEXT_1, mangaSQLiteHelper.COLUMN_TITLE + " as " + SearchManager.SUGGEST_COLUMN_TEXT_1);

        // This value will be appended to the Intent data on selecting an item from Search result or Suggestions ( Optional )
        mAliasMap.put( SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID, mangaSQLiteHelper.COLUMN_ID + " as " + SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID );

        Log.i("mangaDB", mAliasMap.toString());
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
        Log.i("mangaDataBase", "created");
    ContentValues values = new ContentValues(); //put values to be stored in query
    values.put(mangaSQLiteHelper.COLUMN_TITLE, title);
    values.put(mangaSQLiteHelper.COLUMN_STATUS, status);
    values.put(mangaSQLiteHelper.COLUMN_SITE, site);
    database.insert(name, null, values);
/*    database.query(name,
        allColumns, mangaSQLiteHelper.COLUMN_ID + " = " + insertId, null,
        null, null, null);*/
    }

    public ArrayList<MangaInfoHolder> getAllMangas() {
        if (mangas != null)
            return mangas;
        else {
            mangas = new ArrayList<MangaInfoHolder>();

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

    /*
     * Helper that will create the manga and assign an unique id
     */
    public MangaInfoHolder cursorToManga(Cursor cursor) {
        MangaInfoHolder manga = new MangaInfoHolder();
        //initialize data in the manga
        manga.setId(cursor.getLong(0));
        manga.setTitle(cursor.getString(1));
        manga.setStatus(cursor.getString(2));
        manga.setSite(cursor.getString(3));
        return manga;
    }

    /** Returns Mangas  */
    public Cursor getMangas(String[] selectionArgs){
        Log.d("mangaDatabase getMangas", selectionArgs.toString());
        String selection = mangaSQLiteHelper.COLUMN_TITLE + " like ? ";

        if(selectionArgs!=null){
            selectionArgs[0] = "%"+selectionArgs[0] + "%";
        }

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setProjectionMap(mAliasMap);

        queryBuilder.setTables(mangaSQLiteHelper.TABLE_INFO);

        Cursor c = queryBuilder.query(dbHelper.getReadableDatabase(),
                new String[] { "_ID",
                        SearchManager.SUGGEST_COLUMN_TEXT_1 ,
                        SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID } ,
                selection,
                selectionArgs,
                null,
                null,
                mangaSQLiteHelper.COLUMN_TITLE + " asc ","10"
        );
        return c;

    }

    /** Return Manga corresponding to the id */
    public Cursor getManga(String id){
        Log.d("mangaDatabase getManga", id);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        queryBuilder.setTables(mangaSQLiteHelper.TABLE_INFO);

        Cursor c = queryBuilder.query(dbHelper.getReadableDatabase(),
                new String[] { "_id", "title", "status", "site" } ,
                "title = ?", new String[] { id } , null, null, null ,"1"
        );

        return c;
    }

}
