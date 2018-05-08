package com.example.fchataigner.mypocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class ItemDetailsFragment<T extends Adaptable> extends Fragment
        implements Button.OnClickListener
{
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle args = this.getArguments();
        T item = (T) args.getParcelable(getContext().getString(R.string.bundle_item));

        View view = inflater.inflate( item.detailsLayout(), container, false);
        item.createDetailView(view);
        return view;
    }

    @Override
    public void onClick(View v)
    {
        /*
        if ( item != null )
        {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.link) );
            startActivity(intent);
        }
        */
    }
}
