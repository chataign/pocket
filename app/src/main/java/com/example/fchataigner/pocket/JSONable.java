package com.example.fchataigner.pocket;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONable
{
    void readJSON(JSONObject json) throws JSONException;
    JSONObject writeJSON() throws JSONException;
    Object buildFromJSON( JSONObject json ) throws JSONException;
}