package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class AddBookActivity extends Activity
        implements ListView.OnItemClickListener,
        TextView.OnEditorActionListener,
        FindBook.OnBookResultsListener
{
    private ArrayList<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_book_activity);

        final TextView text_view = findViewById( R.id.search_text );
        text_view.setOnEditorActionListener(this);
    }

    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event)
    {
        String search_text = view.getText().toString();
        String[] search_strings = search_text.split(" ");

        FindBook bookFinder = new FindBook(this.getApplicationContext(), this);
        bookFinder.execute(search_strings);
        return true;
    }

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
        this.books = books;
        Collections.sort(this.books);

        TextView search_info = findViewById(R.id.search_info);
        search_info.setText( String.format("Found %d results", books.size() ) );

        ListView results = findViewById(R.id.search_results);
        results.setAdapter( new ItemAdapter<Book>( this.getApplicationContext(), this.books, R.layout.book_item ) );
        results.setOnItemClickListener(this);
    }
}