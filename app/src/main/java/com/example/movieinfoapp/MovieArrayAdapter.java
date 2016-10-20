// MovieArrayAdapter.java
// An ArrayAdapter for displaying a List<Movie>'s elements in a GridView
package com.example.movieinfoapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

class MovieArrayAdapter extends ArrayAdapter<Movie> {
    // class for reusing views as items scroll off and onto the screen
    private static class ViewHolder {
        ImageView posterImageView;
    }

    // constructor to initialize superclass inherited members
    public MovieArrayAdapter(Context context, List<Movie> movies) {
        super(context, -1, movies);
    }

    // creates the custom views for the GridView's items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get Movie object for this specified GridView position
        Movie movie = getItem(position);

        final ViewHolder viewHolder;

        // check for reusable ViewHolder from a GridView item that scrolled offscreen; otherwise,
        // create a new ViewHolder
        if (convertView == null) { // no reusable ViewHolder, so create one
            LayoutInflater inflater = LayoutInflater.from(getContext());

            convertView = inflater.inflate(R.layout.grid_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.posterImageView = (ImageView) convertView.findViewById(R.id.movieImageView);

            convertView.setTag(viewHolder);
        } else { // reuse existing ViewHolder stored as the grid item's tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Picasso.with(getContext()).load(movie.poster_url).into(viewHolder.posterImageView);

        return convertView; // return completed item to display
    }
}
