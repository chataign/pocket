package com.example.fchataigner.pocket.places;

import android.location.Location;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.fchataigner.pocket.AddLocalizedItemActivity;
import com.example.fchataigner.pocket.R;

import java.util.ArrayList;

public class FindPlaceActivity extends AddLocalizedItemActivity<Place>
{
    private PlaceFinder place_finder=null;

    @Override
    public int getListItemLayout() { return R.layout.place_item; }

    @Override
    public int getActivityLayout() { return R.layout.add_place_activity; }

    @Override
    public String getQueryHints() { return "place name, address"; }

    @Override
    public @NonNull
    ListView getListView() { return findViewById(R.id.results_list); }

    @Override
    public @NonNull
    SearchView getSearchView() { return findViewById(R.id.search_view); }

    @Override
    public @NonNull
    TextView getInfoView() { return findViewById(R.id.info_text); }

    @Override
    public @NonNull
    ProgressBar getProgressBar() { return findViewById(R.id.progress_bar); }

    @Override
    public void setupSearchInterface()
    {
        String[] place_types = this.getResources().getStringArray(R.array.place_types);
        ArrayAdapter<String> type_adapter = new ArrayAdapter<>( this, R.layout.spinner_item, R.id.spinner_text, place_types );
        Spinner type_spinner = findViewById(R.id.type_spinner);
        type_spinner.setAdapter( type_adapter );

        ArrayList<Integer> search_radii = new ArrayList<>();
        for ( int radius : this.getResources().getIntArray(R.array.search_radii) ) search_radii.add(radius);

        ArrayAdapter<Integer> radius_adapter = new ArrayAdapter<>( this, R.layout.spinner_item, R.id.spinner_text, search_radii );
        Spinner radius_spinner = findViewById(R.id.radius_spinner);
        radius_spinner.setAdapter( radius_adapter );
    }

    @Override
    public boolean startSearch( String query, @NonNull Location location )
    {
        Spinner radius_spinner = findViewById(R.id.radius_spinner);
        Integer search_radius = (Integer) radius_spinner.getSelectedItem();

        Spinner type_spinner = findViewById(R.id.type_spinner);
        String place_type = (String) type_spinner.getSelectedItem();

        if ( place_finder != null ) place_finder.cancel(true);
        place_finder = new PlaceFinder( this, location, search_radius, place_type, this );
        place_finder.execute( query );

        return true;
    }

    @Override
    public void stopSearch()
    {
        if ( place_finder != null ) place_finder.cancel(true);
    }
}