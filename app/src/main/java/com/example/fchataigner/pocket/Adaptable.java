package com.example.fchataigner.pocket;

import android.view.View;

public interface Adaptable
{
    void createListView( View view );
    void createDetailView( View view );

    int listLayout();
    int detailsLayout();
    int fileResource();
}