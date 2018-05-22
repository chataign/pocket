package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.Vibrator;
import android.support.annotation.NonNull;
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

import com.example.fchataigner.pocket.interfaces.Detailable;
import com.example.fchataigner.pocket.interfaces.JSONable;
import com.example.fchataigner.pocket.interfaces.Listable;
import com.example.fchataigner.pocket.interfaces.Shareable;

import java.util.ArrayList;

import static android.content.Context.VIBRATOR_SERVICE;

public abstract class ItemListFragment<Item extends Listable & Detailable & JSONable & Parcelable & Shareable>
        extends Fragment
        implements
        ListView.OnItemClickListener,
        ListView.OnItemLongClickListener
{
    public final int REQUEST_ADD_ITEM = 1;
    public final String TAG = "ItemListFragment";

    private ItemListAdapter<Item> list_adapter = null;
    private ItemsFile<Item> item_file=null;

    protected abstract int getFileResource();
    protected abstract int getItemListLayout();
    protected abstract Class<?> getAddItemActivity();
    protected abstract JSONable.Builder<Item> getBuilder();

    public ItemListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);

        String items_file = getContext().getString( getFileResource() );

        item_file = new ItemsFile<Item>( getContext(), getFileResource(), getBuilder() );

        list_adapter = new ItemListAdapter<Item>( getContext(), new ArrayList<Item>(), getItemListLayout() );
        list_adapter.addAll( item_file.getItems() );

        /*
        AsyncFileReader<Item> file_reader = new AsyncFileReader<>(
                getContext(), getBuilder(), new AsyncFileReader.Listener<Item>()
        {
            @Override
            public void onResult(ArrayList<Item> items) { list_adapter.addAll(items); }
        } );

        file_reader.execute(items_file);
        */

        return inflater.inflate(R.layout.itemlist_fragment, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ListView list = getView().findViewById(R.id.list);
        list.setEmptyView( getView().findViewById( R.id.empty_list_view ) );
        list.setAdapter( list_adapter );
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        FloatingActionButton add_button = (FloatingActionButton) getView().findViewById(R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent( getActivity(), getAddItemActivity() );
                startActivityForResult( intent, REQUEST_ADD_ITEM );
            }
        });
    }

    @Override
    public void onStop()
    {
        //String items_file = getContext().getString( getFileResource() );
        //AsyncFileSaver<Item> file_saver = new AsyncFileSaver<>( getContext(), items_file, null );
        //file_saver.execute( list_adapter.getItems() );

        super.onStop();
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
    public void onActivityResult( int request, int result, Intent intent )
    {
        if ( request == REQUEST_ADD_ITEM && result == Activity.RESULT_OK )
        {
            try
            {
                String bundle_item = getContext().getString(R.string.bundle_item);
                Item new_item = intent.getParcelableExtra(bundle_item);

                boolean item_exists=false;

                for ( int i=0; i< list_adapter.getCount(); ++i )
                    if ( list_adapter.getItem(i).equals(new_item) ) { item_exists=true; break; }

                if ( !item_exists )
                {
                    list_adapter.insert(new_item, 0); // add to top of the list
                    list_adapter.notifyDataSetChanged();
                    item_file.insert(new_item);
                    Log.i( TAG, "added item=" + new_item.toString() );
                }
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

        final Item removed = (Item) list.getItemAtPosition(position);

        list_adapter.remove(position);
        item_file.delete(removed);

        View.OnClickListener undo_callback = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                list_adapter.undoRemove();
                item_file.insert( removed );
            }
        };

        Snackbar snackbar = Snackbar.make( getView(), R.string.item_deleted, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, undo_callback );
        snackbar.show();

        return true;
    }
}
