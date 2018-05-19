package com.example.fchataigner.pocket.places;

import com.example.fchataigner.pocket.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PlaceDetails
{
    public String place_id;
    public String google_maps_url;
    public String website_url;
    public String name;
    public String phone_number;
    public String address;
    public ArrayList<PlaceReview> reviews = new ArrayList<>();

    public static PlaceDetails fromGoogleJSON( JSONObject json ) throws JSONException
    {
        PlaceDetails details = new PlaceDetails();

        details.place_id = json.getString("place_id");
        details.name = json.getString("name");
        details.google_maps_url = json.getString("url");
        details.website_url = json.optString( "website","#");
        details.phone_number = json.optString("formatted_phone_number","");
        details.address = json.getString("formatted_address");

        JSONArray json_reviews = json.getJSONArray("reviews");

        for ( int i=0; i< json_reviews.length(); ++i )
            details.reviews.add( PlaceReview.fromGoogleJSON( json_reviews.getJSONObject(i) ) );

        return details;
    }
}

