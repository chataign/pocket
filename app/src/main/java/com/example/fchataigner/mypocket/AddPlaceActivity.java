package com.example.fchataigner.mypocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class AddPlaceActivity extends Activity
        implements ListView.OnItemClickListener,
        FindPlace.OnPlaceResultsListener
{
    static final String EXTRA_ITEM_TYPE = "item_type";
    static final String EXTRA_LATITUDE = "latitude";
    static final String EXTRA_LONGITUDE = "longitude";
    static final String EXTRA_SEARCH_RADIUS = "search_radius";

    private ArrayList<Place> places;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_place_activity);
    }

    public void onAddItemClicked(View view)
    {
        Bundle extras = getIntent().getExtras();

        int item_type = extras.getInt(EXTRA_ITEM_TYPE);
        double latitude = extras.getDouble(EXTRA_LATITUDE);
        double longitude = extras.getDouble(EXTRA_LONGITUDE);
        double search_radius = extras.getDouble(EXTRA_SEARCH_RADIUS);

        TextView text_view = findViewById( R.id.search_text );

        String search_text = text_view.getText().toString();
        String[] search_strings = search_text.split(" ");

        switch( item_type )
        {
            case R.id.item_food:
            {
                FindPlace placeFinder = new FindPlace( this.getApplicationContext(), this,
                        latitude, longitude, search_radius, "food" );
                placeFinder.execute( search_strings );
                break;
            }
            case R.id.item_shop:
            {
                FindPlace placeFinder = new FindPlace( this.getApplicationContext(), this,
                        latitude, longitude, search_radius, "store" );
                placeFinder.execute( search_strings );
                break;
            }
        }
    }

    @Override
    public void onItemClick( AdapterView<?> list, View view, int position, long id )
    {
        Intent intent = new Intent();
        Place place = (Place) list.getItemAtPosition(position);
        intent.putExtra("place", place );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    public void onPlaceResults( ArrayList<Place> places )
    {
        this.places = places;

        ListView results = findViewById(R.id.search_results);
        results.setAdapter( new ItemAdapter<Place>( this.getApplicationContext(), this.places ) );
        results.setOnItemClickListener(this);
    }
}