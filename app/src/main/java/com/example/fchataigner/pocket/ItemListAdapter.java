package com.example.fchataigner.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.example.fchataigner.pocket.interfaces.Listable;

import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter<Item extends Listable> extends ArrayAdapter<Item>
{
    Context context;
    int layout_resource;
    int removed_position;
    Item removed_item=null;

    public ItemListAdapter(@NonNull Context context, @NonNull ArrayList<Item> items, int layout_resource )
    {
        super( context, 0 , items );

        this.context = context;
        this.layout_resource = layout_resource;
    }

    public void remove( int position )
    {
        removed_item = getItem(position);
        removed_position=position;

        super.remove(removed_item);
        notifyDataSetChanged();
    }

    public int undoRemove()
    {
        if ( removed_item == null ) return -1;
        this.insert( removed_item, removed_position );
        removed_item = null;
        notifyDataSetChanged();
        return removed_position;
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Item item = getItem(position);
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(context).inflate( layout_resource, parent,false);

        item.createListView(view);
        return view;
    }
}
