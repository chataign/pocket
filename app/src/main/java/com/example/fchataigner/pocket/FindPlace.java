package com.example.fchataigner.pocket;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FindPlace extends AsyncTask< String, Void, ArrayList<Place> >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    public interface OnPlaceResultsListener
    {
        public void onPlaceResults(ArrayList<Place> places);
    }

    OnPlaceResultsListener listener;
    RequestQueue requestQueue;
    ArrayList<Place> places = new ArrayList<Place>();
    String baseUrl, apiKey;
    int requestsPending=0;
    final Location location;
    final int search_radius;
    final String type;

    public FindPlace(Context context, @NonNull OnPlaceResultsListener listener,
                     @NonNull Location location, int search_radius, String type )
    {
        this.listener = listener;
        this.location = location;
        this.search_radius = search_radius;
        this.type = type;
        this.requestQueue = Volley.newRequestQueue(context);
        this.baseUrl = context.getString(com.example.fchataigner.pocket.R.string.places_api_url);
        this.apiKey = context.getString(com.example.fchataigner.pocket.R.string.google_places_key);
    }

    @Override
    public void onResponse( JSONObject response )
    {
        requestsPending--;

        try
        {
            JSONArray results = response.getJSONArray("results");

            for ( int i=0; i< results.length(); ++i )
            {
                JSONObject json = results.getJSONObject(i);
                Place place = Place.fromGoogleJSON(json);

                Location place_location = new Location(place.place_id);

                place_location.setLatitude(place.latitude);
                place_location.setLongitude(place.longitude);

                if ( place_location.distanceTo(location) < search_radius )
                    places.add( place );
            }

            Log.i( "FindPlace", "results=" + results.length() );
        }
        catch( Exception ex )
        {
            Log.e( "FindPlace", ex.getMessage() ); return;
        }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( "FindPlace", "VolleyError=" + error.toString() );
        //this.cancel(true);
    }

    class SortByDistance implements Comparator<Place>
    {
        private final Location location;

        SortByDistance( @NonNull Location location )
        {
            this.location = location;
        }

        public int compare(Place place1, Place place2 )
        {
            Location l1 = new Location("place1");
            Location l2 = new Location("place2");

            l1.setLatitude(place1.latitude);
            l1.setLongitude(place1.longitude);

            l2.setLatitude(place2.latitude);
            l2.setLongitude(place2.longitude);

            double d1 = location.distanceTo(l1);
            double d2 = location.distanceTo(l2);

            if ( d1 < d2 ) return 1;
            if ( d1 == d2 ) return 0;
            return -1;
        }
    }

    protected ArrayList<Place> doInBackground( String... search_strings )
    {
        places.clear();

        String queryUrl = baseUrl;
        queryUrl += "location=" + location.getLatitude() + "," + location.getLongitude();
        queryUrl += "&rankby=distance";
        //queryUrl += "&radius=" + search_radius;
        queryUrl += "&key=" + apiKey;
        //queryUrl += "&type=" + type;
        queryUrl += "&keyword=" + type;
        for ( String str : search_strings ) queryUrl += "+" + str;

        Log.i("FindPlace", "url=" + queryUrl);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, queryUrl,
                null, this, this);

        requestsPending++;
        requestQueue.add(request);

        while( requestsPending > 0 )
            SystemClock.sleep(100 );

        return places;
    }

    protected void onPreExecute() {}

    protected void onPostExecute( ArrayList<Place> places )
    {
        Collections.sort(places, new SortByDistance(location) );
        listener.onPlaceResults(places);
    }
}
