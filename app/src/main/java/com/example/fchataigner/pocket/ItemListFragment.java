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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.fchataigner.pocket.interfaces.Displayable;
import com.example.fchataigner.pocket.interfaces.JSONable;
import com.example.fchataigner.pocket.interfaces.Listable;
import com.example.fchataigner.pocket.interfaces.Shareable;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

public class ItemListFragment<Item extends Listable & Displayable & JSONable & Parcelable & Shareable>
        extends Fragment
        implements
        AsyncFileReader.Listener<Item>,
        ListView.OnItemClickListener,
        ListView.OnItemLongClickListener
{
    public final int REQUEST_ADD_ITEM = 1;
    public final String TAG = "ItemListFragment";

    private ArrayList<Item> items = new ArrayList<Item>();
    private ItemListAdapter<Item> adapter = null;
    private int deleted_position;
    private Item deleted_item=null;
    private Item base_item=null;

    public ItemListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        Bundle args = getArguments();
        String bundle_item = getContext().getString(R.string.bundle_item);
        this.base_item = (Item) args.getParcelable(bundle_item);

        String items_file = getContext().getString( base_item.getFileResource() );
        JSONable.Builder builder = base_item.getBuilder();

        AsyncFileReader<Item> file_reader = new AsyncFileReader<>( getContext(), builder, this );
        file_reader.execute(items_file);

        return inflater.inflate(R.layout.itemlist_fragment, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.item_list_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu_item)
    {
        switch (menu_item.getItemId())
        {
            case R.id.action_filter:
                return true;

            default:
                return super.onOptionsItemSelected(menu_item);
        }
    }

    @Override
    public void onItemsRead( ArrayList<Item> items )
    {
        this.items = items;

        ListView list = (ListView) getView().findViewById(R.id.list);

        adapter = new ItemListAdapter<Item>( getContext(), items, base_item.getItemLayout() );
        list.setAdapter( adapter );
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        FloatingActionButton add_button = (FloatingActionButton) getView().findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String bundle_item = getContext().getString(R.string.bundle_item);
                Intent intent = new Intent( getActivity(), base_item.getAddItemClass() );
                intent.putExtra( bundle_item, base_item );
                startActivityForResult( intent, REQUEST_ADD_ITEM );
            }
        });
    }

    @Override
    public void onStop()
    {
        String items_file = getContext().getString( base_item.getFileResource() );
        AsyncFileSaver<Item> file_saver = new AsyncFileSaver<>( getContext(), items_file, null );
        file_saver.execute( items );

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
                Item new_item = bundle.getParcelable(bundle_item);

                boolean item_exists=false;

                for ( Item item : items )
                    if ( new_item.equals(item) ) { item_exists=true; break; }

                if ( !item_exists ) items.add( 0, new_item ); // add to top of the list
                else Snackbar.make( getView(), R.string.duplicate_item, Snackbar.LENGTH_SHORT).show();
            }
            catch( Exception ex )
            {
                Log.e( TAG, "failed to add item, error=" + ex.getMessage() );
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