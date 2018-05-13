package com.example.fchataigner.pocket;

import org.json.JSONException;
import org.json.JSONObject;

public class Review
{
    public String author_name;
    public String author_url;
    public String language;
    public double rating;
    public String profile_photo_url;
    public String relative_time;
    public String time;
    public String text;

    public static Review fromGoogleJSON( JSONObject json ) throws JSONException
    {
        Review review = new Review();
        review.author_name = json.getString("author_name");
        review.author_url = json.getString("author_url");
        review.language = json.getString("language");
        review.profile_photo_url = json.getString("profile_photo_url");
        review.rating = json.getDouble("rating");
        review.relative_time = json.getString("relative_time_description");
        review.text = json.getString("text");
        review.time = json.getString("time");
        return review;
    }
}

