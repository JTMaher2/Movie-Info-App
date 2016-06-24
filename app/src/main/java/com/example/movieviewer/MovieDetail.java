package com.example.movieviewer;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MovieDetail extends AppCompatActivity {
    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final View linearLayout = findViewById(R.id.linearLayout);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_movie_detail);

        Bundle bundle = getIntent().getExtras();
        movie = bundle.getParcelable("com.example.movieviewer.Movie");

        // populate screen with movie information
        if (movie != null) {
            TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
            if (titleTextView != null)
                titleTextView.setText(movie.title);

            ImageView posterImageView = (ImageView) findViewById(R.id.posterImageView);
            Picasso.with(MovieDetail.this).load(movie.poster_url).into(posterImageView);

            TextView yearTextView = (TextView) findViewById(R.id.yearTextView);
            if (yearTextView != null)
                yearTextView.setText(movie.release_date.substring(0, 4));

            // do separate JSON request for runtime
            URL url = createURL();
            if (url != null) {
                GetMovieDetailsTask getMovieDetailsTask = new GetMovieDetailsTask();
                getMovieDetailsTask.execute(url);
            } else
                if (linearLayout != null)
                    Snackbar.make(linearLayout, R.string.invalid_url, Snackbar.LENGTH_LONG).show();

            TextView ratingTextView = (TextView) findViewById(R.id.ratingTextView);
            if (ratingTextView != null)
                ratingTextView.setText(getString(R.string.rating, movie.vote_average));

            TextView overviewTextView = (TextView) findViewById(R.id.overviewTextView);
            if (overviewTextView != null)
                overviewTextView.setText(movie.overview);
        }
    }

    // makes the REST web service call to get detailed movie data
    private class GetMovieDetailsTask extends AsyncTask<URL, Void, JSONObject> {
        View coordinatorLayout = findViewById(R.id.coordinatorLayout);

        @Override
        protected JSONObject doInBackground(URL... params) {
            HttpURLConnection connection = null;

            try {
                connection = (HttpURLConnection) params[0].openConnection();

                int response = connection.getResponseCode();

                if (response == HttpURLConnection.HTTP_OK) {
                    StringBuilder builder = new StringBuilder();

                    try (BufferedReader reader =
                                 new BufferedReader(new InputStreamReader(
                                         connection.getInputStream()))) {
                        String line;

                        while ((line = reader.readLine()) != null)
                            builder.append(line);
                    } catch (IOException e) {
                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.read_error,
                                    Snackbar.LENGTH_LONG).show();

                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                } else {
                    if (coordinatorLayout != null)
                        Snackbar.make(coordinatorLayout, R.string.connect_error,
                                Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                if (coordinatorLayout != null)
                    Snackbar.make(coordinatorLayout, R.string.connect_error, Snackbar.LENGTH_LONG)
                            .show();

                e.printStackTrace();
            } finally {
                if (connection != null)
                    connection.disconnect(); // close the HttpURLConnection
            }

            return null;
        }

        // display runtime
        @Override
        protected void onPostExecute(JSONObject details) {
            try {
                TextView runtimeTextView = (TextView) findViewById(R.id.runtimeTextView);
                if (runtimeTextView != null)
                    runtimeTextView.setText(getString(R.string.runtime, details.getInt("runtime")));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // create themoviedb.org details URL
    private URL createURL() {
        String apiKey = BuildConfig.TMDB_API_KEY,
                baseUrl = getString(R.string.movie_detail_url);

        try {
            // create URL for specified movie
            String urlString = baseUrl + movie.id + "?api_key=" + apiKey;

            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }
}