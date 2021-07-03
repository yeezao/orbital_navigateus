package com.example.myapptest.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.example.myapptest.R;
import com.google.android.material.snackbar.Snackbar;

public class LocationServices {

    //for location permissions and locating
    LocationManager locationManager;
    LocationManager secondLocationManager;
    Location userLocation = new Location("");
    boolean isLocationPermissionGranted = false;
    boolean isFirstRun = true;
    boolean searchingLocation = true;

    Activity activity;
    Context context;

    LocationFound listenerLocationFound;


    final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public void checkLocationPermission(Activity activity, Context context, LocationFound listenerLocationFound) {
        this.activity = activity;
        this.context = context;
        this.listenerLocationFound = listenerLocationFound;
        locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        secondLocationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            return;
        } else {
            isLocationPermissionGranted = true;
            getUserLocationAndStopList();
        }
    }



    LocationListener networkLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.e("network location is: ", location + "");
            if (location.getAccuracy() < 30) {
                userLocation = location;
                secondLocationManager.removeUpdates(this);
                locationManager.removeUpdates(gpsLocationListener);
                Log.e("userLocation is: ", "" + userLocation);
                searchingLocation = false;
                listenerLocationFound.onLocationSecured(userLocation);
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };
    LocationListener gpsLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            Log.e("gps location is: ", location + "");
            if (location.getAccuracy() < 30) {
                userLocation = location;
                locationManager.removeUpdates(this);
                secondLocationManager.removeUpdates(networkLocationListener);
                Log.e("userLocation is: ", "" + userLocation);
                searchingLocation = false;
                listenerLocationFound.onLocationSecured(userLocation);
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
        @Override
        public void onProviderEnabled(String provider) {
        }
        @Override
        public void onProviderDisabled(String provider) {
        }
    };

    public void getUserLocationAndStopList() {
        try {
            searchingLocation = true;
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Log.e("im here before null", " yes");
                isLocationPermissionGranted = true;
                secondLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, networkLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, gpsLocationListener);
            } else if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                userLocation.setLatitude(0.0);
                userLocation.setLongitude(0.0);
                isLocationPermissionGranted = false;
                Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                        "Please check that your Location setting is switched on and set to High Accuracy.",
                        Snackbar.LENGTH_LONG);
                snackbar.setAnchorView(R.id.nav_view);
                snackbar.show();
                searchingLocation = false;
                listenerLocationFound.onLocationSecured(userLocation);
            }

        } catch (SecurityException e) {
//            Log.e("SecExp", "yes");
            userLocation.setLatitude(0.0);
            userLocation.setLongitude(0.0);
            isLocationPermissionGranted = false;
            Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content),
                    "To sort the list of bus stops by proximity, please enable location permissions.",
                    Snackbar.LENGTH_LONG);
            snackbar.setAnchorView(R.id.textView_container);
            snackbar.show();
            listenerLocationFound.onLocationSecured(userLocation);

        }

    }

    public interface LocationFound {
        void onLocationSecured(Location userLocation);
    }

}
