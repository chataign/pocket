package com.example.fchataigner.pocket.books;

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
import com.example.fchataigner.pocket.Language;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class BookFinder extends AsyncTask< String, Void, ArrayList<Book> >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    private static String TAG = "BookFinder";

    public interface Listener
    {
        void onResults( ArrayList<Book> books );
    }

    Language language;
    String type;
    Listener listener;
    RequestQueue requestQueue;
    ArrayList<Book> books;
    String base_url;
    int requestsPending;

    public BookFinder(@NonNull Context context,
                      @NonNull Language language,
                      @NonNull String type,
                      @NonNull Listener listener )
    {
        this.language = language;
        this.type = type;
        this.listener = listener;

        books = new ArrayList<Book>();
        requestQueue = Volley.newRequestQueue(context);
        base_url = context.getString(com.example.fchataigner.pocket.R.string.book_api_url);
        requestsPending=0;
    }

    @Override
    public void onResponse( JSONObject response )
    {
        requestsPending--;
        JSONArray json_items;

        try { json_items = response.getJSONArray("items"); }
        catch( Exception ex ) { Log.e( "onResponse", ex.getMessage() ); return; }

        Log.i( TAG, "results=" + json_items.length() );

        for ( int i=0; i< json_items.length(); ++i )
        {
            try
            {
                JSONObject json = json_items.getJSONObject(i);
                Book book = Book.fromGoogleJSON(json);

                if ( book.language.equals(this.language.code) == false )
                    continue;

                books.add( book );
            }
            catch( Exception ex )
            {
                Log.w( "BookFinder.onResponse", ex.getMessage() );
            }
        }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( TAG, "VolleyError=" + error.toString() );
        //this.cancel(true);
    }

    protected ArrayList<Book> doInBackground( String... search_strings )
    {
        books.clear();

        String query_url = base_url;
        for ( String str : search_strings ) query_url += "+" + str;

        query_url += "&printType=" + type;
        query_url += "&maxResults=40";
        query_url += "&langRestrict=" + language.code;
        
        Log.i(TAG, query_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query_url,
                null, this, this);

        requestsPending++;
        requestQueue.add(request);

        while( requestsPending > 0 )
            SystemClock.sleep(100 );

        return books;
    }

    protected void onPreExecute() {}
    protected void onPostExecute( ArrayList<Book> books ) { listener.onResults(books); }
}
