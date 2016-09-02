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
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.InfoWindowAdapter {

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
        mMap.setBuildingsEnabled(false);
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            LatLng current_location = new LatLng(latitude, longitude);
            //mMap.addMarker(new MarkerOptions().position(current_location).title("Here you are"));
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
        searchLocation = (EditText) findViewById(R.id.Search);
        searchLocation.addTextChangedListener(new TextWatcher() {
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
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(MapsActivity.this);
                alertdialog.setTitle("Are you sure to place a marker here?");
                alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       locationCount++;
                       /* AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                        alertDialog.setTitle("Set a message for marker");
                        final EditText input = new EditText(MapsActivity.this);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
                        input.setLayoutParams(lp);
                        alertDialog.setView(input);
                        alertDialog.setPositiveButton("SET MESSAGE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"Mark Placed",Toast.LENGTH_SHORT).show();
                            }
                        });
                        alertDialog.setNegativeButton("LEAVE IT BLANK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        alertDialog.show();*/
                        drawMarker(latLng);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(LocationsDB.FIELD_LAT, latLng.latitude); // Setting latitude in ContentValues
                        contentValues.put(LocationsDB.FIELD_LNG, latLng.longitude); // Setting longitude in ContentValues
                        contentValues.put(LocationsDB.FIELD_ZOOM, mMap.getCameraPosition().zoom); // Setting zoom in ContentValues
                        LocationInsertTask insertTask = new LocationInsertTask(); // Creating an instance of LocationInsertTask
                        insertTask.execute(contentValues); // Storing the latitude, longitude and zoom level to SQLite database
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lat" + Integer.toString((locationCount - 1)), Double.toString(latLng.latitude)); // Storing the latitude for the i-th location
                        editor.putString("lng" + Integer.toString((locationCount - 1)), Double.toString(latLng.longitude));  // Storing the longitude for the i-th location
                        editor.putInt("locationCount", locationCount); // Storing the count of locations or marker count
                        editor.putString("zoom", Float.toString(mMap.getCameraPosition().zoom));
                        editor.commit();
                    }
                });
                sharedPreferences = getSharedPreferences("location", 0);
                locationCount = sharedPreferences.getInt("locationCount", 0); // Getting number of locations already stored
                String zoom = sharedPreferences.getString("zoom", "0"); // Getting stored zoom level if exists else return 0
                if (locationCount != 0) { // If locations are already saved
                    String lat = "";
                    String lng = "";
                    for (int i = 0; i < locationCount; i++) { // Iterating through all the locations stored
                        lat = sharedPreferences.getString("lat" + i, "0"); // Getting the latitude of the i-th location
                        lng = sharedPreferences.getString("lng" + i, "0"); // Getting the longitude of the i-th location
                        drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))); // Drawing marker on the map
                        LatLng pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
                        CameraPosition cameraPosition = new CameraPosition.Builder() // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                                .target(pos)      // Sets the center of the map to Mountain View
                                .zoom(17)                   // Sets the zoom
                                .bearing(90)                // Sets the orientation of the camera to east
                                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                                .build();                   // Creates a CameraPosition from the builder
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    }
                }
                alertdialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertdialog.show();
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                View v= getLayoutInflater().inflate(R.layout.markershow,null);
                return v;
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MapsActivity.this);
                alertDialog.setTitle("You want to delete all markers?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.clear(); // Removing the marker and circle from the Google Map
                        SharedPreferences.Editor editor = sharedPreferences.edit(); // Opening the editor object to delete data from sharedPreferences
                        editor.clear(); // Clearing the editor
                        editor.commit(); // Committing the changes
                        locationCount=0; // Setting locationCount to zero
                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
    }


    private void drawMarker(LatLng point){
        MarkerOptions markerOptions = new MarkerOptions(); // Creating an instance of MarkerOptions
        markerOptions.position(point); // Setting latitude and longitude for the marker
        Marker marker = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.male))); // Adding marker on the Google Map
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
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
