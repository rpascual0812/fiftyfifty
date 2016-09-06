package com.example.rafael.fiftyfifty;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by pogi on 9/6/2016.
 */
public class PassengerLocationsContentProvider extends ContentProvider {
    public static final String PROVIDER_NAME = "com.example.rafael.fiftyfifty";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/passengers" ); /** A uri to do operations on locations table. A content provider is identified by its uri */
    private static final int LOCATIONS2 = 1;/** Constant to identify the requested operation */
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "passengers", LOCATIONS2);
    }
    PassLocationsDB passLocationsDB; /** This content provider does the database operations by this object */

    @Override
    public boolean onCreate() {
        passLocationsDB = new PassLocationsDB(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(uriMatcher.match(uri)==LOCATIONS2){
            return passLocationsDB.getAllLocations();
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowID = passLocationsDB.insert(values);
        Uri _uri=null;
        if(rowID>0){
            _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
        }else {
            try {
                throw new SQLException("Failed to insert : " + uri);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return _uri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int cnt = 0;
        cnt = passLocationsDB.del();
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
