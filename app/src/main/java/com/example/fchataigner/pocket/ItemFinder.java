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
import com.example.fchataigner.pocket.interfaces.AsyncResultsListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public abstract class ItemFinder<Item> extends AsyncTask< String, Void, Void >
        implements
        Response.ErrorListener,
        Response.Listener<JSONObject>
{
    protected abstract String createQueryUrl( String[] search_strings, String next_page_token );
    protected abstract List<Item> parseResponse(JSONObject response ) throws JSONException;

    private class DelayedRequest
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

    private AsyncResultsListener<Item> listener;
    private RequestQueue request_queue;
    private boolean request_running=false;
    private LinkedList<DelayedRequest> delayed_requests = new LinkedList<>();
    private String[] search_strings=null;

    protected static String TAG = "ItemFinder";

    private static long REQUEST_WAIT_MS = 100;
    private static long REQUEST_DELAY_MS = 2000;

    public ItemFinder(Context context, @NonNull AsyncResultsListener<Item> listener )
    {
        this.listener = listener;
        this.request_queue = Volley.newRequestQueue(context);
    }

    @Override
    public void onResponse( JSONObject response )
    {
        String next_page_token = response.optString("next_page_token" );

        if ( !next_page_token.isEmpty() )
        {
            String query_url = createQueryUrl( search_strings, next_page_token );
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query_url,
                    null, this, this);
            delayed_requests.addLast( new DelayedRequest( request, REQUEST_DELAY_MS ) );
        }

        try
        {
            List<Item> items = parseResponse(response);
            Log.i( TAG, String.format( "parsed %d results", items.size() ) );
            listener.onNewResults(items);
        }
        catch( JSONException ex )
        {
            Log.e( TAG, "failed to parse response, error=" + ex.getMessage() );
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
    protected Void doInBackground( String... search_strings )
    {
        this.search_strings = search_strings;
        String query_url = createQueryUrl( search_strings, "");

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query_url,
                null, this, this);

        delayed_requests.add(new DelayedRequest(request, 0));

        while (!isCancelled())
        {
            if (request_running) { SystemClock.sleep(REQUEST_WAIT_MS); continue; }
            else if ( delayed_requests.isEmpty() ) break; // we're done

            DelayedRequest next_request = delayed_requests.getFirst();
            if ( !next_request.isReady() ) { SystemClock.sleep(REQUEST_WAIT_MS); continue; }

            Log.i(TAG, String.format("starting request after %.2f seconds, url=%s",
                    next_request.elapsed() / 1000.0, next_request.request.getUrl() ) );

            request_running = true;
            request_queue.add( next_request.request );
            delayed_requests.removeFirst();
        }

        return null;
    }

    @Override
    protected void onCancelled()
    {
        delayed_requests.clear();
        request_queue.stop();
        request_running=false;

        Log.i( TAG, "cancelled" );
        super.onCancelled();
    }

    @Override
    protected void onPreExecute() {}

    //Collections.sort( books, Book.OrderByRating );
    //listener.onNewResults(books);
    //listener.onPostExecute();

    @Override
    protected void onPostExecute( Void nothing ) { listener.onPostExecute(); }
}
