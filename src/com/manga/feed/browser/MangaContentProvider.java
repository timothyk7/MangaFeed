package com.manga.feed.browser;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Created by Timothy on 7/22/2014.
 */
public class MangaContentProvider extends ContentProvider {
    public static final String AUTHORITY = "com.manga.feed.browser.MangaContentProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/mangas" );

    private static final int SUGGESTIONS_MANGA = 1;
    private static final int GET_MANGA = 2;

    private mangaDatabase mangaSearchDB = null;

    UriMatcher mUriMatcher = buildUriMatcher();

    private UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Suggestion items of Search Dialog is provided by this uri
        uriMatcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY,SUGGESTIONS_MANGA);


        // This URI is invoked, when user selects a suggestion from search dialog or an item from the listview
        // Country details for CountryActivity is provided by this uri
        // See, SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID in CountryDB.java
        uriMatcher.addURI(AUTHORITY, "mangas/#", GET_MANGA);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mangaSearchDB = new mangaDatabase(getContext(), Browser.key);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor c = null;
        switch(mUriMatcher.match(uri)){
            case SUGGESTIONS_MANGA :
                c = mangaSearchDB.getMangas(selectionArgs);
                break;
            case GET_MANGA :
                String id = uri.getLastPathSegment();
                c = mangaSearchDB.getManga(id);
        }

        return c;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }


    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
