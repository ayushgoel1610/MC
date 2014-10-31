package com.iiitd.mcproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Vedant on 30-10-2014.
 */
public class TopicList extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String> topics;
    private final ArrayList<String> imageId;

    public TopicList(Activity context,
                     ArrayList<String> topics, ArrayList<String> imageId) {
        super(context, R.layout.topic_list_row, topics);
        this.context = context;
        this.topics = topics;
        this.imageId = imageId;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.topic_list_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.name);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
        txtTitle.setText(topics.get(position));
        String imageUrl=null;
        if(position<imageId.size())
            imageUrl = imageId.get(position);

        if(imageUrl==null) {
            imageView.setImageResource(R.drawable.ic_launcher);
        }else {
            Picasso.with(context).load(imageUrl).error(R.drawable.ic_launcher).into(imageView);
        }
        return rowView;
    }
}
