package com.example.fchataigner.pocket;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Book implements Parcelable, JSONable, Adaptable
{
    public class Builder implements JSONable.Builder<Book>
    {
        @Override
        public Book buildFromJSON( JSONObject json ) throws JSONException
        {
            Book book = new Book();
            book.readJSON(json);
            return book;
        }
    }

    public String isbn_13;
    public String isbn_10;
    public String title;
    public String author;
    public String thumbnail;
    public String language;
    public double averageRating;
    public int ratingsCount;
    public String description;
    public String link;
    public String publisher;
    public String date;

    private Book() {}

    @Override public int listLayout() { return com.example.fchataigner.pocket.R.layout.book_item; }
    @Override public int detailsLayout() { return com.example.fchataigner.pocket.R.layout.book_details; }
    @Override public int fileResource() { return com.example.fchataigner.pocket.R.string.books_file; }

    @Override
    public void createListView( View view )
    {
        ImageView image = view.findViewById(com.example.fchataigner.pocket.R.id.thumbnail);

        try { Picasso.get().load(thumbnail).into(image); }
        catch( Exception ex ) { image.setImageResource( com.example.fchataigner.pocket.R.drawable.ic_launcher_background ); }

        TextView title_view = view.findViewById(com.example.fchataigner.pocket.R.id.title);
        title_view.setText( title );

        TextView author_view = view.findViewById(com.example.fchataigner.pocket.R.id.author);
        author_view.setText(author);
    }

    @Override
    public void createDetailView( View view )
    {
        ImageView image = (ImageView) view.findViewById(com.example.fchataigner.pocket.R.id.thumbnail);
        Picasso.get().load(thumbnail).into(image);

        TextView title_view = (TextView) view.findViewById(com.example.fchataigner.pocket.R.id.title);
        title_view.setText(title);

        TextView author_view = (TextView) view.findViewById(com.example.fchataigner.pocket.R.id.author);
        author_view.setText(author);

        TextView ratings = (TextView) view.findViewById(com.example.fchataigner.pocket.R.id.ratings);
        ratings.setText( String.format("rating: %.1f (%d reviews)", averageRating, ratingsCount ) );

        TextView description_view = (TextView) view.findViewById(com.example.fchataigner.pocket.R.id.description);
        description_view.setText( description );

        TextView publisher_view = (TextView) view.findViewById(com.example.fchataigner.pocket.R.id.publisher);
        publisher_view.setText( publisher );

        TextView date_view = (TextView) view.findViewById(com.example.fchataigner.pocket.R.id.date);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try { int year = format.parse(date).getYear(); date_view.setText( year ); }
        catch( Exception ex ) { date_view.setText(date); }

        //Button link_view = (Button) view.findViewById(R.id.link);
        //link_view.setOnClickListener(this);
    }

    public void readJSON( JSONObject json ) throws JSONException
    {
        this.title = json.getString("title");
        this.author = json.getString("author");
        this.thumbnail = json.getString("thumbnail");
        this.isbn_10 = json.getString("isbn_10");
        this.isbn_13 = json.getString("isbn_13");
        this.language = json.getString("language");
        this.averageRating = json.getDouble("averageRating");
        this.ratingsCount = json.getInt("ratingsCount");
        this.description = json.getString("description");
        this.link = json.getString("link");
        this.publisher = json.getString("publisher");
        this.date = json.getString("date");
    }

    public JSONObject writeJSON() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put( "title", this.title );
        json.put( "author", this.author );
        json.put( "thumbnail", this.thumbnail );
        json.put( "isbn_10", this.isbn_10 );
        json.put( "isbn_13", this.isbn_13 );
        json.put( "language", this.language );
        json.put( "averageRating", this.averageRating );
        json.put( "ratingsCount", this.ratingsCount );
        json.put( "description", this.description );
        json.put( "link", this.link );
        json.put( "publisher", this.publisher );
        json.put( "date", this.date );

        return json;
    }

    private Book( Parcel parcel )
    {
        title = parcel.readString();
        author = parcel.readString();
        thumbnail = parcel.readString();
        isbn_13 = parcel.readString();
        isbn_10 = parcel.readString();
        language = parcel.readString();
        averageRating = parcel.readDouble();
        ratingsCount = parcel.readInt();
        description = parcel.readString();
        link = parcel.readString();
        publisher = parcel.readString();
        date = parcel.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel( Parcel parcel, int flags )
    {
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeString(thumbnail);
        parcel.writeString(isbn_13);
        parcel.writeString(isbn_10);
        parcel.writeString(language);
        parcel.writeDouble(averageRating);
        parcel.writeInt(ratingsCount);
        parcel.writeString(description);
        parcel.writeString(link);
        parcel.writeString(publisher);
        parcel.writeString(date);
    }

    static public Book fromGoogleJSON( JSONObject json ) throws JSONException
    {
        JSONObject volumeInfo = json.getJSONObject("volumeInfo");
        JSONObject accessInfo = json.getJSONObject("accessInfo");

        Book book = new Book();

        book.title       = volumeInfo.getString("title");
        book.language    = volumeInfo.getString("language");
        book.author      = volumeInfo.getJSONArray("authors").getString(0);
        book.description = volumeInfo.getString("description");
        book.publisher   = volumeInfo.getString("publisher");
        book.date        = volumeInfo.getString("publishedDate");
        book.link        = accessInfo.getString("webReaderLink");

        JSONArray ids = volumeInfo.getJSONArray("industryIdentifiers");

        book.isbn_13 = ids.getJSONObject(0).getString("identifier");
        book.isbn_10 = ids.getJSONObject(1).getString("identifier");

        try
        {
            JSONObject images = volumeInfo.getJSONObject("imageLinks");
            book.thumbnail = images.getString("thumbnail");
        }
        catch( Exception ex )
        {
            book.thumbnail = "";
            Log.w( "Book", "book=" + book.title + " has no thumbnail" );
        }

        try
        {
            book.averageRating = volumeInfo.getDouble("averageRating");
            book.ratingsCount = volumeInfo.getInt("ratingsCount");
            Log.w( "Book", "book=" + book.title + " has no ratings" );
        }
        catch( Exception ex )
        {
            book.averageRating = 0.0;
            book.ratingsCount = 0;
        }

        return book;
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>()
    {
        public Book createFromParcel(Parcel parcel) { return new Book(parcel); }
        public Book[] newArray(int size) { return new Book[size]; }
    };
};
