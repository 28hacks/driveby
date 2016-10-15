package com.github.a28hacks.driveby.location;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by stefan on 15.10.16.
 */

public class DbLocationAdapter implements LocationListener {
    private static final String TAG = "DbLocationAdapter";
    private LocationChangedListener mChangedListener;

    public interface LocationChangedListener {
        void onLocationChanged(Location newLocation);
    }


    public DbLocationAdapter(String provider) {
        Log.d(TAG, "LocationListener " + provider);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged: " + location);
        if (mChangedListener != null) {
            mChangedListener.onLocationChanged(location);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "onProviderDisabled: " + provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "onProviderEnabled: " + provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "onStatusChanged: " + provider);
    }

    public void setChangedListener(LocationChangedListener changedListener) {
        mChangedListener = changedListener;
    }
}
