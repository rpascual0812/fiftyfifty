package com.example.rafael.fiftyfifty;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static com.example.rafael.fiftyfifty.R.string.longitude;

/**
 * Created by pogi on 9/20/2016.
 */
public class RouteActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks{
    private static final String LOG_TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private AutoCompleteTextView goingfrom, goingto;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));
    Button goMap, saveRoute;
    private TextView mAddressTextView, latLongTV, LongTV;
    Double latitude, Longitude;
    private String latitud, longitud;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination);
        latLongTV = (TextView) findViewById(R.id.latLongTV);
        LongTV = (TextView) findViewById(R.id.LongTV);
        mGoogleApiClient = new GoogleApiClient.Builder(RouteActivity.this).addApi(Places.GEO_DATA_API).enableAutoManage(this, GOOGLE_API_CLIENT_ID, this).addConnectionCallbacks(this).build();
        mAddressTextView = (TextView) findViewById(R.id.address);
        goingfrom = (AutoCompleteTextView) findViewById(R.id.editfrom);
        final Intent nintent = getIntent();
        latitude = nintent.getDoubleExtra("latitude", 0);
        Longitude = nintent.getDoubleExtra("longitude", 0);
        Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        List<Address> addresses  = null;
        try {
            addresses = geocoder.getFromLocation(latitude,Longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        goingfrom.setText(city+","+state+","+country);
        goingto = (AutoCompleteTextView) findViewById(R.id.editto);
        goingto.setThreshold(3);
        goingto.setOnItemClickListener(AutocompleteClickListener);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
        goingfrom.setAdapter(mPlaceArrayAdapter);
        goingto.setAdapter(mPlaceArrayAdapter);
        saveRoute = (Button) findViewById(R.id.btnRoute);
        saveRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("latitude", latitud);
                intent.putExtra("longitude", longitud);
                intent.putExtra("Count", 1);
                startActivity(intent);
            }
        });
        goMap = (Button) findViewById(R.id.btnMap);
        goMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("Count", 1);
                startActivity(intent);
            }
        });
    }

    private AdapterView.OnItemClickListener AutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i(LOG_TAG, "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(UpdatePlaceDetailsCallback);
            Log.i(LOG_TAG, "Fetching details for ID: " + item.placeId);
        }
    };

    private ResultCallback<PlaceBuffer> UpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e(LOG_TAG, "Place query did not complete. Error: " + places.getStatus().toString());
                return;
            }
            final Place place = places.get(0); // Selecting the first object buffer.
            CharSequence attributions = places.getAttributions();
            mAddressTextView.setText(Html.fromHtml(place.getAddress() + ""));
            String address = mAddressTextView.getText().toString();
            GeocodingLocation locationAddress = new GeocodingLocation();
            locationAddress.getAddressFromLocation(address, getApplicationContext(), new GeocoderHandler());
        }
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        Log.i(LOG_TAG, "Google Places API connected.");
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        Log.e(LOG_TAG, "Google Places API connection suspended.");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(LOG_TAG, "Google Places API connection failed with error code: " + connectionResult.getErrorCode());

        Toast.makeText(this, "Google Places API connection failed with error code:" + connectionResult.getErrorCode(), Toast.LENGTH_LONG).show();
    }

    private class GeocoderHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            String locationAddress;
            String locationAddress1;
            switch (message.what) {
                case 1:
                    Bundle bundle = message.getData();
                    locationAddress = bundle.getString("address");
                    locationAddress1 = bundle.getString("address1");
                    break;
                default:
                    locationAddress = null;
                    locationAddress1 = null;
            }
            latLongTV.setText(locationAddress);
            LongTV.setText(locationAddress1);
            latitud = latLongTV.getText().toString();
            longitud = LongTV.getText().toString();
        }
    }
}
