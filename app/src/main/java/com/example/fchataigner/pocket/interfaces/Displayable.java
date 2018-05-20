package com.example.fchataigner.pocket.interfaces;

import android.content.Context;
import android.view.View;

public interface Displayable
{
    void createDetailsView( Context context, View view );

    int getDetailsLayout();
    int getFileResource();

    Class<?> getAddItemClass();
}