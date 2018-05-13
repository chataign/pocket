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

public class GetPlaceDetails extends AsyncTask< Place, Void, PlaceDetails >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    public interface OnDetailsReceived
    {
        public void onDetailsReceived( PlaceDetails details );
    }

    OnDetailsReceived listener;
    RequestQueue request_queue;
    String base_url, api_key;
    int requests_pending=0;
    PlaceDetails details=null;

    public GetPlaceDetails(Context context, @NonNull OnDetailsReceived listener )
    {
        this.listener = listener;
        this.request_queue = Volley.newRequestQueue(context);
        this.base_url = context.getString(R.string.places_details_api_url);
        this.api_key = context.getString(R.string.google_places_key);
    }

    @Override
    public void onResponse( JSONObject response )
    {
        requests_pending--;

        try
        {
            details = PlaceDetails.fromGoogleJSON( response.getJSONObject("result") );
        }
        catch( Exception ex )
        {
            Log.e( "FindPlace", ex.getMessage() ); return;
        }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( "GetPlaceDetails", "VolleyError=" + error.toString() );
        //this.cancel(true);
    }

    protected PlaceDetails doInBackground( Place... places )
    {
        details = null;

        if ( places.length == 0 )
                return null;

        Place place = places[0];

        String query_url = base_url + "key=" + api_key + "&placeid=" + place.place_id;
        Log.i("GetPlaceDetails", "url=" + query_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query_url,
                null, this, this);

        requests_pending++;
        request_queue.add(request);

        while( requests_pending > 0 )
            SystemClock.sleep(100 );

        return details;
    }

    protected void onPreExecute() {}

    protected void onPostExecute( PlaceDetails details )
    {
        listener.onDetailsReceived(details);
    }
}
