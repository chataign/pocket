package com.example.fchataigner.pocket;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.example.fchataigner.pocket.interfaces.Detailable;
import com.example.fchataigner.pocket.interfaces.Listable;

public abstract class AddLocalizedItemActivity<Item extends Parcelable & Listable & Detailable>
        extends FindtemActivity<Item>
        implements LocationListener
{
    static protected String LOCATION_UNAVAILABLE = "Location unavailable";
    static private int ACCESS_COARSE_LOCATION = 1;
    static private int ACCESS_FINE_LOCATION = 2;

    protected Location location = null;

    public abstract boolean startSearch( String query, @NonNull Location location );

    @Override
    public boolean startSearch( String query )
    {
        if ( location == null )
        {
            Snackbar.make( getListView(), LOCATION_UNAVAILABLE, Snackbar.LENGTH_SHORT ).show();
            return false;
        }

        return startSearch( query, location );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        requestLocationPermissions();

        LocationManager location_manager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        try
        {
            Location last_location = location_manager.getLastKnownLocation( LocationManager.NETWORK_PROVIDER );
            this.onLocationChanged(last_location);

            location_manager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 0, 0, this );
            location_manager.requestLocationUpdates( LocationManager.GPS_PROVIDER, 0, 0, this );
        }
        catch( SecurityException ex )
        {
            Log.e( TAG, "location listener error=" + ex.getMessage() );
        }
    }

    @Override
    public void onLocationChanged(Location new_location)
    {
        if ( location == null ) Log.i( TAG, "received location=" + new_location.toString() );
        location = new_location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
        Log.i( TAG, "provider=" + provider + " has status=" + status );
    }

    @Override
    public void onProviderEnabled(String provider)
    {
        Log.i( TAG, "provider=" + provider + " enabled" );
    }

    @Override
    public void onProviderDisabled(String provider)
    {
        Log.i( TAG, "provider=" + provider + " disabled" );
    }

    void requestLocationPermissions()
    {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]
                    { Manifest.permission.ACCESS_FINE_LOCATION }, ACCESS_FINE_LOCATION );
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]
                    { Manifest.permission.ACCESS_COARSE_LOCATION }, ACCESS_COARSE_LOCATION );
        }
    }
}