package com.example.fchataigner.pocket.places;

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
import com.example.fchataigner.pocket.R;

import org.json.JSONObject;

public class GetPlaceDetails extends AsyncTask<Place, Void, PlaceDetails>
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    static private String TAG = "GetPlaceDetails";

    public interface OnDetailsReceived
    {
        void onDetailsReceived( PlaceDetails details );
    }

    OnDetailsReceived listener;
    RequestQueue request_queue;
    String base_url, api_key;
    boolean request_running=false;
    PlaceDetails details=null;

    public GetPlaceDetails( @NonNull Context context, @NonNull OnDetailsReceived listener )
    {
        this.listener = listener;
        this.request_queue = Volley.newRequestQueue(context);
        this.base_url = context.getString(R.string.places_details_api_url);
        this.api_key = context.getString(R.string.google_places_key);
    }

    @Override
    public void onResponse( JSONObject response )
    {
        try { details = PlaceDetails.fromGoogleJSON( response.getJSONObject("result") ); }
        catch( Exception ex ) { Log.e( TAG, ex.getMessage() ); }

        request_running=false;
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( TAG, "VolleyError=" + error.toString() );
        //this.cancel(true);
    }

    protected PlaceDetails doInBackground( Place... places )
    {
        details = null;

        if ( places.length == 0 )
            return null;

        Place place = places[0];

        String query_url = base_url + "key=" + api_key + "&placeid=" + place.place_id;
        Log.i(TAG, "url=" + query_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query_url,
                null, this, this);

        request_queue.add(request);
        request_running=true;

        while( request_running )
            SystemClock.sleep(100 );

        return details;
    }

    protected void onPreExecute() {}

    protected void onPostExecute( PlaceDetails details )
    {
        listener.onDetailsReceived(details);
    }
}
