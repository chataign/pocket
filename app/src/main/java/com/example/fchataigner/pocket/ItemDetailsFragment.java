package com.example.fchataigner.pocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ItemDetailsFragment<Item extends Displayable> extends Fragment
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = this.getArguments();
        String bundle_item = getContext().getString(R.string.bundle_item);
        Item item = (Item) args.getParcelable(bundle_item);

        View view = inflater.inflate( item.getDetailsLayout(), container, false);
        item.createDetailsView( getContext(), view );

        return view;
    }
}
