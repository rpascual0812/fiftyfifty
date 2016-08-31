package com.example.rafael.fiftyfifty;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
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
        if(gps.canGetLocation()){
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            LatLng current_location = new LatLng(latitude, longitude);

            mMap.addMarker(new MarkerOptions().position(current_location).title("Here you are"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current_location));
            mMap.setMyLocationEnabled(true);

            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current_location, 0));

            // Zoom in, animating the camera.
            mMap.animateCamera(CameraUpdateFactory.zoomIn());

            // Zoom out to zoom level 10, animating with a duration of 2 seconds.
            mMap.animateCamera(CameraUpdateFactory.zoomTo(50), 2000, null);


            // Construct a CameraPosition focusing on Mountain View and animate the camera to that position.
            CameraPosition cameraPosition = new CameraPosition.Builder()
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


    }
}
