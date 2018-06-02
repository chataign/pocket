package com.example.fchataigner.pocket.books;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fchataigner.pocket.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;

public class BookDetailsActivity
        extends AppCompatActivity
{
    private static String TAG = "BookDetailsActivity";
    private static int SEARCH_BY_AUTHOR = 1;

    private static int THUMBNAIL_WIDTH = 225;
    private static int THUMBNAIL_HEIGHT = 300;

    private Book book=null;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        setTheme(R.style.AppTheme_NoActionBar);
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        String item_extra = getString(R.string.item_extra);

        if ( extras == null || !extras.containsKey(item_extra) )
        {
            Log.e( TAG, "No book data in intent" );
            finish();
        }

        try
        {
            book = (Book) extras.getParcelable(item_extra);
        }
        catch( Exception e )
        {
            Log.e( TAG, "Invalid book data in intent, error=" + e.getMessage() );
            finish();
        }

        View view = getLayoutInflater().inflate( R.layout.book_details, null );

        ImageView image_view = view.findViewById(R.id.thumbnail);

        Picasso.get()
                .load(book.thumbnail)
                .resize(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                .centerCrop()
                .into(image_view);

        TextView title_view = view.findViewById(R.id.title);
        title_view.setText(book.title);

        TextView author_view = view.findViewById(R.id.author);
        author_view.setText(book.author);

        final Intent search_by_author = new Intent( this, FindBookActivity.class );
        search_by_author.putExtra( FindBookActivity.QUERY, book.author );

        author_view.setOnClickListener( new TextView.OnClickListener() {
            @Override
            public void onClick( View view ) {
            startActivityForResult( search_by_author, SEARCH_BY_AUTHOR );
            }
        });

        TextView ratings = view.findViewById(R.id.ratings);
        ratings.setText( String.format("%.1f (from %d reviews)", book.averageRating, book.ratingsCount ) );

        TextView description_view = view.findViewById(R.id.description);
        description_view.setText( book.description );

        TextView publisher_view = view.findViewById(R.id.publisher);
        publisher_view.setText( book.publisher );

        TextView date_view = view.findViewById(R.id.date);

        try
        {
            date_view.setText( String.format("%s %d", book.getMonth(), book.getYear() ) );
        }
        catch( ParseException ex )
        {
            date_view.setText(book.date);
            Log.w( "Book", "failed to parse date, error=" + ex.getMessage() );
        }

        Button link_button = view.findViewById(R.id.link_button);
        link_button.setOnClickListener( new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(book.link) );
                getApplicationContext().startActivity(intent);
            }
        } );

        /*
        GetBookAuthor.Listener author_listener = new GetBookAuthor.Listener()
        {
            @Override
            public void onResults(final ArrayList<BookAuthor> authors )
            {
                for ( BookAuthor author : authors )
                    Log.i( "BookAuthor", "read author: name=" + author.name + " id=" + author.id );
            }
        };

        GetBookAuthor get_authors = new GetBookAuthor( context, author_listener );
        get_authors.execute( this.author );
        */

        setContentView(view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Book details");
        setSupportActionBar( toolbar );
    }

    @Override
    public void onActivityResult( int request, int result, Intent intent )
    {
        if (intent == null)
            return;

        if (request == SEARCH_BY_AUTHOR && result == Activity.RESULT_OK)
        {
            setResult( Activity.RESULT_OK, intent );
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.book_details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menu_item)
    {
        switch (menu_item.getItemId())
        {
            case R.id.action_share:
                Intent share_intent = new Intent( Intent.ACTION_SEND );
                share_intent.setAction(Intent.ACTION_SEND);
                share_intent.setType("text/plain");
                share_intent.putExtra( Intent.EXTRA_TEXT, book.getShareableString() );
                startActivity( Intent.createChooser( share_intent, "share book" )  );
                finish();
                return true;

            case R.id.action_add:
                setResult( Activity.RESULT_OK, getIntent() );
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(menu_item);
        }
    }
}
