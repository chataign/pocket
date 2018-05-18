package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.api.CommonStatusCodes;

import java.util.ArrayList;
import java.util.Collections;

public class AddBookActivity extends AppCompatActivity
        implements
        SearchView.OnQueryTextListener,
        FindBook.OnBookResultsListener,
        ListView.OnItemClickListener
{
    static private String TAG = "AddBookActivity";

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.add_book_activity );
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        SearchView search_view = findViewById(R.id.search_view);
        search_view.setQueryHint( "book title, author" );
        search_view.setOnQueryTextListener(this);

        ArrayList<Language> languages = new ArrayList<>();
        languages.add( Language.English );
        languages.add( Language.Spanish );
        languages.add( Language.French );

        ArrayAdapter<Language> language_adapter = new ArrayAdapter<Language>( this, R.layout.spinner_item, R.id.spinner_text, languages );

        Spinner language_spinner = findViewById(R.id.language_spinner);
        language_spinner.setAdapter( language_adapter );

        ArrayList<String> types = new ArrayList<>();
        types.add( "books" );
        types.add( "magazines" );
        types.add( "all" );

        ArrayAdapter<String> type_adapter = new ArrayAdapter<>( this, R.layout.spinner_item, R.id.spinner_text, types );

        Spinner type_spinner = findViewById(R.id.type_spinner);
        type_spinner.setAdapter( type_adapter );

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
                startActivityForResult( intent, OcrCaptureActivity.GET_TEXT );
            }
        } );
    }

    @Override
    public boolean onQueryTextChange( String query )
    {
        return true;
    }

    @Override
    public boolean onQueryTextSubmit( String query )
    {
        Spinner language_spinner = findViewById(R.id.language_spinner);
        Language language = (Language) language_spinner.getSelectedItem();

        Spinner type_spinner = findViewById(R.id.type_spinner);
        String type = (String) type_spinner.getSelectedItem();

        InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow( language_spinner.getWindowToken(), 2);

        String[] query_strings = query.split(" ");

        FindBook book_finder = new FindBook( this, language, type, this );
        book_finder.execute(query_strings);

        ProgressBar progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        return true;
    }

    @Override
    public void onBookResults( @NonNull ArrayList<Book> books )
    {
        ProgressBar progress_bar = findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.INVISIBLE);

        TextView info_text = findViewById(R.id.info_text);
        info_text.setText( String.format( "Found %d books", books.size() ) );

        Collections.sort( books, Book.OrderByRating );

        ListView results_list = findViewById(R.id.results_list);
        results_list.setAdapter( new ItemListAdapter( this.getApplicationContext(), books, R.layout.book_item ) );
        results_list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> list, View view, int position, long id )
    {
        Book book = (Book) list.getItemAtPosition(position);

        Intent intent = new Intent();
        String bundle_item = this.getString(R.string.bundle_item);
        intent.putExtra( bundle_item, book );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    @Override
    public void onActivityResult( int request, int result, Intent intent )
    {
        if ( request == OcrCaptureActivity.GET_TEXT && result == CommonStatusCodes.SUCCESS )
        {
            String query = intent.getStringExtra( OcrCaptureActivity.TextBlockObject );
            query = query.replaceAll("['\"+\n\t]"," ");

            SearchView search_view = findViewById(R.id.search_view);
            search_view.setQuery( query, true );
            //this.onQueryTextSubmit(query);
        }
    }
}