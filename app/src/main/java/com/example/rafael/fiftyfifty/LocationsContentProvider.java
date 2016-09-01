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
 * Created by pogi on 9/1/2016.
 */
public class LocationsContentProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "in.wptrafficanalyzer.locationmarkersqlite.locations";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/locations" ); /** A uri to do operations on locations table. A content provider is identified by its uri */
    private static final int LOCATIONS = 1;/** Constant to identify the requested operation */
    private static final UriMatcher uriMatcher ;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "locations", LOCATIONS);
    }
    LocationsDB mLocationsDB; /** This content provider does the database operations by this object */

    @Override
    public boolean onCreate() {
        mLocationsDB = new LocationsDB(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if(uriMatcher.match(uri)==LOCATIONS){
            return mLocationsDB.getAllLocations();
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
        long rowID = mLocationsDB.insert(values);
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
        cnt = mLocationsDB.del();
        return cnt;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
