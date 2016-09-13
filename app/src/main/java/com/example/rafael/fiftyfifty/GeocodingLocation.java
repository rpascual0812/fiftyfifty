package com.example.rafael.fiftyfifty;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by pogi on 9/12/2016.
 */
public class GeocodingLocation {
    private static final String TAG = "GeocodingLocation";
    public static void getAddressFromLocation(final String locationAddress, final Context context, final Handler handler) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                String result = null;
                String result1 = null;
                String put1;
                String put2;
                try {
                    List addressList = geocoder.getFromLocationName(locationAddress, 1);
                    if (addressList != null && addressList.size() > 0) {
                        Address address = (Address) addressList.get(0);
                        StringBuilder sb = new StringBuilder();
                        StringBuilder nd = new StringBuilder();
                        sb.append(address.getLatitude()).append("\n");
                        nd.append(address.getLongitude()).append("\n");
                        result = sb.toString();
                        result1 = nd.toString();
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Unable to connect to Geocoder", e);
                } finally {
                    Message message = Message.obtain();
                    message.setTarget(handler);
                    if (result != null) {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        put1 = result;
                        put2 = result1;
                        bundle.putString("address", put1);
                        bundle.putString("address1", put2);
                        message.setData(bundle);
                    } else {
                        message.what = 1;
                        Bundle bundle = new Bundle();
                        result = "Address: " + locationAddress + "\n Unable to get Latitude and Longitude for this address location.";
                        result1 = "Address: " + locationAddress + "\n Unable to get Latitude and Longitude for this address location.";
                        bundle.putString("address", result);
                        bundle.putString("address1", result1);
                        message.setData(bundle);
                    }
                    message.sendToTarget();
                }
            }
        };
        thread.start();
    }
}
