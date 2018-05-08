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

import static android.content.Context.VIBRATOR_SERVICE;

public class ItemListFragment<T extends Parcelable & Adaptable & JSONable>
        extends Fragment
        implements
        ListView.OnItemClickListener,
        ListView.OnItemLongClickListener
{
    public final int REQUEST_ADD_ITEM = 1;

    private ItemList<T> itemlist;
    private Vibrator vibrator=null;

    public ItemListFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = getArguments();
        int file_resource = args.getInt("file_resource");

        itemlist = new ItemList<T>( getContext(), file_resource );
        return inflater.inflate(com.example.fchataigner.pocket.R.layout.itemlist_fragment, container, false);
    }

    @Override
    public void onStart()
    {
        super.onStart();

        ListView list = (ListView) getView().findViewById(com.example.fchataigner.pocket.R.id.list);

        list.setAdapter( itemlist.adapter );
        list.setOnItemClickListener(this);
        list.setOnItemLongClickListener(this);

        vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);

        FloatingActionButton add_button = (FloatingActionButton) getView().findViewById(com.example.fchataigner.pocket.R.id.add_button);
        add_button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent( getActivity(), AddBookActivity.class );
                startActivityForResult( intent, REQUEST_ADD_ITEM );
            }
        });
    }

    @Override
    public void onStop()
    {
        super.onStop();
        itemlist.saveJSON();
    }

    @Override
    public void onActivityResult( int request, int result, Intent intent )
    {
        if ( request == REQUEST_ADD_ITEM && result == Activity.RESULT_OK )
        {
            try
            {
                Bundle bundle = intent.getExtras();
                itemlist.add( (T) bundle.getParcelable(getContext().getString(com.example.fchataigner.pocket.R.string.bundle_item) ) );
            }
            catch( Exception ex )
            {
                Log.e( "onActivityResult", ex.getMessage() );
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id )
    {
        Bundle bundle = new Bundle();
        bundle.putParcelable( getContext().getString(com.example.fchataigner.pocket.R.string.bundle_item), itemlist.get(position) );

        Fragment fragment = new ItemDetailsFragment<T>();
        fragment.setArguments(bundle);

        getFragmentManager().beginTransaction()
                .replace( com.example.fchataigner.pocket.R.id.fragment_container, fragment )
                .addToBackStack(null)
                .commit();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> list, View view, int position, long id )
    {
        if ( vibrator != null ) vibrator.vibrate(50);

        itemlist.delete(position);

        View.OnClickListener undo_callback = new View.OnClickListener()
        {
            @Override
            public void onClick(View v) { itemlist.undelete(); }
        };

        Snackbar snackbar = Snackbar.make( getView(), com.example.fchataigner.pocket.R.string.item_deleted, Snackbar.LENGTH_SHORT);
        snackbar.setAction(com.example.fchataigner.pocket.R.string.undo, undo_callback );
        snackbar.show();

        return true;
    }
}