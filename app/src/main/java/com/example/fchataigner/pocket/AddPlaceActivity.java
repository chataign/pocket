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
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class AddPlaceActivity extends AppCompatActivity
        implements
        ListView.OnItemClickListener,
        FindPlace.OnPlaceResultsListener,
        SearchView.OnQueryTextListener,
        LocationListener
{
    static int ACCESS_FINE_LOCATION;
    static int ACCESS_COARSE_LOCATION;

    Location location = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.i("AddPlace", "onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_place_activity);
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        final SearchView search_view = findViewById(R.id.search_view);
        final SeekBar seekbar = findViewById(R.id.search_radius);
        final TextView seekbar_label = findViewById(R.id.seekbar_label);

        search_view.setOnQueryTextListener(this);
        seekbar_label.setText( String.format("Radius: %dm", seekbar.getProgress() ) );

        seekbar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                { seekbar_label.setText( String.format("Radius: %dm", progress ) ); }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        } );

        //final TextView text_view = findViewById( R.id.search_text );
        //text_view.setOnEditorActionListener(this);

        LocationManager location_manager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

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

        try { location_manager.requestLocationUpdates( LocationManager.NETWORK_PROVIDER, 1000, 0, this ); }
        catch( SecurityException ex ) { Log.e( "onCreate", ex.getMessage() ); }

        Log.i("AddPlace", "onCreate:done");
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        final LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        final SearchView search_view = findViewById(R.id.search_view);
        final SeekBar search_radius = findViewById(R.id.search_radius);

        InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow( layout.getWindowToken(), 0);

        if ( location == null )
        {
            Snackbar.make( layout, "No GPS location", Snackbar.LENGTH_SHORT).show();
            return false;
        }

        String search_text = search_view.getQuery().toString();
        String[] search_strings = search_text.split(" ");

        FindPlace placeFinder = new FindPlace( this.getApplicationContext(), this,
                location, search_radius.getProgress(), "food" );
        placeFinder.execute( search_strings );
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) { return true; }

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
    public void onItemClick( AdapterView<?> list, View view, int position, long id )
    {
        Place place = (Place) list.getItemAtPosition(position);
        Log.i( "AddPlace", "adding place id=" + place.place_id );

        Intent intent = new Intent();
        String bundle_item = getApplicationContext().getString(R.string.bundle_item);
        intent.putExtra(bundle_item, place );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    public void onPlaceResults( ArrayList<Place> places )
    {
        Log.i( "AddPlace", "onPlaceResults");

        final SeekBar search_radius = findViewById(R.id.search_radius);
        final TextView info_view = findViewById(R.id.search_info);

        info_view.setText( String.format("Found %d results (search radius: %dm)",
                places.size(), search_radius.getProgress() ) );

        ListView results = findViewById(R.id.search_results);
        results.setAdapter( new ItemListAdapter<Place>( this.getApplicationContext(), places, R.layout.place_item ) );
        results.setOnItemClickListener(this);
    }
}