package com.example.fchataigner.pocket;

import android.view.View;

public interface Displayable
{
    void createListView( View view );
    void createDetailsView( View view );

    int getItemLayout();
    int getDetailsLayout();
    int getFileResource();

    Class<?> getAddItemClass();
}