package com.example.rafael.fiftyfifty;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    SharedPreferences sharedPreferences;
    int locationCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        EditText searchLocation;

        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            LatLng current_location = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(current_location).title("Here you are"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mMap.setMyLocationEnabled(true);
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, 0));
            mMap.animateCamera(CameraUpdateFactory.zoomIn()); // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(50), 2000, null);  // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            CameraPosition cameraPosition = new CameraPosition.Builder() // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                .target(current_location)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//            mMap.setMinZoomPreference(6.0f);
//            mMap.setMaxZoomPreference(14.0f);
//            LatLngBounds AUSTRALIA = new LatLngBounds(
//                    new LatLng(-44, 113), new LatLng(-10, 154));
//
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(AUSTRALIA, 0));
        }
        searchLocation = (EditText)findViewById(R.id.Search);
        searchLocation.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                LatLng current_location = new LatLng(latitude, longitude);
                mMap.addMarker(new MarkerOptions().position(current_location).title("Here you are"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
            }
        });
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationCount++;
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                alertDialog.setTitle("Place your mark here");
                final EditText input = new EditText(MapsActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                alertDialog.setView(input);
                alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        Toast.makeText(getApplicationContext(),"Mark Placed", Toast.LENGTH_SHORT).show();
                    }
                });
                alertDialog.setNegativeButton("NO",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // Write your code here to execute after dialog
                                dialog.cancel();
                            }
                        });
                alertDialog.show();
                drawMarker(latLng); // Drawing marker on the map
                ContentValues contentValues = new ContentValues(); // Creating an instance of ContentValues
                contentValues.put(LocationsDB.FIELD_LAT, latLng.latitude ); // Setting latitude in ContentValues
                contentValues.put(LocationsDB.FIELD_LNG, latLng.longitude); // Setting longitude in ContentValues
                contentValues.put(LocationsDB.FIELD_ZOOM, mMap.getCameraPosition().zoom); // Setting zoom in ContentValues
                LocationInsertTask insertTask = new LocationInsertTask(); // Creating an instance of LocationInsertTask
                insertTask.execute(contentValues); // Storing the latitude, longitude and zoom level to SQLite database
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("lat"+ Integer.toString((locationCount-1)), Double.toString(latLng.latitude)); // Storing the latitude for the i-th location
                editor.putString("lng"+ Integer.toString((locationCount-1)), Double.toString(latLng.longitude));  // Storing the longitude for the i-th location
                editor.putInt("locationCount", locationCount); // Storing the count of locations or marker count
                editor.putString("zoom", Float.toString(mMap.getCameraPosition().zoom)); /** Storing the zoom level to the shared preferences */
                editor.commit(); /** Saving the values stored in the shared preferences */
            }
        });
        sharedPreferences = getSharedPreferences("location", 0);
        locationCount = sharedPreferences.getInt("locationCount", 0); // Getting number of locations already stored
        String zoom = sharedPreferences.getString("zoom", "0"); // Getting stored zoom level if exists else return 0
        if(locationCount!=0){ // If locations are already saved
            String lat = "";
            String lng = "";
            for(int i=0;i<locationCount;i++){ // Iterating through all the locations stored
                lat = sharedPreferences.getString("lat"+i,"0"); // Getting the latitude of the i-th location
                lng = sharedPreferences.getString("lng"+i,"0"); // Getting the longitude of the i-th location
                drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))); // Drawing marker on the map
            }
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng)))); // Moving CameraPosition to last clicked position
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(Float.parseFloat(zoom))); // Setting the zoom level in the map on last position  is clicked
        }
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                mMap.clear(); // Removing the marker and circle from the Google Map
                SharedPreferences.Editor editor = sharedPreferences.edit(); // Opening the editor object to delete data from sharedPreferences
                editor.clear(); // Clearing the editor
                editor.commit(); // Committing the changes
                locationCount=0; // Setting locationCount to zero
            }
        });
    }

    private void drawMarker(LatLng point){
        MarkerOptions markerOptions = new MarkerOptions(); // Creating an instance of MarkerOptions
        markerOptions.position(point); // Setting latitude and longitude for the marker
        mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))); // Adding marker on the Google Map
    }

    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void> {
        @Override
        protected Void doInBackground(ContentValues... contentValues) {
            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]); /** Setting up values to insert the clicked location into SQLite database */
            return null;
        }
    }

    private class LocationDeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(LocationsContentProvider.CONTENT_URI, null, null);  /** Deleting all the locations stored in SQLite database */
            return null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = LocationsContentProvider.CONTENT_URI; // Uri to the content provider LocationsContentProvider
        return new CursorLoader(this, uri, null, null, null, null); // Fetches all the rows from locations table
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor arg1) {
        int locationCount = 0;
        double lat = 0;
        double lng = 0;
        float zoom = 0;
        locationCount = arg1.getCount(); // Number of locations available in the SQLite database table
        arg1.moveToFirst(); // Move the current record pointer to the first row of the table
        for (int i = 0; i < locationCount; i++) {
            lat = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LAT)); // Get the latitude
            lng = arg1.getDouble(arg1.getColumnIndex(LocationsDB.FIELD_LNG)); // Get the longitude
            zoom = arg1.getFloat(arg1.getColumnIndex(LocationsDB.FIELD_ZOOM)); // Get the zoom level
            LatLng location = new LatLng(lat, lng); // Creating an instance of LatLng to plot the location in Google Maps
            drawMarker(location); // Drawing the marker in the Google Maps
            arg1.moveToNext(); // Traverse the pointer to the next row
        }if(locationCount>0){
            mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lat,lng))); // Moving CameraPosition to last clicked position
            mMap.animateCamera(CameraUpdateFactory.zoomTo(zoom)); // Setting the zoom level in the map on last position  is clicked
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
