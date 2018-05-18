package com.example.fchataigner.pocket.interfaces;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONable
{
    void readJSON(JSONObject json) throws JSONException;
    JSONObject writeJSON() throws JSONException;
    Builder getBuilder();

    interface Builder
    {
        Object buildFromJSON( JSONObject json ) throws JSONException;
    }
}