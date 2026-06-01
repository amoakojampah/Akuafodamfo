package com.example.akuafodamfo.services;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.Task;

public class LocationService {
    private static final String TAG = "LocationService";
    private final FusedLocationProviderClient fusedLocationClient;
    private final Context context;
    private LocationCallback gmsLocationCallback;

    // Single-method interface for lambda support
    public interface SimpleLocationCallback {
        void onLocationReceived(double lat, double lng);
    }

    public LocationService(@NonNull Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    @RequiresPermission(anyOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    public void getCurrentLocation(@NonNull SimpleLocationCallback callback) {
        try {
            // First try to get last known location
            Task<Location> locationTask = fusedLocationClient.getLastLocation();

            locationTask.addOnSuccessListener(location -> {
                if (location != null) {
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                } else {
                    // Last location is null, request fresh location
                    requestFreshLocation(callback);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Error getting last location", e);
                requestFreshLocation(callback);
            });

        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
        }
    }

    @RequiresPermission(anyOf = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    })
    private void requestFreshLocation(SimpleLocationCallback callback) {
        // Create location request
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY,
                10000 // 10 seconds interval
        ).setWaitForAccurateLocation(true).build();

        // Create and save the location callback
        this.gmsLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                if (location != null) {
                    // Remove updates and return result
                    fusedLocationClient.removeLocationUpdates(this);
                    callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                }
            }

            @Override
            public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                if (!locationAvailability.isLocationAvailable()) {
                    Log.w(TAG, "Location not available");
                }
            }
        };

        // Request location updates
        try {
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    gmsLocationCallback,
                    Looper.getMainLooper()
            ).addOnFailureListener(e -> {
                Log.e(TAG, "Error requesting location updates", e);
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission not granted", e);
        }
    }

    public void stopLocationUpdates() {
        if (gmsLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(gmsLocationCallback);
        }
    }
}