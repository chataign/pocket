package com.example.fchataigner.pocket;

import android.content.Context;
import android.view.View;

public interface Displayable
{
    void createDetailsView( Context context, View view );

    int getItemLayout();
    int getDetailsLayout();
    int getFileResource();
    int getAddActivityLayout();

    Class<?> getAddItemClass();
}