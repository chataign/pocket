package com.example.fchataigner.pocket;

import android.content.Context;
import android.util.Log;

import com.example.fchataigner.pocket.interfaces.JSONable;

import java.util.ArrayList;
import java.util.List;

public class ItemsFile<Item extends JSONable>
{
    private Context context;
    private String filename;
    private List<Item> file_items = new ArrayList<>();

    ItemsFile( Context context, int file_resource, JSONable.Builder<Item> builder )
    {
        this.context = context;
        this.filename = context.getString(file_resource);

        try { this.file_items = Utils.readFromFile( context, filename, builder ); }
        catch( Exception ex ) { Log.e( filename, ex.getMessage() ); }
    }

    List<Item> getItems()
    {
        return file_items;
    }

    void insert( Item item )
    {
        file_items.add(item);
        save();
    }

    void delete( Item item )
    {
        for ( int i=0; i< file_items.size(); ++i )
            if (file_items.get(i).equals(item) ) file_items.remove(i);
        save();
    }

    void save()
    {
        try { Utils.writeToFile( context, filename, file_items ); }
        catch( Exception ex ) { Log.e( filename, ex.getMessage() ); }
    }
}
