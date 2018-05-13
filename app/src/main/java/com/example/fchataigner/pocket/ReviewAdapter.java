package com.example.fchataigner.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends ArrayAdapter<Review>
{
    private Context context;
    private List<Review> reviews;

    public ReviewAdapter(@NonNull Context context, @NonNull ArrayList<Review> reviews )
    {
        super( context, 0 , reviews );

        this.context = context;
        this.reviews = reviews;
     }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        Review review = reviews.get(position);
        View view = convertView;

        if(view == null)
            view = LayoutInflater.from(context).inflate( R.layout.review_item, parent,false);

        TextView author = view.findViewById(R.id.author_name);
        author.setText( review.author_name );

        TextView time = view.findViewById(R.id.relative_time);
        time.setText( review.relative_time );

        TextView text = view.findViewById(R.id.text);
        text.setText( review.text );

        return view;
    }
}