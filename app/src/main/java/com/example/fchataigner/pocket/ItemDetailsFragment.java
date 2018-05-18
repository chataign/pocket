package com.example.fchataigner.pocket;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.example.fchataigner.pocket.interfaces.Displayable;
import com.example.fchataigner.pocket.interfaces.Shareable;

public class ItemDetailsFragment<Item extends Displayable & Shareable> extends Fragment
{
    private Item item = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = this.getArguments();
        String bundle_item = getContext().getString(R.string.bundle_item);
        item = (Item) args.getParcelable(bundle_item);

        View view = inflater.inflate( item.getDetailsLayout(), container, false);
        item.createDetailsView( getContext(), view );

        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.book_details_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu_item)
    {
        switch (menu_item.getItemId())
        {
            case R.id.action_share:
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, item.getShareableString() );
                sendIntent.setType("text/plain");
                startActivity( Intent.createChooser(sendIntent, "Send to") );
                return true;

            default:
                return super.onOptionsItemSelected(menu_item);
        }
    }
}
