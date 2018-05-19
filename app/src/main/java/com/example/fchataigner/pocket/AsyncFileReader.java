package com.example.fchataigner.pocket;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.fchataigner.pocket.interfaces.JSONable;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.util.ArrayList;

public class AsyncFileReader<Item>
        extends AsyncTask< String, Void, ArrayList<Item> >
{
    public interface Listener<Item>
    {
        void onResult( ArrayList<Item> items );
    }

    static final private String TAG = "AsyncFileReader";

    private Context context;
    private JSONable.Builder builder;
    private Listener listener;

    public AsyncFileReader(@NonNull Context context, @NonNull JSONable.Builder builder, Listener listener )
    {
        this.context = context;
        this.builder = builder;
        this.listener = listener;
    }

    protected ArrayList<Item> doInBackground( String... filenames )
    {
        ArrayList<Item> items = new ArrayList<>();

        for ( String filename : filenames )
        {
            try
            {
                String json_str = Utils.readString( context, filename );
                JSONArray json = (JSONArray) new JSONTokener(json_str).nextValue();

                for ( int i=0; i< json.length(); ++i )
                    items.add( (Item) builder.buildFromJSON( json.getJSONObject(i) ) );

                Log.w( TAG, String.format( "read %d items from file='%s'", items.size(), filename ) );
            }
            catch( Exception e )
            {
                Log.w( TAG, String.format( "failed to read item from file='%s' error=%s", filename, e.getMessage() ) );
            }
        }

        return items;
    }

    protected void onPostExecute( ArrayList<Item> items ) { if ( listener != null ) listener.onResult(items); }
}
