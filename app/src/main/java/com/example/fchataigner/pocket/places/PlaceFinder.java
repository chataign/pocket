package com.example.fchataigner.pocket.places;

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
import java.util.LinkedList;

public class PlaceFinder extends AsyncTask< Void, Void, Void >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    public interface Listener
    {
        void onNewResults(ArrayList<Place> places);
        void onPostExecute();
    }

    class DelayedRequest
    {
        JsonObjectRequest request;
        long time_added_ms;
        long delay_ms;

        DelayedRequest( JsonObjectRequest request, long delay_ms )
        {
            this.request = request;
            this.time_added_ms = SystemClock.currentThreadTimeMillis();
            this.delay_ms = delay_ms;
        }

        private long elapsed()
        {
            long time_now_ms = SystemClock.currentThreadTimeMillis();
            return ( time_now_ms - time_added_ms );
        }

        private boolean isReady()
        {
            return elapsed() > delay_ms;
        }
    }

    private Listener listener;
    private RequestQueue request_queue;
    private String base_url, api_key;
    private boolean request_running=false;
    private final Location location;
    private final int search_radius;
    private final String place_type;
    private String search_string;
    private int response_count=0;
    private LinkedList<DelayedRequest> delayed_requests = new LinkedList<>();

    private static String TAG = "PlaceFinder";
    private static long REQUEST_DELAY_MS = 2000;

    public PlaceFinder(Context context, @NonNull Listener listener,
                       @NonNull Location location, int search_radius, String place_type, String search_string )
    {
        this.listener = listener;
        this.location = location;
        this.search_radius = search_radius;
        this.place_type = place_type;
        this.search_string = search_string;
        this.request_queue = Volley.newRequestQueue(context);
        this.base_url = context.getString(com.example.fchataigner.pocket.R.string.places_api_url);
        this.api_key = context.getString(com.example.fchataigner.pocket.R.string.google_places_key);
    }

    @Override
    public void onResponse( JSONObject response )
    {
        String next_page_token = response.optString("next_page_token" );

        if ( !next_page_token.isEmpty() )
        {
            JsonObjectRequest request = createRequest( search_string, next_page_token );
            delayed_requests.addLast( new DelayedRequest( request, REQUEST_DELAY_MS ) );
        }

        try
        {
            JSONArray results = response.getJSONArray("results");
            Log.i( TAG, String.format( "onResponse[%d]: parsing %d results", ++response_count, results.length() ) );

            ArrayList<Place> places = new ArrayList<>();

            for ( int i=0; i< results.length(); ++i )
            {
                JSONObject json = results.getJSONObject(i);
                Place place = Place.fromGoogleJSON(json);
                place.distance = (double) location.distanceTo( place.getLocation() );

                if ( place.distance < search_radius )
                    places.add( place );
            }

            listener.onNewResults(places);
        }
        catch( Exception ex )
        {
            Log.e( TAG, ex.getMessage() );
        }

        request_running=false;
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( TAG, "VolleyError=" + error.toString() );
        this.cancel(true);
    }

    @Override
    protected Void doInBackground( Void... nothings )
    {
        JsonObjectRequest request = createRequest( search_string, "" );
        delayed_requests.add( new DelayedRequest( request, 0 ) );

        while( !isCancelled() )
        {
            if ( request_running ) { SystemClock.sleep(100); continue; }
            else if ( delayed_requests.isEmpty() ) break;

            DelayedRequest first = delayed_requests.getFirst();

            if ( first.isReady() )
            {
                Log.i( TAG, String.format( "starting request after %.2f seconds", first.elapsed() / 1000.0 ) );

                request_running=true;
                request_queue.add( first.request );
                delayed_requests.removeFirst();
            }
        }

        return null;
    }

    private JsonObjectRequest createRequest( String search_string, String next_page_token )
    {
        String query_url = base_url;
        query_url += "location=" + location.getLatitude() + "," + location.getLongitude();
        query_url += "&rankby=distance";
        //query_url += "&radius=" + search_radius;
        query_url += "&key=" + api_key;
        //query_url += "&type=" + place_type;
        search_string = search_string.replaceAll( " ", "+" );
        query_url += "&keyword=" + place_type + "+" + search_string;
        if ( !next_page_token.isEmpty() ) query_url += "&pagetoken=" + next_page_token;

        Log.i(TAG, "url=" + query_url);

        return new JsonObjectRequest(Request.Method.GET, query_url,
                null, this, this);
    }

    @Override
    protected void onCancelled()
    {
        delayed_requests.clear();
        request_queue.stop();
        request_running=false;

        Log.i( TAG, "onCancelled" );
        super.onCancelled();
    }

    @Override
    protected void onPreExecute() {}

    @Override
    protected void onPostExecute( Void nothing ) { listener.onPostExecute(); }
}
