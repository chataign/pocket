package com.example.fchataigner.pocket;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

public class Place implements Parcelable, JSONable, Listable, Displayable, Cloneable
{
    public String place_id;
    public String name;
    public double latitude;
    public double longitude;
    public String vicinity;
    public String icon;
    public ArrayList<String> types = new ArrayList<String>();

    public Place() {}

    public static Place fromJSON( JSONObject json ) throws JSONException
    {
        Place place = new Place();
        place.readJSON(json);
        return place;
    }

    @Override
    public int getDetailsLayout() { return R.layout.place_details; }

    @Override
    public int getItemLayout() { return R.layout.place_item; }

    @Override
    public int getAddActivityLayout() { return R.layout.add_place_activity; }

    @Override
    public int getFileResource() { return R.string.places_file; }

    @Override
    public Class<?> getAddItemClass() { return AddPlaceActivity.class; }

    @Override
    public void createListView( View view )
    {
        ImageView image_view = view.findViewById(R.id.icon);

        Picasso.get()
                .load(icon)
                .error( R.drawable.ic_launcher_background )
                .resize(100, 100)
                .centerCrop()
                .into(image_view);

        TextView name_field = view.findViewById(R.id.name);
        name_field.setText( name );

        TextView vicinity_field = view.findViewById(R.id.vicinity);
        vicinity_field.setText( vicinity );
    }

    @Override
    public void createDetailsView( final Context context, final View view )
    {
        ImageView image_view = (ImageView) view.findViewById(R.id.icon);

        Picasso.get()
                .load(icon)
                .error( R.drawable.ic_launcher_background )
                .resize(100, 100)
                .centerCrop()
                .into(image_view);

        TextView name_view = (TextView) view.findViewById(R.id.name);
        name_view.setText(name);

        TextView vicinity_view = (TextView) view.findViewById(R.id.vicinity);
        vicinity_view.setText(vicinity);

        TextView types_field = view.findViewById(R.id.types);
        types_field.setText( types.toString() );

        GetPlaceDetails.OnDetailsReceived details_received = new GetPlaceDetails.OnDetailsReceived()
        {
            @Override
            public void onDetailsReceived(final PlaceDetails details)
            {
                final Button website = view.findViewById(R.id.website);
                website.setOnClickListener( new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(details.website_url) );
                        context.startActivity(intent);
                    }
                } );

                final Button google_maps = view.findViewById(R.id.google_maps);
                google_maps.setOnClickListener( new Button.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(details.google_maps_url) );
                        context.startActivity(intent);
                    }
                } );

                TextView phone_number = view.findViewById(R.id.phone_number);
                phone_number.setText( details.phone_number );

                TextView reviews_header = view.findViewById(R.id.reviews_header);
                reviews_header.setText( String.format( "%d reviews", details.reviews.size() ) );

                LinearLayout reviews_layout = (LinearLayout) view.findViewById(R.id.reviews);
                LayoutInflater inflater = LayoutInflater.from(context);

                for ( Review review : details.reviews)
                {
                    View item_view = inflater.inflate(R.layout.review_item, reviews_layout, false);
                    review.populateView(item_view);
                    reviews_layout.addView(item_view);
                }
            }
        };

        GetPlaceDetails get_place_details = new GetPlaceDetails( context, details_received );
        get_place_details.execute( this );

    }

    @Override
    public Place buildFromJSON( JSONObject json ) throws JSONException
    {
        Place place = new Place();
        place.readJSON(json);
        return place;
    }

    @Override
    public void readJSON( JSONObject json ) throws JSONException
    {
        this.place_id = json.getString("place_id");
        this.latitude = json.getDouble("latitude");
        this.longitude = json.getDouble("longitude");
        this.name = json.getString("name");
        this.vicinity = json.getString("vicinity");
        this.icon = json.getString("icon");

        JSONArray json_types = json.getJSONArray("types");

        for ( int i=0; i< json_types.length(); ++i )
        {
            String type = json_types.getString(i);
            this.types.add(type);
        }
    }

    @Override
    public JSONObject writeJSON() throws JSONException
    {
        JSONArray json_types = new JSONArray();
        for ( String type : types ) json_types.put(type);

        JSONObject json = new JSONObject();
        json.put( "place_id", place_id );
        json.put( "latitude", latitude );
        json.put( "longitude", longitude );
        json.put( "name", name );
        json.put( "vicinity", vicinity );
        json.put( "icon", icon );
        json.put( "types", json_types );

        return json;
    }

    private Place(Parcel parcel )
    {
        place_id = parcel.readString();
        latitude = parcel.readDouble();
        longitude = parcel.readDouble();
        name = parcel.readString();
        vicinity = parcel.readString();
        icon = parcel.readString();
        parcel.readStringList(types);

    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel( Parcel parcel, int flags )
    {
        parcel.writeString(place_id);
        parcel.writeDouble(latitude);
        parcel.writeDouble(longitude);
        parcel.writeString(name);
        parcel.writeString(vicinity);
        parcel.writeString(icon);
        parcel.writeStringList(types);
    }

    static public Place fromGoogleJSON(JSONObject json ) throws JSONException
    {
        JSONObject location = json.getJSONObject("geometry").getJSONObject("location");

        Place place = new Place();
        place.place_id = json.getString("place_id");
        place.latitude = location.getDouble("lat");
        place.longitude = location.getDouble("lng");
        place.name = json.getString("name");
        place.vicinity = json.getString("vicinity");
        place.icon = json.getString("icon");

        JSONArray json_types = json.getJSONArray("types");
        for ( int i=0; i< json_types.length(); ++i ) place.types.add( json_types.getString(i) );

        return place;
    }

    public static final Creator<Place> CREATOR = new Creator<Place>()
    {
        public Place createFromParcel(Parcel parcel) { return new Place(parcel); }
        public Place[] newArray(int size) { return new Place[size]; }
    };

    @Override
    public boolean equals(Object obj)
    {
        final Place place = (Place) obj;
        return place != null && place.place_id.equals(this.place_id);
    }
};
