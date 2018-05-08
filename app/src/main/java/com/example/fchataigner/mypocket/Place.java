package com.example.fchataigner.mypocket;

import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Place implements Parcelable, JSONable, Adaptable
{
    public String id;
    public String name;
    public String vicinity;
    public String icon;
    public ArrayList<String> types = new ArrayList<String>();

    private Place() {}

    @Override public int listLayout() { return R.layout.place_item; }
    @Override public int detailsLayout() { return 0; }
    @Override public int fileResource() { return R.string.places_file; }

    public void createListView( View view )
    {
        ImageView image_view = view.findViewById(R.id.icon);

        try { Picasso.get().load(icon).into(image_view); }
        catch( Exception ex ) { image_view.setImageResource( R.drawable.ic_launcher_background ); }

        TextView name_field = view.findViewById(R.id.name);
        name_field.setText( name );

        TextView type_field = view.findViewById(R.id.type);
        String type = types.get(0);
        if ( type != null ) type_field.setText( type );
    }

    @Override
    public void createDetailView( View view ) {}

    public void readJSON( JSONObject json ) throws JSONException
    {
        this.id = json.getString("id");
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

    public JSONObject writeJSON() throws JSONException
    {
        JSONArray json_types = new JSONArray();
        for ( String type : types ) json_types.put(type);

        JSONObject json = new JSONObject();
        json.put( "id", id );
        json.put( "name", name );
        json.put( "vicinity", vicinity );
        json.put( "icon", icon );
        json.put( "types", json_types );

        return json;
    }

    private Place(Parcel parcel )
    {
        id = parcel.readString();
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
        parcel.writeString(id);
        parcel.writeString(name);
        parcel.writeString(vicinity);
        parcel.writeString(icon);
        parcel.writeStringList(types);
    }

    static public Place fromGoogleJSON(JSONObject json ) throws JSONException
    {
        Place place = new Place();
        place.id = json.getString("id");
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
};
