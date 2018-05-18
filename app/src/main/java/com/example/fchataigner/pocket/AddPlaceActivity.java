package com.example.fchataigner.pocket;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;

public class AddPlaceActivity extends AppCompatActivity
        implements
        SearchView.OnQueryTextListener,
        FindPlace.OnPlaceResultsListener,
        ListView.OnItemClickListener
{
    static private String TAG = "AddPlaceActivity";
    static private int ACCESS_COARSE_LOCATION = 1;
    static private int ACCESS_FINE_LOCATION = 2;

    private Location location = null;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.add_place_activity );
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        requestLocationPermissions();

        LocationManager location_manager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener location_listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location new_location) { location = new_location; }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        try { location_manager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 100, 0, location_listener ); }
        catch( SecurityException ex ) { Log.e( TAG, "location listener error=" + ex.getMessage() ); }

        SearchView search_view = findViewById(R.id.search_view);
        search_view.setQueryHint( "place name, address" );
        search_view.setOnQueryTextListener(this);

        final Activity activity = this;

        ImageButton camera_button = findViewById(R.id.camera_button);
        camera_button.setOnClickListener( new Button.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent intent = new Intent( activity, OcrCaptureActivity.class );
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, false);
                startActivityForResult( intent, OcrCaptureActivity.GET_TEXT );
            }
        } );
    }

    void requestLocationPermissions()
    {
        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]
                    { android.Manifest.permission.ACCESS_FINE_LOCATION }, ACCESS_FINE_LOCATION );
        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED )
        {
            ActivityCompat.requestPermissions( this, new String[]
                    { android.Manifest.permission.ACCESS_COARSE_LOCATION }, ACCESS_COARSE_LOCATION );
        }
    }

    @Override
    public boolean onQueryTextChange( String query )
    {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit( String query )
    {
        SearchView search_view = findViewById(R.id.search_view);

        InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow( search_view.getWindowToken(), 0);

        if ( location == null )
        {
            Snackbar.make( search_view, "No GPS location", Snackbar.LENGTH_SHORT ).show();
            return false;
        }

        String[] search_strings = query.split(" ");
        FindPlace place_finder = new FindPlace( this, this, location, 1000, "food" );
        place_finder.execute(search_strings);

        return true;
    }

    @Override
    public void onPlaceResults( @NonNull ArrayList<Place> places )
    {
        TextView info_text = findViewById(R.id.info_text);
        info_text.setText( String.format( "found %d places", places.size() ) );

        ListView results_list = findViewById(R.id.results_list);
        results_list.setAdapter( new ItemListAdapter( this.getApplicationContext(), places, R.layout.place_item ) );
        results_list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id )
    {
        Place place = (Place) list.getItemAtPosition(position);

        Intent intent = new Intent();
        String bundle_item = this.getString(R.string.bundle_item);
        intent.putExtra( bundle_item, place );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    @Override
    public void onActivityResult( int request, int result, Intent intent )
    {
        if ( request == OcrCaptureActivity.GET_TEXT && result == CommonStatusCodes.SUCCESS )
        {
            String query = intent.getStringExtra( OcrCaptureActivity.TextBlockObject );
            query = query.replaceAll("['\"+\n\t]"," ");

            SearchView search_view = findViewById(R.id.search_view);
            search_view.setQuery( query, false );
            this.onQueryTextSubmit(query);
        }
    }
}