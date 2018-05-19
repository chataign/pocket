package com.example.fchataigner.pocket.books;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Xml;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.fchataigner.pocket.R;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;

public class GetBookAuthor extends AsyncTask< String, Void, ArrayList<BookAuthor> >
        implements Response.ErrorListener, Response.Listener<String>
{
    static private String TAG = "GetBookAuthor";

    public interface Listener
    {
        void onResults(ArrayList<BookAuthor> authors );
    }

    Listener listener;
    RequestQueue request_queue;
    String api_key;
    int requests_pending=0;
    ArrayList<BookAuthor> authors = new ArrayList<>();

    public GetBookAuthor(@NonNull Context context, @NonNull Listener listener )
    {
        this.listener = listener;
        this.request_queue = Volley.newRequestQueue(context);
        this.api_key = context.getString(R.string.goodreads_api_read_key);
    }

    @Override
    public void onResponse( String response )
    {
        requests_pending--;

        try
        {
            Log.i( TAG, response.toString() );
            BookAuthor author = BookAuthor.parseGoodreadsXML(response);
            authors.add(author);
        }
        catch( Exception ex ) { Log.e( TAG, ex.getMessage() ); }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( TAG, "VolleyError=" + error.toString() );
        //this.cancel(true);
    }

    protected ArrayList<BookAuthor> doInBackground( String... query_strings )
    {
        authors.clear();

        String query_url = "https://www.goodreads.com/api/author_url/";
        for ( String query_string : query_strings )
        {
            query_string.replaceAll( " ", "+" );
            query_url += query_string;
        }
        query_url += "?format=json&key=" + api_key;

        Log.i(TAG, "url=" + query_url);

        StringRequest request = new StringRequest(Request.Method.GET, query_url,
                this, this);

        requests_pending++;
        request_queue.add(request);

        while( requests_pending > 0 )
            SystemClock.sleep(100 );

        return authors;
    }

    protected void onPreExecute() {}

    protected void onPostExecute( ArrayList<BookAuthor> authors ) { listener.onResults(authors); }
}
