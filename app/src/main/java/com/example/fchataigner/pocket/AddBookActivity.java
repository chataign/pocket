package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AddBookActivity extends AppCompatActivity
        implements
        ListView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        FindBook.OnBookResultsListener
{
    private ArrayList<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_book_activity);
        setSupportActionBar( (Toolbar) findViewById(R.id.toolbar) );

        final SearchView search_view = findViewById( R.id.search_view );
        search_view.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        final LinearLayout layout = (LinearLayout)findViewById(R.id.layout);
        final SearchView search_view = findViewById(R.id.search_view);

        InputMethodManager manager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow( layout.getWindowToken(), 0);

        String search_text = search_view.getQuery().toString();
        String[] search_strings = search_text.split(" ");

        FindBook bookFinder = new FindBook(this.getApplicationContext(), this);
        bookFinder.execute(search_strings);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String query) { return true; }

    @Override
    public void onItemClick( AdapterView<?> list, View view, int position, long id )
    {
        Book book = (Book) list.getItemAtPosition(position);

        Intent intent = new Intent();
        String bundle_item = getApplicationContext().getString(R.string.bundle_item);
        intent.putExtra( bundle_item, book );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    public void onBookResults( ArrayList<Book> books )
    {
        Comparator<Book> BookComparator = new Comparator<Book>()
        {
            public int compare( Book book1, Book book2 )
            {
                if ( book1.ratingsCount < book2.ratingsCount ) return 1;
                else if ( book1.ratingsCount == book2.ratingsCount ) return 0;
                return -1;
            }
        };

        this.books = books;
        Collections.sort( this.books, BookComparator );

        TextView search_info = findViewById(R.id.search_info);
        search_info.setText( String.format("Found %d results", books.size() ) );

        ListView results = findViewById(R.id.search_results);
        results.setAdapter( new ItemListAdapter<Book>( this.getApplicationContext(), this.books, R.layout.book_item ) );
        results.setOnItemClickListener(this);
    }
}