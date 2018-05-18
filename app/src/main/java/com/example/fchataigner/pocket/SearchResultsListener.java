package com.example.fchataigner.pocket;

import java.util.ArrayList;

public interface SearchResultsListener<Item>
{
    void onResults(ArrayList<Item> items );
}