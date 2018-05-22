package com.example.fchataigner.pocket;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.fchataigner.pocket.interfaces.Listable;
import com.example.fchataigner.pocket.interfaces.AsyncResultsListener;
import com.example.fchataigner.pocket.ocr.OcrCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;
import java.util.List;

public abstract class AddItemActivity<Item extends Parcelable & Listable> extends AppCompatActivity
        implements
        SearchView.OnQueryTextListener,
        AsyncResultsListener<Item>,
        ListView.OnItemClickListener
{
    static final protected String TAG = "AddItemActivity";
    static final protected String QUERY_DELIMITER = " ";

    public abstract String getQueryHints();
    public abstract int getListItemLayout();
    public abstract int getActivityLayout();

    public abstract @NonNull ListView getListView();
    public abstract @NonNull SearchView getSearchView();
    public abstract ProgressBar getProgressBar();
    public abstract TextView getInfoView();

    public abstract void setupSearchInterface();
    public abstract boolean startSearch( String query );
    public abstract void stopSearch();

    private ItemListAdapter<Item> list_adapter;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        setContentView( getActivityLayout() );
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        list_adapter = new ItemListAdapter<Item>(
                this.getApplicationContext(), new ArrayList<Item>(), getListItemLayout() );

        ListView results_list = getListView();
        results_list.setAdapter( list_adapter );
        results_list.setOnItemClickListener(this);

        SearchView search_view = getSearchView();
        search_view.setQueryHint( getQueryHints() );
        search_view.setOnQueryTextListener(this);

        setupSearchInterface();

        final Activity activity = this;

        ImageButton camera_button = findViewById(R.id.camera_button);
        camera_button.setOnClickListener( new Button.OnClickListener()
        {
            @Override
            public void onClick( View view )
            {
                Intent intent = new Intent( activity, OcrCaptureActivity.class );
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, false);
                startActivityForResult( intent, OcrCaptureActivity.OCR_GET_TEXT );
            }
        } );
    }

    @Override
    public void onStop()
    {
        stopSearch();
        super.onStop();
    }

    @Override
    public boolean onQueryTextChange( String query )
    {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit( String query )
    {
        ProgressBar progress_bar = getProgressBar();
        if ( progress_bar != null ) progress_bar.setVisibility(View.VISIBLE);

        try
        {
            InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow( progress_bar.getWindowToken(), 2);
        }
        catch( Exception ex ) {  }

        list_adapter.clear();
        startSearch(query);

        return true;
    }

    @Override
    public void onNewResults( @NonNull List<Item> new_items )
    {
        list_adapter.addAll(new_items);
        list_adapter.notifyDataSetChanged();
        // TODO sort items

        TextView info_text = getInfoView();
        if ( info_text != null )  info_text.setText( String.format( "Found %d results", list_adapter.getCount() ) );
    }

    @Override
    public void onPostExecute()
    {
        ProgressBar progress_bar = getProgressBar();
        if ( progress_bar != null ) progress_bar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id )
    {
        Item item = (Item) list.getItemAtPosition(position);

        Intent intent = new Intent();
        String bundle_item = this.getString(R.string.bundle_item);
        intent.putExtra( bundle_item, item );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    @Override
    public void onActivityResult( int request, int result, Intent intent )
    {
        if ( intent == null )
            return;

        if ( request == OcrCaptureActivity.OCR_GET_TEXT && result == CommonStatusCodes.SUCCESS )
        {
            ArrayList<String> ocr_strings = intent.getStringArrayListExtra( OcrCaptureActivity.OCR_TEXT_RESULT );

            if ( ocr_strings == null || ocr_strings.isEmpty() )
            {
                Log.w( TAG, "No strings received from OCR");
                return;
            }

            String query = Utils.join( ocr_strings, QUERY_DELIMITER );
            query = query.replaceAll("['\"+\n\t]", QUERY_DELIMITER );

            SearchView search_view = getSearchView();
            search_view.setQuery( query, true );
            //this.onQueryTextSubmit(query);
        }
    }
}