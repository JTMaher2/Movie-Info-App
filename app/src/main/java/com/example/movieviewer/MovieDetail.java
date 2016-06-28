package com.example.movieviewer;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MovieDetail extends AppCompatActivity {
    private Movie movie;

    // List of Trailer objects representing the search query
    private ArrayList<Trailer> trailerList = new ArrayList<>();

    // ArrayAdapter for binding Trailer objects to a ListView
    private TrailerArrayAdapter trailerArrayAdapter;

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
            URL url = createURL("details");
            if (url != null) {
                GetMovieDataTask getMovieDetailsTask = new GetMovieDataTask();
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

            ListView trailerListView = (ListView) findViewById(R.id.trailerListView);
            trailerArrayAdapter = new TrailerArrayAdapter(MovieDetail.this, trailerList);
            if (trailerListView != null) {
                trailerListView.setAdapter(trailerArrayAdapter);

                // open video when clicked
                trailerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Trailer trailer = (Trailer) adapterView.getItemAtPosition(i);
                        startActivity(new Intent(Intent.ACTION_VIEW,
                                                 Uri.parse(trailer.trailerURL)));
                    }
                });
            }

            // do separate JSON request for trailers
            url = createURL("trailers");
            if (url != null) {
                GetMovieDataTask getMovieTrailersTask = new GetMovieDataTask();
                getMovieTrailersTask.execute(url);
            } else
                if (linearLayout != null)
                    Snackbar.make(linearLayout, R.string.invalid_url, Snackbar.LENGTH_LONG).show();
        }
    }

    // makes the REST web service call to get detailed movie data
    private class GetMovieDataTask extends AsyncTask<URL, Void, JSONObject> {
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
        protected void onPostExecute(JSONObject data) {
            try {
                // if details
                if (data.has("runtime")) {
                    TextView runtimeTextView = (TextView) findViewById(R.id.runtimeTextView);
                    if (runtimeTextView != null)
                        runtimeTextView.setText(getString(R.string.runtime, data.getInt("runtime")));
                } else { // trailers
                    convertJSONtoArrayList(data);
                    trailerArrayAdapter.notifyDataSetChanged(); // rebind to ListView
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    // create Trailer objects from JSONObject containing the trailer list
    private void convertJSONtoArrayList(JSONObject trailers) {
        trailerList.clear(); // clear old trailer data

        try {
            // get trailer list's "results" JSONArray
            JSONArray list = trailers.getJSONArray("results");

            // convert each element of list to a Trailer object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject trailer = list.getJSONObject(i); // get one trailer's data

                // add new Trailer object to trailerList
                trailerList.add(new Trailer(trailer.getString("name"), trailer.getString("site"),
                                            trailer.getString("key")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // create themoviedb.org details URL
    private URL createURL(String type) {
        String apiKey = BuildConfig.TMDB_API_KEY,
                baseUrl = getString(R.string.movie_detail_url);

        try {
            // create URL for specified movie
            String urlString;

            switch (type) {
                case "details":
                    urlString = baseUrl + movie.id + "?api_key=" + apiKey;
                    break;
                case "trailers":
                    urlString = baseUrl + movie.id + "/videos?api_key=" + apiKey;
                    break;
                default:
                    throw new RuntimeException("unknown URL type");
            }

            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }
}