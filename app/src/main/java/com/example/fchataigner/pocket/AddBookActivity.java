package com.example.fchataigner.pocket;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class AddBookActivity extends Activity
        implements ListView.OnItemClickListener,
        FindBook.OnBookResultsListener
{
    private ArrayList<Book> books;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.example.fchataigner.pocket.R.layout.add_book_activity);
    }

    public void onAddItemClicked(View view)
    {
        TextView text_view = findViewById( com.example.fchataigner.pocket.R.id.search_text );

        String search_text = text_view.getText().toString();
        String[] search_strings = search_text.split(" ");

        FindBook bookFinder = new FindBook(this.getApplicationContext(), this);
        bookFinder.execute(search_strings);
    }

    @Override
    public void onItemClick( AdapterView<?> list, View view, int position, long id )
    {
        Book book = (Book) list.getItemAtPosition(position);

        Intent intent = new Intent();
        intent.putExtra("book", book );

        setResult( Activity.RESULT_OK, intent );
        finish();
    }

    public void onBookResults( ArrayList<Book> books )
    {
        this.books = books;

        TextView text = findViewById(com.example.fchataigner.pocket.R.id.text);
        text.setText( String.format("Found %d results", books.size() ) );

        ListView results = findViewById(com.example.fchataigner.pocket.R.id.search_results);
        results.setAdapter( new ItemAdapter<Book>( this.getApplicationContext(), this.books ) );
        results.setOnItemClickListener(this);
    }
}