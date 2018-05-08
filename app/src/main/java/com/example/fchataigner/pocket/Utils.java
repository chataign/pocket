package com.example.fchataigner.pocket;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public final class Utils
{
    static String readString( Context context, String filename ) throws IOException
    {
        InputStream file = context.openFileInput(filename);
        InputStreamReader stream_reader = new InputStreamReader(file);
        BufferedReader buffered_reader = new BufferedReader(stream_reader);

        StringBuilder string_builder = new StringBuilder();

        while (true)
        {
            String str = buffered_reader.readLine();
            if ( str == null ) break;
            string_builder.append(str);
        }

        return string_builder.toString();
    }

    static final <T extends JSONable>
    ArrayList<T> readJSONFile( Context context, int filename_resource )
            throws IOException, JSONException
    {
        String filename = context.getString(filename_resource);
        String json_str = Utils.readString( context, filename );
        JSONArray json = (JSONArray) new JSONTokener(json_str).nextValue();

        ArrayList<T> items = new ArrayList<T>( json.length() );

        for ( int i=0; i< json.length(); ++i )
            items.get(i).readJSON( json.getJSONObject(i) );

        return items;
    }

    static final <T extends JSONable>
    void writeJSONFile(ArrayList<T> items, Context context, int filename_resource )
            throws IOException, JSONException
    {
        JSONArray json = new JSONArray();
        for ( JSONable item : items ) json.put( item.writeJSON() );

        String filename = context.getString(filename_resource);
        FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);
        OutputStreamWriter writer = new OutputStreamWriter(file);
        writer.write( json.toString() );
        writer.close();
    }
}
