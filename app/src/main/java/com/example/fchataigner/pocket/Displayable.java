package com.example.fchataigner.pocket;

import android.content.Context;
import android.view.View;

public interface Displayable
{
    void createListView( View view );
    void createDetailsView( Context context, View view );

    int getItemLayout();
    int getDetailsLayout();
    int getFileResource();

    Class<?> getAddItemClass();
}