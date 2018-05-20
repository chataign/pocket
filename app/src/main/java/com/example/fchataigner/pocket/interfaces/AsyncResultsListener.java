package com.example.fchataigner.pocket.interfaces;

import java.util.List;

public interface AsyncResultsListener<Item>
{
    void onNewResults(List<Item> items);
    void onPostExecute();
}