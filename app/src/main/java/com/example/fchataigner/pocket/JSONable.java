package com.example.fchataigner.pocket;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.io.InputStream;

public interface JSONable
{
    void readJSON(JSONObject json) throws JSONException;
    JSONObject writeJSON() throws JSONException;

    public interface Builder<T>
    {
        public T buildFromJSON(JSONObject json) throws JSONException;
    }
}