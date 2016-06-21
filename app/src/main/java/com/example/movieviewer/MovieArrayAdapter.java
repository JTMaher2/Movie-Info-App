// MovieArrayAdapter.java
// An ArrayAdapter for displaying a List<Movie>'s elements in a ListView
package com.example.movieviewer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

class MovieArrayAdapter extends ArrayAdapter<Movie> {
    // class for reusing views as list items scroll off and onto the screen
    private static class ViewHolder {
        ImageView posterImageView;
    }

    // stores already downloaded Bitmaps for reuse
    private HashMap<String, Bitmap> bitmaps = new HashMap<>();

    // constructor to initialize superclass inherited members
    public MovieArrayAdapter(Context context, List<Movie> movies) {
        super(context, -1, movies);
    }

    // creates the custom views for the ListView's items
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // get Movie object for this specified ListView position
        Movie movie = getItem(position);

        ViewHolder viewHolder; // object that references list item's views

        // check for reusable ViewHolder from a ListView item that scrolled offscreen; otherwise,
        // create a new ViewHolder
        if (convertView == null) { // no reusable ViewHolder, so create one
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.list_item, parent, false);
            viewHolder.posterImageView = (ImageView) convertView.findViewById(R.id.movieImageView);
            convertView.setTag(viewHolder);
        } else { // reuse existing ViewHolder stored as the list item's tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // if movie poster icon already downloaded, use it;
        // otherwise, download icon in a separate thread
        if (bitmaps.containsKey(movie.iconURL)) {
            viewHolder.posterImageView.setImageBitmap(bitmaps.get(movie.iconURL));
        } else {
            // download and display movie poster image
            new LoadImageTask(viewHolder.posterImageView).execute(movie.iconURL);
        }

        return convertView; // return completed list item to display
    }

    // AsyncTask to load weather condition icons in a separate thread
    private class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private ImageView imageView; // displays the thumbnail

        // store ImageView on which to set the downloaded Bitmap
        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        // load image; params[0] is the String URL representing the image
        @Override
        protected Bitmap doInBackground(String... params) {
            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]); // create URL for image

                // open an HttpURLConnection, get its InputStream, and download
                // the image
                connection = (HttpURLConnection) url.openConnection();

                try (InputStream inputStream = connection.getInputStream()) {
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0], bitmap); // cache for later use
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return bitmap;
        }

        // set movie poster image in list item
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
    }
}
