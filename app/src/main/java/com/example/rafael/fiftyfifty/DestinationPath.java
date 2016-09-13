package com.example.rafael.fiftyfifty;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by pogi on 9/9/2016.
 */
public class DestinationPath extends AppCompatActivity {
    Button setup;
    EditText from;
    TextView latLongTV, latTV;
    Double lat , Long;
    int locationCount = 0;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drivingroute);
        latLongTV = (TextView) findViewById(R.id.latLongTV);
        latTV = (TextView) findViewById(R.id.latTV);
        setup = (Button) findViewById(R.id.btnsetup);
        setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                from = (EditText) findViewById(R.id.editfrom);
                String locationfrom = from.getText().toString();
                String latfrom = latLongTV.getText().toString();
                String Longfrom = latTV.getText().toString();
                GeocodingLocation locationAddress = new GeocodingLocation();
                locationAddress.getAddressFromLocation(locationfrom, getApplicationContext(), new GeocoderHandler());
                lat = 0.0;
                Long = 0.0;
                try {
                    lat = Double.valueOf(latfrom);
                    Long = Double.valueOf(Longfrom);
                    locationCount++;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(LocationsDB.FIELD_LAT, lat); // Setting latitude in ContentValues
                    contentValues.put(LocationsDB.FIELD_LNG, Long); // Setting longitude in ContentValues
                    LocationInsertTask insertTask = new LocationInsertTask(); // Creating an instance of LocationInsertTask
                    insertTask.execute(contentValues); // Storing the latitude, longitude and zoom level to SQLite database
                    Intent intent = new Intent(getApplicationContext(),MapsActivity.class);
                    intent.putExtra("lat", lat);
                    intent.putExtra("long", Long);
                    startActivity(intent);
                }catch (NumberFormatException e){
                    e.printStackTrace();
                }
            }
        });
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "DestinationPath Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.rafael.fiftyfifty/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "DestinationPath Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.rafael.fiftyfifty/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            String locationaddress1;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    locationaddress1 = bundle.getString("address1");
                    break;
                default:
                    locationAddress = null;
                    locationaddress1 = null;
            }
            latLongTV.setText(locationAddress);
            latTV.setText(locationaddress1);
        }
    }

    private class LocationInsertTask extends AsyncTask<ContentValues, Void, Void> {
        @Override
        protected Void doInBackground(ContentValues... contentValues) {
            getContentResolver().insert(LocationsContentProvider.CONTENT_URI, contentValues[0]); /** Setting up values to insert the clicked location into SQLite database */
            return null;
        }
    }
}
