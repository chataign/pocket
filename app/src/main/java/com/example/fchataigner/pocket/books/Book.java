package com.example.fchataigner.pocket.books;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fchataigner.pocket.R;
import com.example.fchataigner.pocket.interfaces.Displayable;
import com.example.fchataigner.pocket.interfaces.JSONable;
import com.example.fchataigner.pocket.interfaces.Listable;
import com.example.fchataigner.pocket.interfaces.Shareable;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.Cloneable;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

public class Book implements Parcelable, JSONable, Listable, Displayable, Cloneable, Shareable
{
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

    public class Builder implements JSONable.Builder
    {
        public Object buildFromJSON( JSONObject json ) throws JSONException
        {
            Book book = new Book();
            book.readJSON(json);
            return book;
        }
    }

    public Book() {}

    @Override
    public String getShareableString() { return String.format(
            "Check out this book: \"%s\" by %s", this.title, this.author ); }

    @Override
    public int getDetailsLayout() { return R.layout.book_details; }

    @Override
    public int getItemLayout() { return R.layout.book_item; }

    @Override
    public int getFileResource() { return R.string.books_file; }

    @Override
    public Class<?> getAddItemClass() { return AddBookActivity.class; }

    String getMonth() throws ParseException
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime( format.parse(date) );
        int month = calendar.get(Calendar.MONTH);
        String[] months = new DateFormatSymbols().getMonths();
        return months[month];
    }

    int getYear() throws ParseException
    {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTime( format.parse(date) );
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public void createListView( View view )
    {
        ImageView image_view = view.findViewById(R.id.thumbnail);

        Picasso.get()
                .load(thumbnail)
                .error( R.drawable.ic_launcher_background )
                .resize(150, 200)
                .centerCrop()
                .into(image_view);

        TextView title_view = view.findViewById(R.id.title);
        title_view.setText( title );

        TextView author_view = view.findViewById(R.id.author);
        author_view.setText( author );
    }

    @Override
    public void createDetailsView( final Context context, View view )
    {
        ImageView image_view = (ImageView) view.findViewById(R.id.thumbnail);

        Picasso.get()
                .load(thumbnail)
                .resize(225, 300)
                .centerCrop()
                .into(image_view);

        TextView title_view = (TextView) view.findViewById(R.id.title);
        title_view.setText(title);

        TextView author_view = (TextView) view.findViewById(R.id.author);
        author_view.setText(author);

        TextView ratings = (TextView) view.findViewById(R.id.ratings);
        ratings.setText( String.format("%.1f (from %d reviews)", averageRating, ratingsCount ) );

        TextView description_view = (TextView) view.findViewById(R.id.description);
        description_view.setText( description );

        TextView publisher_view = (TextView) view.findViewById(R.id.publisher);
        publisher_view.setText( publisher );

        TextView date_view = (TextView) view.findViewById(R.id.date);

        try
        {
            date_view.setText( String.format("%s %d", getMonth(), getYear() ) );
        }
        catch( ParseException ex )
        {
            date_view.setText(date);
            Log.w( "Book", "failed to parse date, error=" + ex.getMessage() );
        }

        Button button = (Button) view.findViewById(R.id.button);
        button.setOnClickListener( new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link) );
                context.startActivity(intent);
            }
        } );
    }

    @Override
    public JSONable.Builder getBuilder() { return new Book.Builder(); }

    @Override
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

    @Override
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
        }

        try
        {
            book.averageRating = volumeInfo.getDouble("averageRating");
            book.ratingsCount = volumeInfo.getInt("ratingsCount");
        }
        catch( Exception ex )
        {
            book.averageRating = 0.0;
            book.ratingsCount = 0;
        }

        return book;
    }

    @Override
    public boolean equals(Object obj)
    {
        final Book book = (Book) obj;
        return book != null && book.isbn_10.equals(this.isbn_10);
    }

    public static final Parcelable.Creator<Book> CREATOR = new Parcelable.Creator<Book>()
    {
        public Book createFromParcel(Parcel parcel) { return new Book(parcel); }
        public Book[] newArray(int size) { return new Book[size]; }
    };

    public static final Comparator<Book> OrderByRating = new Comparator<Book>()
    {
        public int compare( Book book1, Book book2 )
        {
            if ( book1.ratingsCount < book2.ratingsCount ) return 1;
            else if ( book1.ratingsCount == book2.ratingsCount ) return 0;
            return -1;
        }
    };
};
