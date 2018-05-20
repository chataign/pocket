package com.example.fchataigner.pocket;

import android.content.Context;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.fchataigner.pocket.interfaces.JSONable;

import org.json.JSONArray;
import org.json.JSONTokener;

import java.io.FileNotFoundException;
import java.io.InputStream;
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
    private JSONable.Builder<Item> builder;
    private Listener<Item> listener;

    AsyncFileReader(@NonNull Context context, @NonNull JSONable.Builder<Item> builder, Listener<Item> listener )
    {
        this.context = context;
        this.builder = builder;
        this.listener = listener;
    }

    protected ArrayList<Item> doInBackground( String... filenames )
    {
        long start_time = SystemClock.currentThreadTimeMillis();
        ArrayList<Item> items = new ArrayList<>();

        for ( String filename : filenames )
        {
            InputStream file;

            try
            {
                file = context.openFileInput(filename);
            }
            catch( FileNotFoundException ex )
            {
                Log.w( TAG, String.format( "file='%s' not found", filename ) );
                continue;
            }

            try
            {
                String json_str = Utils.readString( context, filename );
                JSONArray json = (JSONArray) new JSONTokener(json_str).nextValue();

                for ( int i=0; i< json.length(); ++i )
                    items.add( builder.buildFromJSON( json.getJSONObject(i) ) );
            }
            catch( Exception e )
            {
                Log.w( TAG, String.format( "failed to read file='%s' error=%s", filename, e.getMessage() ) );
            }

            long time_now = SystemClock.currentThreadTimeMillis();

            Log.w( TAG, String.format( "read %d items from file='%s' in %dms",
                items.size(), filename, time_now - start_time ) );
        }

        return items;
    }

    protected void onPostExecute( ArrayList<Item> items )
    {
        if ( listener != null ) listener.onResult(items);
    }
}
