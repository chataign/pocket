package com.example.fchataigner.pocket.books;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.fchataigner.pocket.ItemFinder;
import com.example.fchataigner.pocket.Language;
import com.example.fchataigner.pocket.Utils;
import com.example.fchataigner.pocket.interfaces.AsyncResultsListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BookFinder extends ItemFinder<Book>
{
    private Language language;
    private String type;
    private String base_url;

    BookFinder(@NonNull Context context,
              @NonNull Language language,
              @NonNull String type,
              @NonNull AsyncResultsListener<Book> listener )
{
        super( context, listener );

        this.language = language;
        this.type = type;
        base_url = context.getString(com.example.fchataigner.pocket.R.string.book_api_url);
    }

    @Override
    protected String createQueryUrl( String[] search_strings, String next_page_token )
    {
        String search_string = Utils.join( search_strings, "+" );
        search_string = search_string.replaceAll( " ", "+" );

        String query_url = base_url + search_string;
        query_url += "&printType=" + type;
        query_url += "&langRestrict=" + language.code;
        query_url += "&maxResults=40";

        return query_url;
    }

    @Override
    protected List<Book> parseResponse( JSONObject response ) throws JSONException
    {
        JSONArray json_items = response.getJSONArray("items");
        ArrayList<Book> books = new ArrayList<>();

        for ( int i=0; i< json_items.length(); ++i )
        {
            JSONObject json = json_items.getJSONObject(i);

            try
            {
                Book book = Book.fromGoogleJSON(json);
                if ( book.language.equals(this.language.code) ) books.add(book);
            }
            catch( Exception ex )
            {
                Log.w( TAG, "Failed to parse book JSON, error=" + ex.getMessage() );
            }
        }

        return books;
    }
}
