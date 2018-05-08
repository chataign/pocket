package com.example.fchataigner.mypocket;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.Line;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BookFinder extends AsyncTask< Bitmap, Void, ArrayList<Book> >
        implements Response.ErrorListener, Response.Listener<JSONObject>
{
    Frame.Builder frameBuilder;
    TextRecognizer textDetector;
    RequestQueue requestQueue;
    ArrayList<Book> books;
    String baseUrl;
    int requestsPending;
    ListView bookList;
    Context context;

    public BookFinder( Context context, ListView bookList )
    {
        this.context = context;
        this.bookList = bookList;

        books = new ArrayList<Book>();
        frameBuilder = new Frame.Builder();
        textDetector = new TextRecognizer.Builder(context).build();
        requestQueue = Volley.newRequestQueue(context);
        baseUrl = context.getString(R.string.book_api_url);
        requestsPending=0;
    }

    private static Comparator<Line> LineHeightDescending = new Comparator<Line>()
    {
        public int compare(Line l1, Line l2)
        {
            double h1 = l1.getBoundingBox().height();
            double h2 = l2.getBoundingBox().height();

            if ( h1 > h2 ) return -1;
            if ( h1 < h2 ) return +1;
            return 0;
        }
    };

    @Override
    public void onResponse( JSONObject response )
    {
        requestsPending--;

        try
        {
            JSONArray items = response.getJSONArray("items");

            for ( int i=0; i< items.length(); ++i )
            {
                JSONObject json = items.getJSONObject(i);
                books.add( Book.fromGoogleJSON(json) );
            }
        }
        catch( Exception ex )
        {
            Log.e( "onResponse", ex.getMessage() );
            return;
        }
    }

    @Override
    public void onErrorResponse( VolleyError error )
    {
        Log.e( "VolleyError", error.toString() );
        //this.cancel(true);
    }

    protected ArrayList<Book> doInBackground( Bitmap... bitmaps )
    {
        books.clear();

        frameBuilder.setBitmap( bitmaps[0] );
        SparseArray<TextBlock> textBlocks = textDetector.detect( frameBuilder.build() );

        ArrayList<Line> lines = new ArrayList<Line>();

        for ( int i=0; i< textBlocks.size(); ++i )
        {
            TextBlock block = textBlocks.valueAt(i);

            for ( Text text : block.getComponents() )
                if ( text instanceof Line ) lines.add( (Line) text );
        }

        Collections.sort( lines, LineHeightDescending );
        Log.i( "BookFinder", "blocks=" + textBlocks.size() + " lines=" + lines.size() );

        String queryUrl = baseUrl;

        for ( int i=0; i< Math.min( 2, lines.size() ); ++i )
        {
            for ( Text text : lines.get(i).getComponents() )
                queryUrl += "+" + text.getValue();
        }

        queryUrl += "&printType=books";
        Log.i("url", queryUrl);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, queryUrl,
                null, this, this);

        requestsPending++;
        requestQueue.add(request);

        while( requestsPending > 0 )
            SystemClock.sleep(100 );

        return books;
    }

    protected void onPreExecute()
    {
        //ProgressBar progressBar = (ProgressBar) findViewById( R.res.)
    }

    protected void onPostExecute( ArrayList<Book> books )
    {
        bookList.setAdapter( new ItemAdapter<Book>( context, books ) );
    }
}
