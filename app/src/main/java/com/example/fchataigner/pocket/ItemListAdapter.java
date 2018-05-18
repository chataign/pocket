package com.example.fchataigner.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

public class ItemListAdapter<Item extends Listable> extends ArrayAdapter<Item>
{
    private Context context;
    private List<Item> items;
    private int layout_resource;

    public ItemListAdapter(@NonNull Context context, @NonNull ArrayList<Item> items, int layout_resource )
    {
        super( context, 0 , items );

        this.context = context;
        this.items = items;
        this.layout_resource = layout_resource;
     }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Item item = items.get(position);
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(context).inflate( layout_resource, parent,false);

        item.createListView(view);
        return view;
    }
}