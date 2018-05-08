package com.example.fchataigner.mypocket;

import android.view.View;

public interface Adaptable
{
    void createListView( View view );
    void createDetailView( View view );

    int listLayout();
    int detailsLayout();
    int fileResource();
}