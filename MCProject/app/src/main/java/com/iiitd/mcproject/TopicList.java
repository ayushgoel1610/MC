package com.iiitd.mcproject;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iiitd.mcproject.TabFragments.TopicObject;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Vedant on 30-10-2014.
 */
public class TopicList extends ArrayAdapter<TopicObject> {
    private final Activity context;
    private final ArrayList<TopicObject> topics;
//    private final ArrayList<String> imageId;
//    private final ArrayList<String> categories;

    public TopicList(Activity context,
                     ArrayList<TopicObject> topics) {
        super(context, R.layout.topic_list_row, topics);
        this.context = context;
        this.topics = topics;
//        this.imageId = imageId;
//        this.categories= categories;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView= inflater.inflate(R.layout.topic_list_row, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.name);
        TextView txtCategory = (TextView) rowView.findViewById(R.id.category);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
        txtTitle.setText(topics.get(position).getName());
        txtCategory.setText(topics.get(position).getCategory());
        String imageUrl=null;
        if(position<topics.size())
            imageUrl = topics.get(position).getImage();

//        Log.v("topic list",topics.get(position)+" "+imageUrl);
        final Resources res = context.getResources();
        final int tileSize = res.getDimensionPixelSize(R.dimen.letter_tile_size);

        final LetterTileProvider tileProvider = new LetterTileProvider(context);
        final Bitmap letterTile = tileProvider.getLetterTile(topics.get(position).getName(), topics.get(position).getName(), tileSize, tileSize);

        if(imageUrl=="") {
            imageView.setImageDrawable(new BitmapDrawable(context.getResources(), letterTile));
        }else {
            Drawable db=new BitmapDrawable(context.getResources(), letterTile);
            Picasso.with(context).load(imageUrl).placeholder(db).error(db).into(imageView);
        }
        return rowView;
    }
}
