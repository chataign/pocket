package com.example.fchataigner.pocket.places;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.fchataigner.pocket.ItemFinder;
import com.example.fchataigner.pocket.Utils;
import com.example.fchataigner.pocket.interfaces.AsyncResultsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaceFinder extends ItemFinder<Place>
{
    private String base_url, api_key;
    private final Location location;
    private final int search_radius;
    private final String place_type;

    PlaceFinder(Context context,
                @NonNull Location location,
                int search_radius,
                String place_type,
                @NonNull AsyncResultsListener<Place> listener)
    {
        super( context, listener );

        this.location = location;
        this.search_radius = search_radius;
        this.place_type = place_type;
        this.base_url = context.getString(com.example.fchataigner.pocket.R.string.places_api_url);
        this.api_key = context.getString(com.example.fchataigner.pocket.R.string.google_places_key);
    }

    @Override
    protected String createQueryUrl( String[] search_strings, String next_page_token )
    {
        String search_string = Utils.join( search_strings, "+" );
        search_string = search_string.replaceAll( " ", "+" );

        String query_url = base_url;
        query_url += "location=" + location.getLatitude() + "," + location.getLongitude();
        query_url += "&rankby=distance";
        //query_url += "&radius=" + search_radius;
        query_url += "&key=" + api_key;
        //query_url += "&type=" + place_type;
        query_url += "&keyword=" + place_type + "+" + search_string;
        if ( !next_page_token.isEmpty() ) query_url += "&pagetoken=" + next_page_token;

        return query_url;
    }

    @Override
    protected List<Place> parseResponse(JSONObject response ) throws JSONException
    {
        JSONArray json_results = response.getJSONArray("results");
        ArrayList<Place> places = new ArrayList<>();

        for ( int i=0; i< json_results.length(); ++i )
        {
            JSONObject json_place = json_results.getJSONObject(i);

            try
            {
                Place place = Place.fromGoogleJSON(json_place);
                place.distance = (double) location.distanceTo( place.getLocation() );
                if ( place.distance < search_radius ) places.add( place );
            }
            catch( Exception ex )
            {
                Log.w( TAG, "Failed to parse place JSON, error=" + ex.getMessage() );
            }
        }

        return places;
    }
}
