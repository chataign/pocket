package com.example.fchataigner.pocket.places;

import com.example.fchataigner.pocket.ItemListFragment;
import com.example.fchataigner.pocket.R;

public class PlaceListFragment extends ItemListFragment<Place>
{
    protected int getFileResource() { return R.string.places_file; }
    protected int getItemListLayout() { return R.layout.place_item; }
    protected Class<?> getAddItemActivity() { return FindPlaceActivity.class; }
    protected Place.Builder getBuilder() { return new Place().new Builder(); }
}