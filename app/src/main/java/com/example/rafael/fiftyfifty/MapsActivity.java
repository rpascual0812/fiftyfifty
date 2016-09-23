package com.example.rafael.fiftyfifty;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<Cursor>, GoogleMap.InfoWindowAdapter {

    private GoogleMap mMap;
    private double latitude , longitude;
    SharedPreferences sharedPreferences , sharedPreferences2;
    int locationCount = 0, locationCount2 = 0, IntentCount = 0;
    final Context context = this;
    Circle circle;
    ArrayList<LatLng> markerPoints;
    AutoCompleteTextView atvPlaces,fromPlaces, toPlaces;
    DownloadTasker placesDownloadTasker;
    DownloadTasker placeDetailsDownloadTasker;
    ParserTasker placesParserTasker;
    ParserTasker placeDetailsParserTasker;
    final int PLACES=0;
    final int PLACES_DETAILS=1;
    Double lat, Long, lat1, long1;
    TextView latitude2, longitude2;
    String latitude1, longitude1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        latitude2 = (TextView) findViewById(R.id.lat);
        longitude2 = (TextView)findViewById(R.id.Long1);
        Intent iintent = getIntent();
        latitude1 = iintent.getStringExtra("latitude");
        longitude1 = iintent.getStringExtra("longitude");
        latitude2.setText(latitude1);
        longitude2.setText(longitude1);
        // Getting Google Play availability status
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getBaseContext());
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode);
            dialog.show();
        } else { // Google Play Services are available
            markerPoints = new ArrayList<LatLng>(); // Initializing
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map); // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            mapFragment.getMapAsync(this);
        }
        atvPlaces = (AutoCompleteTextView) findViewById(R.id.Search);
        atvPlaces.setThreshold(1);
        atvPlaces.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                placesDownloadTasker = new DownloadTasker(PLACES); // Creating a DownloadTask to download Google Places matching "s"
                String url = getAutoCompleteUrl(s.toString()); // Getting url to the Google Places Autocomplete api
                placesDownloadTasker.execute(url); // Start downloading Google Place This causes to execute doInBackground() of DownloadTask class
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Setting an item click listener for the AutoCompleteTextView dropdown list
        atvPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int index, long id) {
                ListView lv = (ListView) arg0;
                SimpleAdapter adapter = (SimpleAdapter) arg0.getAdapter();
                HashMap<String, String> hm = (HashMap<String, String>) adapter.getItem(index);
                placeDetailsDownloadTasker = new DownloadTasker(PLACES_DETAILS); // Creating a DownloadTask to download Places details of the selected place
                String url = getPlaceDetailsUrl(hm.get("reference")); // Getting url to the Google Places details api
                placeDetailsDownloadTasker.execute(url); // Start downloading Google Place Details This causes to execute doInBackground() of DownloadTask class
            }
        });

        // Checks, whether start and end locations are captured
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
        mMap.setBuildingsEnabled(false);
        GPSTracker gps = new GPSTracker(this);
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            lat = latitude;
            Long = longitude;
            LatLng current_location = new LatLng(latitude, longitude);
            drawMarker(current_location);
            //mMap.addMarker(new MarkerOptions().position(current_location).title("Here you are").icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).visible(false));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        /*mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder alertdialog = new AlertDialog.Builder(MapsActivity.this);
                alertdialog.setTitle("Are you sure to place a marker here?");
                alertdialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       locationCount++;
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
                alertdialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertdialog.show();
            }
        });*/
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
            }
        }

        sharedPreferences2 = getSharedPreferences("passengers", 0);
        locationCount2 = sharedPreferences2.getInt("locationCount2", 0); // Getting number of locations already stored
        String zoom2 = sharedPreferences2.getString("zoom2", "0"); // Getting stored zoom level if exists else return 0
        if (locationCount2 != 0) { // If locations are already saved
            String lat = "";
            String lng = "";
            for (int i = 0; i < locationCount2; i++) { // Iterating through all the locations stored
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
                dialog.setContentView(R.layout.drivershow);
                dialog.show();
                marker.hideInfoWindow();
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

        Intent mintent = getIntent();
        IntentCount = mintent.getIntExtra("Count" , 0);
        if (IntentCount == 0){
            AlertDialog.Builder alertdialog = new AlertDialog.Builder(this);
            alertdialog.setTitle("Welcome to Carpool App");
            alertdialog.setMessage("This application is kemerut!");
            alertdialog.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getApplicationContext(), RouteActivity.class);
                    intent.putExtra("latitude", lat);
                    intent.putExtra("longitude", Long);
                    startActivity(intent);
                }
            });
            alertdialog.show();
        }

        try{
            String latitudefrom = latitude2.getText().toString();
            String longitudeto = longitude2.getText().toString();
            lat1 = Double.valueOf(latitudefrom.trim()).doubleValue();
            long1 = Double.valueOf(longitudeto.trim()).doubleValue();
            LatLng latLng = new LatLng(lat1,long1);
            drawMarker(latLng);
        }catch (NumberFormatException e){
            e.getStackTrace();
        }
    }

    private void drawMarker(LatLng point){
        // Already 10 locations with 8 waypoints and 1 start location and 1 end location.
        // Upto 8 waypoints are allowed in a query for non-business users
        if(markerPoints.size()>=10){
            return;
        }
        markerPoints.add(point); // Adding new item to the ArrayList
        MarkerOptions markerOptions = new MarkerOptions(); // Creating an instance of MarkerOptions
        markerOptions.position(point); // Setting latitude and longitude for the marker
        if(markerPoints.size()==1){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car)).visible(false);
        }else if(markerPoints.size()==2){
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
        }
        else {
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.car));
        }
        mMap.addMarker(markerOptions); // Add new marker to the Google Map Android API V2
        //Marker marker = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.car))); // Adding marker on the Google Map
        mMap.animateCamera(CameraUpdateFactory.zoomIn()); // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(50), 2000, null);  // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        CameraPosition cameraPosition = new CameraPosition.Builder() // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
                .target(point)      // Sets the center of the map to Mountain View
                .zoom(17)                   // Sets the zoom
                .bearing(90)                // Sets the orientation of the camera to east
                .tilt(30)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        if(markerPoints.size() >= 2){
            LatLng origin = markerPoints.get(0);
            LatLng dest = markerPoints.get(1);
            String url = getDirectionsUrl(origin, dest); // Getting URL to the Google Directions API
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url); // Start downloading json data from Google Directions API
        }
    }

    private void drawMarker2(LatLng point){
        MarkerOptions markerOptions = new MarkerOptions(); // Creating an instance of MarkerOptions
        markerOptions.position(point); // Setting latitude and longitude for the marker
        Marker marker2 = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.fromResource(R.drawable.male)).visible(false)); // Adding marker on the Google Map
        float[] distance = new float[2];
        Location.distanceBetween( marker2.getPosition().latitude, marker2.getPosition().longitude, circle.getCenter().latitude, circle.getCenter().longitude, distance);
        if( distance[0] < circle.getRadius()){
            marker2.setVisible(true);
        }else {
        }
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

    private String getDirectionsUrl(LatLng origin,LatLng dest){
        String str_origin = "origin="+origin.latitude+","+origin.longitude; // Origin of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude; // Destination of route
        String sensor = "sensor=false";  // Sensor enabled
        String parameters = str_origin+"&"+str_dest+"&"+sensor;  // Building the parameters to the web service
        String output = "json"; // Output format
        String url = "https://maps.googleapis.com/maps/api/directions/"+output+"?"+parameters; // Building the url to the web service
        return url;
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

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {
        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {
            String data = ""; // For storing data from web service
            try{
                data = downloadUrl(url[0]); // Fetching the data from web service
            }catch(Exception e){
                Log.d("Background Task",e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result); // Invokes the thread for parsing the JSON data
        }
    }

    /** A class to parse the Google Directions in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> >{

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try{
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject); // Starts parsing data
            }catch(Exception e){
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            for(int i=0;i<result.size();i++){
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);  // Fetching i-th route
                // Fetching all the points in i-th route
                for(int j=0;j<path.size();j++){
                    HashMap<String,String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                lineOptions.addAll(points); // Adding all the points in the route to LineOptions
                lineOptions.width(12);
                lineOptions.color(Color.RED);
            }
            mMap.addPolyline(lineOptions); // Drawing polyline in the Google Map for the i-th route
        }
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
    private class DownloadTasker extends AsyncTask<String, Void, String> {
        private int downloadType = 0;
        public DownloadTasker(int type) {this.downloadType = type;} // Constructor
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
                    placesParserTasker = new ParserTasker(PLACES); // Creating ParserTask for parsing Google Places
                    placesParserTasker.execute(result); // Start parsing google places json data This causes to execute doInBackground() of ParserTask class
                    break;

                case PLACES_DETAILS :
                    placeDetailsParserTasker = new ParserTasker(PLACES_DETAILS); // Creating ParserTask for parsing Google Places
                    placeDetailsParserTasker.execute(result); // Starting Parsing the JSON string This causes to execute doInBackground() of ParserTask class
            }
        }
    }

    /** A class to parse the Google Places in JSON format */
    private class ParserTasker extends AsyncTask<String, Integer, List<HashMap<String,String>>> {
        int parserType = 0;
        public ParserTasker(int type){this.parserType = type;}

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
                    atvPlaces.setAdapter(adapter); // Setting the adapter
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
}
