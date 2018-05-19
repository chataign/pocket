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
import com.example.fchataigner.pocket.R;
import com.example.fchataigner.pocket.places.Place;
import com.example.fchataigner.pocket.places.PlaceDetails;

import org.json.JSONObject;

import java.util.ArrayList;

public class GetBookReviews extends AsyncTask< Book, Void, ArrayList<BookReview> >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    static private String TAG = "GetBookReviews";

    public interface Listener
    {
        void onBookReviews( ArrayList<BookReview> reviews );
    }

    Listener listener;
    RequestQueue request_queue;
    String api_key;
    int requests_pending=0;
    ArrayList<BookReview> reviews = new ArrayList<>();

    public GetBookReviews(@NonNull Context context, @NonNull Listener listener )
    {
        this.listener = listener;
        this.request_queue = Volley.newRequestQueue(context);
        this.api_key = context.getString(R.string.goodreads_api_read_key);
    }

    @Override
    public void onResponse( JSONObject response )
    {
        requests_pending--;

        try
        {
            Log.i( TAG, response.toString() );
            BookReview review = BookReview.fromGoodreadsXML( response.toString() );
            reviews.add(review);
        }
        catch( Exception ex ) { Log.e( TAG, ex.getMessage() ); }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( TAG, "VolleyError=" + error.toString() );
        //this.cancel(true);
    }

    protected ArrayList<BookReview> doInBackground( Book... books )
    {
        reviews.clear();

        if ( books.length == 0 )
            return reviews;

        Book book = books[0];

        String query_url = "https://www.goodreads.com/book/isbn/" + book.isbn_10 + "?format=json&key=" + api_key;
        Log.i(TAG, "url=" + query_url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, query_url,
                null, this, this);

        requests_pending++;
        request_queue.add(request);

        while( requests_pending > 0 )
            SystemClock.sleep(100 );

        return reviews;
    }

    protected void onPreExecute() {}

    protected void onPostExecute( ArrayList<BookReview> reviews )
    {
        listener.onBookReviews(reviews);
    }
}
