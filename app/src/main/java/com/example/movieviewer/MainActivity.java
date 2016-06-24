// MainActivity.java
// Displays information about specified title
package com.example.movieviewer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    // List of Movie objects representing the search query
    private ArrayList<Movie> movieList = new ArrayList<>();

    // ArrayAdapter for binding Movie objects to a ListView
    private MovieArrayAdapter movieArrayAdapter;
    private GridView movieGridView; // displays movie info

    // configure Toolbar and GridView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final View coordinatorLayout = findViewById(R.id.coordinatorLayout);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create switch to toggle between popular and top-rated
        final Switch popularOrTop = (Switch) findViewById(R.id.popularOrTop);

        // create ArrayAdapter to bind movieList to the movieGridView
        movieGridView = (GridView) findViewById(R.id.movieGridView);
        movieArrayAdapter = new MovieArrayAdapter(this, movieList);
        movieGridView.setAdapter(movieArrayAdapter);
        movieGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Movie movie = (Movie) adapterView.getItemAtPosition(i);
                Intent intent = new Intent(view.getContext(), MovieDetail.class);
                intent.putExtra("com.example.movieviewer.Movie", movie);
                startActivity(intent);
            }
        });

        if (popularOrTop != null) {
            popularOrTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    URL url;

                    if (isChecked)
                        url = createURL(popularOrTop.getTextOn().toString());
                    else
                        url = createURL(popularOrTop.getTextOff().toString());

                    // initiate a GetMovieTask to download movie data from TheMovieDB.org in a
                    // separate thread
                    if (url != null) {
                        GetMovieTask getMovieTask = new GetMovieTask();
                        getMovieTask.execute(url);
                    } else
                        if (coordinatorLayout != null)
                            Snackbar.make(coordinatorLayout, R.string.invalid_url,
                                    Snackbar.LENGTH_LONG).show();
                }
            });
        }
    }

    // create themoviedb.org web service URL using title
    private URL createURL(String title) {
        String apiKey = BuildConfig.TMDB_API_KEY,
               baseUrl = getString(R.string.web_service_url);

        try {
            // create URL for specified movie
            String urlString = baseUrl + URLEncoder.encode(title, "UTF-8") + "?api_key=" + apiKey;

            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }

    // makes the REST web service call to get movie data
    private class GetMovieTask extends AsyncTask<URL, Void, JSONObject> {
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

        // process JSON response and update GridView
        @Override
        protected void onPostExecute(JSONObject movies) {
            convertJSONtoArrayList(movies); // repopulate movieList
            movieArrayAdapter.notifyDataSetChanged(); // rebind to GridView
            movieGridView.smoothScrollToPosition(0); // scroll to top
        }
    }

    // create Movie objects from JSONObject containing the movie list
    private void convertJSONtoArrayList(JSONObject movies) {
        movieList.clear(); // clear old movie data

        try {
            // get movie list's "results" JSONArray
            JSONArray list = movies.getJSONArray("results");

            // convert each element of list to a Movie object
            for (int i = 0; i < list.length(); ++i) {
                JSONObject movie = list.getJSONObject(i); // get one movie's data

                // add new Movie object to movieList
                movieList.add(new Movie(movie.getInt("id"), movie.getDouble("vote_average"),
                                        movie.getString("title"), movie.getString("poster_path"),
                                        movie.getString("release_date"),
                                        movie.getString("overview")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
