package com.example.fchataigner.mypocket;

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

public class FindBook extends AsyncTask< String, Void, ArrayList<Book> >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    public interface OnBookResultsListener
    {
        public void onBookResults( ArrayList<Book> books );
    }

    OnBookResultsListener listener;
    RequestQueue requestQueue;
    ArrayList<Book> books;
    String baseUrl;
    int requestsPending;

    public FindBook( Context context, @NonNull OnBookResultsListener listener )
    {
        this.listener = listener;
        books = new ArrayList<Book>();
        requestQueue = Volley.newRequestQueue(context);
        baseUrl = context.getString(R.string.book_api_url);
        requestsPending=0;
    }

    @Override
    public void onResponse( JSONObject response )
    {
        requestsPending--;
        JSONArray items;

        try { items = response.getJSONArray("items"); }
        catch( Exception ex ) { Log.e( "onResponse", ex.getMessage() ); return; }

        Log.i( "FindBook.onResponse", "results=" + items.length() );

        for ( int i=0; i< items.length(); ++i )
        {
            try { JSONObject json = items.getJSONObject(i); books.add( Book.fromGoogleJSON(json) ); }
            catch( Exception ex ) { Log.w( "FindBook.onResponse", ex.getMessage() ); }
        }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( "VolleyError", error.toString() );
        //this.cancel(true);
    }

    protected ArrayList<Book> doInBackground( String... search_strings )
    {
        books.clear();

        String queryUrl = baseUrl;
        for ( String str : search_strings ) queryUrl += "+" + str;

        queryUrl += "&printType=books&maxResults=40&langRestrict=en";
        Log.i("url", queryUrl);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, queryUrl,
                null, this, this);

        requestsPending++;
        requestQueue.add(request);

        while( requestsPending > 0 )
            SystemClock.sleep(100 );

        return books;
    }

    protected void onPreExecute() {}
    protected void onPostExecute( ArrayList<Book> books ) { listener.onBookResults(books); }
}
