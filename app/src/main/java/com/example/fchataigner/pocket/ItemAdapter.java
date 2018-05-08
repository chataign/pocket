package com.example.fchataigner.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter<Item extends Adaptable> extends ArrayAdapter<Item>
{
    private Context context;
    private List<Item> items = new ArrayList<>();

    public ItemAdapter(@NonNull Context context, ArrayList<Item> items )
    {
        super( context, 0 , items );

        this.context = context;
        this.items = items;
     }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Item item = items.get(position);
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(context).inflate( item.listLayout(),parent,false);

        item.createListView(view);
        return view;
    }
}