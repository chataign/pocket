package com.example.fchataigner.pocket;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class ItemList< Item extends JSONable & Parcelable & Adaptable>
{
    private ArrayList<Item> items = new ArrayList<>();
    public ArrayAdapter<Item> adapter=null;
    private Item deleted_item =null;
    private int deleted_position;

    final Context context;
    final int file_resource;

    ItemList(@NonNull Context context, @NonNull int file_resource )
    {
        this.file_resource = file_resource;
        this.context = context;

        loadJSON();

        this.adapter = new ItemAdapter<Item>( context, items );
    }

    public Item get( int position ) { return items.get(position); }
    public void add( Item item ) { items.add(item); }

    private void loadJSON()
    {
        try { items = Utils.readJSONFile( context, file_resource ); }
        catch( Exception ex ) { items = new ArrayList<Item>(); }
    }

    public void saveJSON()
    {
        try { Utils.writeJSONFile( items, context, file_resource ); }
        catch( Exception ex ) { Log.w( "ItemList", "failed to save JSON" ); }
    }

    public void delete( int position )
    {
        deleted_position = position;
        deleted_item = items.get(position);
        items.remove(position);
        adapter.notifyDataSetChanged();
    }

    public void undelete()
    {
        if ( deleted_item == null ) return;
        items.add( deleted_position, deleted_item );
        deleted_item = null;
        adapter.notifyDataSetChanged();
    }
}
