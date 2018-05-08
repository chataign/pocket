package com.example.fchataigner.pocket;

import android.content.Context;
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
    final double latitude, longitude, search_radius;
    final String type;

    public FindPlace(Context context, @NonNull OnPlaceResultsListener listener,
                    double latitude, double longitude, double search_radius, String type )
    {
        this.listener = listener;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.search_radius = search_radius;
        this.requestQueue = Volley.newRequestQueue(context);
        this.baseUrl = context.getString(com.example.fchataigner.pocket.R.string.places_api_url);
        this.apiKey = context.getString(com.example.fchataigner.pocket.R.string.google_api_key);
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
                places.add( Place.fromGoogleJSON(json) );
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

    protected ArrayList<Place> doInBackground( String... search_strings )
    {
        places.clear();

        String queryUrl = baseUrl;
        queryUrl += "location=" + latitude + "," + longitude;
        queryUrl += "&radius=" + search_radius;
        queryUrl += "&type=" + type;
        queryUrl += "&key=" + apiKey;

        //for ( String str : search_strings ) queryUrl += "+" + str;

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
    protected void onPostExecute( ArrayList<Place> places ) { listener.onPlaceResults(places); }
}
