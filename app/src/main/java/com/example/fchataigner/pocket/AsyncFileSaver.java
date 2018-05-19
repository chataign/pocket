package com.example.fchataigner.pocket;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.fchataigner.pocket.interfaces.JSONable;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class AsyncFileSaver<Item extends JSONable>
        extends AsyncTask< List<Item>, Void, Void >
{
    interface Listener
    {
        void onPostExecute( boolean success, String error );
    };

    static private String TAG = "AsyncFileSaver";

    private Context context;
    private String filename;
    private Listener listener=null;

    private String error;
    private boolean success=false;

    public AsyncFileSaver(@NonNull Context context, @NonNull String filename, Listener listener )
    {
        this.context = context;
        this.filename = filename;
        this.listener = listener;
    }

    protected Void doInBackground( List<Item>... item_lists )
    {
        success=true;

        try
        {
            JSONArray json = new JSONArray();
            for ( List<Item> items : item_lists )
                for ( JSONable item : items ) json.put( item.writeJSON() );

            Utils.writeString( json.toString(), context, filename );
            Log.i( TAG, String.format( "saved %d items to file=%s", json.length(), filename ) );

        }
        catch( Exception e )
        {
            success=false; error = e.getMessage();
            Log.w( TAG, String.format( "failed to save items to file=%s error=%s", filename, error ) );
        }

        return null;
    }

    protected void onPreExecute() {}
    protected void onPostExecute() { if ( listener != null ) listener.onPostExecute(success,error); }
}
