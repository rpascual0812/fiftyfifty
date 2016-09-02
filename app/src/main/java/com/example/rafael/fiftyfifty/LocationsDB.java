package com.example.rafael.fiftyfifty;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by pogi on 9/1/2016.
 */
public class LocationsDB extends SQLiteOpenHelper {

    private static String DBNAME = "locationmarkersqlite"; /** Database name */
    private static int VERSION = 1;/** Version number of the database */
    public static final String FIELD_ROW_ID = "_id"; /** Field 1 of the table locations, which is the primary key */
    public static final String FIELD_LAT = "lat";/** Field 2 of the table locations, stores the latitude */
    public static final String FIELD_LNG = "lng";/** Field 3 of the table locations, stores the longitude*/
    public static final String FIELD_ZOOM = "zom";/** Field 4 of the table locations, stores the zoom level of map*/
    private static final String DATABASE_TABLE = "locations";/** A constant, stores the the table name */
    private SQLiteDatabase mDB;/** An instance variable for SQLiteDatabase */
    public LocationsDB(Context context) {
        super(context, DBNAME, null, VERSION);
        this.mDB = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql =     "create table " + DATABASE_TABLE + " ( " +
                FIELD_ROW_ID + " integer primary key autoincrement , " +
                FIELD_LNG + " double , " +
                FIELD_LAT + " double , " +
                FIELD_ZOOM + " text " +
                " ) ";
        db.execSQL(sql);
    }

    /** Inserts a new location to the table locations */
    public long insert(ContentValues contentValues){
        long rowID = mDB.insert(DATABASE_TABLE, null, contentValues);
        return rowID;
    }

    /** Deletes all locations from the table */
    public int del(){
        int cnt = mDB.delete(DATABASE_TABLE, null , null);
        return cnt;
    }

    /** Returns all the locations from the table */
    public Cursor getAllLocations(){
        return mDB.query(DATABASE_TABLE, new String[] { FIELD_ROW_ID, FIELD_LAT , FIELD_LNG, FIELD_ZOOM } , null, null, null, null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
