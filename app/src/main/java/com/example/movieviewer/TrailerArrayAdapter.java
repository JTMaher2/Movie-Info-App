// TrailerAdapter.java
// Creates list of trailer objects
package com.example.movieviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class TrailerArrayAdapter extends ArrayAdapter<Trailer> {
    // class for reusing views as list items scroll off and onto the screen
    private static class ViewHolder {
        ImageView playButton;
        TextView trailerName;
    }

    // constructor to initialize superclass inherited members
    public TrailerArrayAdapter(Context context, List<Trailer> trailers) {
        super(context, -1, trailers);
    }

    // creates the custom views for the ListView's items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get Trailer object for this specified ListView position
        Trailer trailer = getItem(position);

        ViewHolder viewHolder; // object that references list item's views

        // check for reusable ViewHolder from a ListView item that scrolled offscreen; otherwise,
        // create a new ViewHolder
        if (convertView == null) { // no reusable ViewHolder, so create one
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.trailer_list_item, parent, false);
            viewHolder.playButton = (ImageView) convertView.findViewById(R.id.playButton);
            viewHolder.trailerName = (TextView) convertView.findViewById(R.id.trailerName);
            viewHolder.trailerName.setText(trailer.name);
            convertView.setTag(viewHolder);
        }

        return convertView;
    }
}
