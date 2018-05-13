package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

public class ItemListFragment<Item extends Displayable & JSONable & Parcelable>
        extends Fragment
        implements
        ListView.OnItemClickListener,
        ListView.OnItemLongClickListener
{
    public final int REQUEST_ADD_ITEM = 1;

    private ArrayList<Item> items = new ArrayList<Item>();
    private ItemListAdapter<Item> adapter = null;
    private int deleted_position;
    private Item deleted_item=null;
    private Item base_item=null;

    public ItemListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i("Items", "ItemListFragment::onCreateView" );

        try
        {
            Bundle args = getArguments();
            String bundle_item = getContext().getString(R.string.bundle_item);
            this.base_item = (Item) args.getParcelable(bundle_item);
            String items_file = getContext().getString( base_item.getFileResource() );

            JSONArray json = Utils.readJSONFile( getContext(), items_file );

            items = new ArrayList<Item>();

            for ( int i=0; i< json.length(); ++i )
                items.add( (Item) base_item.buildFromJSON( json.getJSONObject(i) ) );

            Log.i( "Items", String.format( "read %d items from file=", items.size(), items_file ) );
        }
        catch( Exception ex )
        {
            Log.w( "Items", String.format( "failed to read file, error=%s", ex.getMessage() ) );
            items = new ArrayList<Item>();
        }

        return inflater.inflate(R.layout.itemlist_fragment, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.i("Items", "ItemListFragment::onStart" );

        ListView list = (ListView) getView().findViewById(R.id.list);

        adapter = new ItemListAdapter<Item>( getContext(), items, base_item.getItemLayout() );
        list.setAdapter( adapter );
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        FloatingActionButton add_button = (FloatingActionButton) getView().findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent( getActivity(), base_item.getAddItemClass() );
                startActivityForResult( intent, REQUEST_ADD_ITEM );
            }
        });
    }

    @Override
    public void onStop()
    {
        Log.i("Items", "ItemListFragment::onStop" );
        String items_file = getContext().getString( base_item.getFileResource() );

        try
        {
            Utils.writeJSONFile( items, getContext(), items_file );
            Log.i( "Items", String.format( "saved %d items to file=%s", items.size(), items_file ) );
        }
        catch( Exception ex )
        {
            Log.e( "Items", "failed to save items to file=" + items_file );
        }

        super.onStop();
    }

    @Override
    public void onActivityResult( int request, int result, Intent intent )
    {
        if ( request == REQUEST_ADD_ITEM && result == Activity.RESULT_OK )
        {
            try
            {
                String bundle_item = getContext().getString(R.string.bundle_item);
                Bundle bundle = intent.getExtras();
                Item new_item = (Item) bundle.getParcelable(bundle_item);

                boolean item_exists=false;

                for ( Item item : items )
                    if ( new_item.equals(item) ) { item_exists=true; break; }

                if ( item_exists )
                {
                    Snackbar.make( getView(), R.string.duplicate_item, Snackbar.LENGTH_SHORT).show();
                }
                else
                {
                    items.add( 0, new_item ); // add to top of the list
                }
            }
            catch( Exception ex )
            {
                Log.e( "Items", "failed to add item, error=" + ex.getMessage() );
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id )
    {
        Item item = (Item) list.getItemAtPosition(position);

        String bundle_item = getContext().getString(R.string.bundle_item);
        Bundle args = new Bundle();
        args.putParcelable( bundle_item, item );

        Fragment fragment = new ItemDetailsFragment<Item>();
        fragment.setArguments(args);

        getFragmentManager().beginTransaction()
                .replace( R.id.fragment_container, fragment )
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> list, View view, int position, long id )
    {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        if ( vibrator != null ) vibrator.vibrate(50);

        deleted_position = position;
        deleted_item = items.get(position);
        items.remove(position);
        adapter.notifyDataSetChanged();

        View.OnClickListener undo_callback = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if ( deleted_item == null ) return;
                items.add( deleted_position, deleted_item );
                deleted_item = null;
                adapter.notifyDataSetChanged();
            }
        };

        Snackbar snackbar = Snackbar.make( getView(), R.string.item_deleted, Snackbar.LENGTH_SHORT);
        snackbar.setAction(R.string.undo, undo_callback );
        snackbar.show();

        return true;
    }
}