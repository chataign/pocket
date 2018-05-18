package com.example.fchataigner.pocket;

import android.content.Context;
import android.util.Log;

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
import java.util.Arrays;

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

    static void writeString( String string, Context context, String filename ) throws IOException
    {
        FileOutputStream file = context.openFileOutput(filename, Context.MODE_PRIVATE);
        OutputStreamWriter writer = new OutputStreamWriter(file);
        writer.write( string );
        writer.close();
    }

    static String join( String[] strings, String delimiter )
    {
        String str = new String("");
        if ( strings.length == 0 ) return str;
        str = strings[0];
        for ( int i=1; i< strings.length; ++i ) str += delimiter + strings[i];
        return str;
    }
}
