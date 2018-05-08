package com.example.fchataigner.mypocket;

import android.Manifest;
import android.location.LocationListener;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener, LocationListener
{
    static int ACCESS_FINE_LOCATION;
    static int ACCESS_COARSE_LOCATION;

    Location location = null;

    private boolean loadFragment( int item_id )
    {
        Fragment fragment=null;
        String title="";

        Bundle args = new Bundle();

        switch( item_id )
        {
            case R.id.item_book:
                title = getString(R.string.category_books);
                args.putInt( "file_resource", R.string.books_file );
                fragment = new ItemListFragment<Book>();
                break;
            case R.id.item_food:
                title = getString(R.string.item_food);
                args.putInt( "file_resource", R.string.places_file );
                fragment = new ItemListFragment<Place>();
                break;
            case R.id.item_shop:
                title = getString(R.string.category_shops);
                args.putInt( "file_resource", R.string.places_file );
                fragment = new ItemListFragment<Place>();
                break;
            case R.id.item_event:
                title = getString(R.string.category_events);
                fragment = new EventFragment();
                break;
        }

        if (fragment == null) return false;
        fragment.setArguments(args);

        getSupportActionBar().setTitle(title);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
        return true;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        return loadFragment( item.getItemId() );
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onLocationChanged( Location new_location )
    {
        Log.i("onLocationChanged","latitude=" + new_location.getLatitude() + " longitude=" + new_location.getLongitude() );
        location = new_location;
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        //getSupportActionBar().setDisplayShowHomeEnabled(true);
        //getSupportActionBar().setIcon(R.mipmap.ic_pocket);

        LocationManager location_manager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]
                    { android.Manifest.permission.ACCESS_FINE_LOCATION },
                    ACCESS_FINE_LOCATION );
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]
                            { android.Manifest.permission.ACCESS_COARSE_LOCATION },
                    ACCESS_COARSE_LOCATION );
        }

        try { location_manager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 5000, 10, this ); }
        catch( SecurityException ex ) { Log.e( "onCreate", ex.getMessage() ); }

        BottomNavigationView navigation = findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        loadFragment( R.id.item_book );
    }
}
