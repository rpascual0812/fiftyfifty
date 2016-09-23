package com.example.rafael.fiftyfifty;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * Created by pogi on 9/6/2016.
 */
public class PassengerActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.InfoWindowAdapter {
    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    AutoCompleteTextView atvPlaces2;
    SharedPreferences sharedPreferences;
    SharedPreferences sharedPreferences2;
    int locationCount2 = 0, locationCount = 0;
    final Context context = this;
    Circle circle;
    DownloadTask placesDownloadTasker;
    DownloadTask placeDetailsDownloadTasker;
    ParserTask placesParserTasker;
    ParserTask placeDetailsParserTasker;
    final int PLACES=0;
    final int PLACES_DETAILS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.passenger);
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map2); // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment.getMapAsync(this);
        atvPlaces2 = (AutoCompleteTextView) findViewById(R.id.Search2);
        atvPlaces2.setThreshold(1);
        atvPlaces2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTasker = new DownloadTask(PLACES); // Creating a DownloadTask to download Google Places matching "s"
                String url = getAutoCompleteUrl(s.toString()); // Getting url to the Google Places Autocomplete api
                placesDownloadTasker.execute(url); // Start downloading Google Place This causes to execute doInBackground() of DownloadTask class
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Setting an item click listener for the AutoCompleteTextView dropdown list
        atvPlaces2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {
                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                placeDetailsDownloadTasker = new DownloadTask(PLACES_DETAILS); // Creating a DownloadTask to download Places details of the selected place
                String url = getPlaceDetailsUrl(hm.get("reference")); // Getting url to the Google Places details api
                placeDetailsDownloadTasker.execute(url); // Start downloading Google Place Details This causes to execute doInBackground() of DownloadTask class
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setBuildingsEnabled(false);
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            LatLng current_location = new LatLng(latitude, longitude);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            circle = mMap.addCircle(new CircleOptions()
                    .center(current_location)
                    .radius(100)
                    .strokeColor(Color.RED)
                    .fillColor(Color.BLUE)
                    .visible(false));
            mMap.animateCamera(CameraUpdateFactory.zoomIn()); // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(50), 2000, null);  // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            CameraPosition cameraPosition = new CameraPosition.Builder() // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                    .target(current_location)      // Sets the center of the map to Mountain View
                    .zoom(17)                   // Sets the zoom
                    .bearing(90)                // Sets the orientation of the camera to east
                    .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(PassengerActivity.this);
                alertdialog.setTitle("Are you sure to place a marker here?");
                alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        locationCount2++;
                        drawMarker(latLng);
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(PassLocationsDB.FIELD_LAT, latLng.latitude); // Setting latitude in ContentValues
                        contentValues.put(PassLocationsDB.FIELD_LNG, latLng.longitude); // Setting longitude in ContentValues
                        contentValues.put(PassLocationsDB.FIELD_ZOOM, mMap.getCameraPosition().zoom); // Setting zoom in ContentValues
                        LocationInsertTask insertTask = new LocationInsertTask(); // Creating an instance of LocationInsertTask
                        insertTask.execute(contentValues); // Storing the latitude, longitude and zoom level to SQLite database
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("lat" + Integer.toString((locationCount2 - 1)), Double.toString(latLng.latitude)); // Storing the latitude for the i-th location
                        editor.putString("lng" + Integer.toString((locationCount2 - 1)), Double.toString(latLng.longitude));  // Storing the longitude for the i-th location
                        editor.putInt("locationCount2", locationCount2); // Storing the count of locations or marker count
                        editor.putString("zoom2", Float.toString(mMap.getCameraPosition().zoom));
                        editor.commit();
                    }
                });
                alertdialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertdialog.show();
            }
        });
        sharedPreferences = getSharedPreferences("passengers", 0);
        locationCount2 = sharedPreferences.getInt("locationCount2", 0); // Getting number of locations already stored
        String zoom = sharedPreferences.getString("zoom2", "0"); // Getting stored zoom level if exists else return 0
        if (locationCount2 != 0) { // If locations are already saved
            String lat = "";
            String lng = "";
            for (int i = 0; i < locationCount2; i++) { // Iterating through all the locations stored
                lat = sharedPreferences.getString("lat" + i, "0"); // Getting the latitude of the i-th location
                lng = sharedPreferences.getString("lng" + i, "0"); // Getting the longitude of the i-th location
                drawMarker(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))); // Drawing marker on the map
                LatLng pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            }
        }

        sharedPreferences2 = getSharedPreferences("location", 0);
        locationCount = sharedPreferences2.getInt("locationCount", 0); // Getting number of locations already stored
        String zoom2 = sharedPreferences2.getString("zoom", "0"); // Getting stored zoom level if exists else return 0
        if (locationCount != 0) { // If locations are already saved
            String lat = "";
            String lng = "";
            for (int i = 0; i < locationCount; i++) { // Iterating through all the locations stored
                lat = sharedPreferences2.getString("lat" + i, "0"); // Getting the latitude of the i-th location
                lng = sharedPreferences2.getString("lng" + i, "0"); // Getting the longitude of the i-th location
                drawMarker2(new LatLng(Double.parseDouble(lat), Double.parseDouble(lng))); // Drawing marker on the map
                LatLng pos = new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
            }
        }

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker arg0) {
                View v= getLayoutInflater().inflate(R.layout.infoshow,null);
                return v;
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.markershow);
                dialog.show();
                marker.hideInfoWindow();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(PassengerActivity.this);
                alertDialog.setTitle("You want to delete all markers?");
                alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mMap.clear(); // Removing the marker and circle from the Google Map
                        SharedPreferences.Editor editor = sharedPreferences.edit(); // Opening the editor object to delete data from sharedPreferences
                        editor.clear(); // Clearing the editor
                        editor.commit(); // Committing the changes
                        locationCount2=0; // Setting locationCount to zero
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

    private void drawMarker2(LatLng point){
        MarkerOptions markerOptions = new MarkerOptions(); // Creating an instance of MarkerOptions
        markerOptions.position(point); // Setting latitude and longitude for the marker
        Marker marker2 = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.car))/*.visible(false)*/); // Adding marker on the Google Map
        /*float[] distance = new float[2];
        Location.distanceBetween( marker2.getPosition().latitude, marker2.getPosition().longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);
        if( distance[0] < circle.getRadius()){
            marker2.setVisible(true);
        }else {
        }*/
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
            getContentResolver().insert(PassengerLocationsContentProvider.CONTENT_URI, contentValues[0]); /** Setting up values to insert the clicked location into SQLite database */
            return null;
        }
    }

    private class LocationDeleteTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(PassengerLocationsContentProvider.CONTENT_URI, null, null);  /** Deleting all the locations stored in SQLite database */
            return null;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = PassengerLocationsContentProvider.CONTENT_URI; // Uri to the content provider LocationsContentProvider
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
            lat = arg1.getDouble(arg1.getColumnIndex(PassLocationsDB.FIELD_LAT)); // Get the latitude
            lng = arg1.getDouble(arg1.getColumnIndex(PassLocationsDB.FIELD_LNG)); // Get the longitude
            zoom = arg1.getFloat(arg1.getColumnIndex(PassLocationsDB.FIELD_ZOOM)); // Get the zoom level
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

    private String getAutoCompleteUrl(String place){
        String key = "key=AIzaSyDnAm9rfFlNauZdySrtQB_1bsrA4el6yRc";  // Obtain browser key from https://code.google.com/apis/console
        String input = "input="+place; // place to be be searched
        String types = "types=geocode"; // place type to be searched
        String sensor = "sensor=false"; // Sensor enabled
        String parameters = input+"&"+types+"&"+sensor+"&"+key; // Building the parameters to the web service
        String output = "json"; // Output format
        String url = "https://maps.googleapis.com/maps/api/place/autocomplete/"+output+"?"+parameters; // Building the url to the web service
        return url;
    }

    private String getPlaceDetailsUrl(String ref){
        String key = "key=AIzaSyDnAm9rfFlNauZdySrtQB_1bsrA4el6yRc"; // Obtain browser key from https://code.google.com/apis/console
        String reference = "reference="+ref; // reference of place
        String sensor = "sensor=false"; // Sensor enabled
        String parameters = reference+"&"+sensor+"&"+key; // Building the parameters to the web service
        String output = "json"; // Output format
        String url = "https://maps.googleapis.com/maps/api/place/details/"+output+"?"+parameters; // Building the url to the web service
        return url;
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        private int downloadType = 0;
        public DownloadTask(int type) {this.downloadType = type;} // Constructor
        @Override
        protected String doInBackground(String... url) {
            String data = ""; // For storing data from web service
            try {
                data = downloadUrl(url[0]); // Fetching the data from web service
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            switch(downloadType){
                case PLACES:
                    placesParserTasker = new ParserTask(PLACES); // Creating ParserTask for parsing Google Places
                    placesParserTasker.execute(result); // Start parsing google places json data This causes to execute doInBackground() of ParserTask class
                    break;

                case PLACES_DETAILS :
                    placeDetailsParserTasker = new ParserTask(PLACES_DETAILS); // Creating ParserTask for parsing Google Places
                    placeDetailsParserTasker.execute(result); // Starting Parsing the JSON string This causes to execute doInBackground() of ParserTask class
            }
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String,String>>> {
        int parserType = 0;
        public ParserTask(int type){this.parserType = type;}

        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<HashMap<String, String>> list = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                switch(parserType){
                    case PLACES :
                        PlaceJSONParser placeJsonParser = new PlaceJSONParser();
                        list = placeJsonParser.parse(jObject); // Getting the parsed data as a List construct
                        break;
                    case PLACES_DETAILS :
                        PlaceDetailJSONParser placeDetailsJsonParser = new PlaceDetailJSONParser();
                        list = placeDetailsJsonParser.parse(jObject); // Getting the parsed data as a List construct
                }
            }catch(Exception e){
                Log.d("Exception",e.toString());
            }
            return list;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> result) {
            switch(parserType){
                case PLACES :
                    String[] from = new String[] { "description"};
                    int[] to = new int[] { android.R.id.text1 };
                    SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), result, android.R.layout.simple_list_item_1, from, to);// Creating a SimpleAdapter for the AutoCompleteTextView
                    atvPlaces2.setAdapter(adapter); // Setting the adapter
                    break;
                case PLACES_DETAILS :
                    HashMap<String, String> hm = result.get(0);
                    latitude = Double.parseDouble(hm.get("lat")); // Getting latitude from the parsed data
                    longitude = Double.parseDouble(hm.get("lng")); // Getting longitude from the parsed data
                    LatLng point = new LatLng(latitude, longitude);
                    drawMarker(point);
                    /*CameraUpdate cameraPosition = CameraUpdateFactory.newLatLng(point);
                    CameraUpdate cameraZoom = CameraUpdateFactory.zoomBy(5);
                    mMap.moveCamera(cameraPosition);// Showing the user input location in the Google Map
                    mMap.animateCamera(cameraZoom);
                    MarkerOptions options = new MarkerOptions();
                    options.position(point);
                    options.title("Position");
                    options.snippet("Latitude:"+latitude+",Longitude:"+longitude);
                    mMap.addMarker(options); // Adding the marker in the Google Map*/
                    break;
            }
        }
    }

    /** A method to download json data from url */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try{
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();  // Creating an http connection to communicate with url
            urlConnection.connect(); // Connecting to url
            iStream = urlConnection.getInputStream(); // Reading data from url
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb  = new StringBuffer();
            String line = "";
            while( ( line = br.readLine())  != null){
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        }catch(Exception e){
            Log.d("Exception while downloading url", e.toString());
        }finally{
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }
}
