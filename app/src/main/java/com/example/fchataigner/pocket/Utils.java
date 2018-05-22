package com.example.fchataigner.pocket;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.example.fchataigner.pocket.interfaces.JSONable;
import com.google.android.gms.vision.text.Text;
import com.google.android.gms.vision.text.TextBlock;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

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

    static <Item extends JSONable>
    List<Item> readFromFile( Context context, String filename, JSONable.Builder<Item> builder )
        throws JSONException, IOException
    {
        long start_time = SystemClock.currentThreadTimeMillis();
        ArrayList<Item> items = new ArrayList<>();

        String json_str = Utils.readString( context, filename );
        JSONArray json = (JSONArray) new JSONTokener(json_str).nextValue();

        for ( int i=0; i< json.length(); ++i )
            items.add( builder.buildFromJSON( json.getJSONObject(i) ) );

        long time_now = SystemClock.currentThreadTimeMillis();

        Log.i( "readFromFile", String.format( "read %d items from file='%s' in %dms",
                items.size(), filename, time_now - start_time ) );

        return items;
    }

    static <Item extends JSONable>
    void writeToFile( Context context, String filename, List<Item> items )
            throws JSONException, IOException
    {
        long start_time = SystemClock.currentThreadTimeMillis();

        JSONArray json = new JSONArray();
            for ( JSONable item : items ) json.put( item.writeJSON() );

        Utils.writeString( json.toString(), context, filename );

        long time_now = SystemClock.currentThreadTimeMillis();

        Log.i( "writeToFile", String.format( "wrote %d items to file='%s' in %dms",
                items.size(), filename, time_now - start_time ) );
    }

    static public String join( List<String> strings, String delimiter )
    {
        String str="";

        if ( strings.size() > 0 )
        {
            str = strings.get(0);
            for ( int i=1; i< strings.size(); ++i ) str += delimiter + strings.get(i);
        }

        return str;
    }

    static public String join( String[] strings, String delimiter )
    {
        ArrayList<String> string_array = new ArrayList<>();
        for ( String string : strings ) string_array.add(string);
        return join( string_array, delimiter );
    }

    static public ArrayList<String> getStrings( TextBlock block )
    {
        ArrayList<String> strings = new ArrayList<>();

        for ( Text text : block.getComponents() )
        {
            List<? extends Text> components = text.getComponents();
            if ( components.size() <= 1 ) strings.add( text.getValue() );
            else for ( Text component : components ) strings.add( component.getValue() );
        }

        return strings;
    }

    static public String readXmlText( XmlPullParser parser, String tag ) throws IOException, XmlPullParserException
    {
        String result = "";
        parser.require( XmlPullParser.START_TAG, null, tag );
        if ( parser.next() == XmlPullParser.TEXT) { result = parser.getText(); parser.nextTag(); }
        parser.require( XmlPullParser.END_TAG, null, tag );
        return result;
    }

    static public void skip( XmlPullParser parser ) throws XmlPullParserException, IOException
    {
        if (parser.getEventType() != XmlPullParser.START_TAG)
            throw new IllegalStateException();

        int depth = 1;

        while (depth != 0)
        {
            switch (parser.next())
            {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}
