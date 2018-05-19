package com.example.fchataigner.pocket.places;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fchataigner.pocket.ItemListAdapter;
import com.example.fchataigner.pocket.ItemListFragment;
import com.example.fchataigner.pocket.R;
import com.example.fchataigner.pocket.Utils;
import com.example.fchataigner.pocket.ocr.OcrCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;
import java.util.Collections;

public class AddPlaceActivity extends AppCompatActivity
        implements
        SearchView.OnQueryTextListener,
        PlaceFinder.Listener,
        ListView.OnItemClickListener,
        LocationListener
{
    static private String TAG = "AddPlaceActivity";
    static private String LOCATION_UNAVAILABLE = "Location unavailable";

    static private int ACCESS_COARSE_LOCATION = 1;
    static private int ACCESS_FINE_LOCATION = 2;

    private Location location = null;
    private int location_count=0;
    private View activity_view = null;
    private ItemListAdapter<Place> place_adapter = null;
    private PlaceFinder place_finder=null;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        LayoutInflater layout_inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        activity_view = layout_inflater.inflate( R.layout.add_place_activity, null );
        setContentView( activity_view );
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar( toolbar );

        requestLocationPermissions();

        LocationManager location_manager = (LocationManager)
                this.getSystemService(Context.LOCATION_SERVICE);

        TextView location_text = findViewById(R.id.location_text);
        location_text.setText( LOCATION_UNAVAILABLE );

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

        place_adapter = new ItemListAdapter( this.getApplicationContext(), new ArrayList<Place>(), R.layout.place_item );
        ListView results_list = findViewById(R.id.results_list);
        results_list.setAdapter( place_adapter );
        results_list.setOnItemClickListener(this);

        SearchView search_view = findViewById(R.id.search_view);
        search_view.setQueryHint( "place name, address" );
        search_view.setOnQueryTextListener(this);

        String[] place_types = this.getResources().getStringArray(R.array.place_types);
        ArrayAdapter<String> type_adapter = new ArrayAdapter<>( this, R.layout.spinner_item, R.id.spinner_text, place_types );
        Spinner type_spinner = findViewById(R.id.type_spinner);
        type_spinner.setAdapter( type_adapter );

        ArrayList<Integer> search_radii = new ArrayList<>();
        for ( int radius : this.getResources().getIntArray(R.array.search_radii) ) search_radii.add(radius);

        ArrayAdapter<Integer> radius_adapter = new ArrayAdapter<>( this, R.layout.spinner_item, R.id.spinner_text, search_radii );
        Spinner radius_spinner = findViewById(R.id.radius_spinner);
        radius_spinner.setAdapter( radius_adapter );

        final Activity activity = this;

        ImageButton camera_button = findViewById(R.id.camera_button);
        camera_button.setOnClickListener( new Button.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                if ( location == null )
                {
                    Snackbar.make( activity_view, LOCATION_UNAVAILABLE, Snackbar.LENGTH_SHORT ).show();
                    return;
                }

                Intent intent = new Intent( activity, OcrCaptureActivity.class );
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, false);
                startActivityForResult( intent, OcrCaptureActivity.GET_TEXT );
            }
        } );
    }

    @Override
    public void onLocationChanged(Location new_location)
    {
        TextView location_text = findViewById(R.id.location_text);
        location_text.setText( String.format( "Location accuracy: %dm (%d)",
                (int) new_location.getAccuracy(), ++location_count ) );

        location = new_location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onProviderDisabled(String provider) {}

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
        InputMethodManager manager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow( activity_view.getWindowToken(), 0);

        if ( location == null )
        {
            Snackbar.make( activity_view, LOCATION_UNAVAILABLE, Snackbar.LENGTH_SHORT ).show();
            return false;
        }

        Spinner radius_spinner = findViewById(R.id.radius_spinner);
        Integer search_radius = (Integer) radius_spinner.getSelectedItem();

        Spinner type_spinner = findViewById(R.id.type_spinner);
        String place_type = (String) type_spinner.getSelectedItem();

        ProgressBar progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        place_adapter.clear();
        place_adapter.notifyDataSetChanged();

        if ( place_finder != null ) place_finder.cancel(true);
        place_finder = new PlaceFinder( this, this, location, search_radius, place_type, query );
        place_finder.execute();

        return true;
    }

    @Override
    public void onNewResults( @NonNull ArrayList<Place> places )
    {
        Log.i( TAG, String.format( "received %d search results", places.size() ) );

        place_adapter.addAll(places);
        place_adapter.sort( new Place.SortByDistance(location) );
        place_adapter.notifyDataSetChanged();

        TextView info_text = findViewById(R.id.info_text);
        info_text.setText( String.format( "Found %d places", place_adapter.getCount() ) );
    }

    @Override
    public void onPostExecute()
    {
        Log.i( TAG, "onPostExecute");
        ProgressBar progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id )
    {
        if ( place_finder != null ) place_finder.cancel(true);

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
        if ( intent == null )
            return;

        if ( request == OcrCaptureActivity.GET_TEXT && result == CommonStatusCodes.SUCCESS )
        {
            ArrayList<String> strings = intent.getStringArrayListExtra( OcrCaptureActivity.TextBlockObject );

            if ( strings == null || strings.isEmpty() )
            {
                Log.w( TAG, "No strings received from OCR");
                return;
            }

            String query = Utils.join( strings, " " );
            query = query.replaceAll("['\"+\n\t]"," ");
            Log.i( TAG, "query=" + query );

            SearchView search_view = findViewById(R.id.search_view);
            search_view.setQuery( query, false );
            this.onQueryTextSubmit(query);
        }
    }
}