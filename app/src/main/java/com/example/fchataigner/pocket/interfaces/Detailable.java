package com.example.fchataigner.pocket.interfaces;

import android.content.Context;
import android.view.View;

public interface Detailable
{
    int getDetailsLayout();
    void createDetailsView( Context context, View view );
}