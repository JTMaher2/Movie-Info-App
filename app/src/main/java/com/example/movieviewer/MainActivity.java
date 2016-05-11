// MainActivity.java
// Displays information about specified title
package com.example.movieviewer;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
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

    // configure Toolbar, ListView and FAB
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // create ArrayAdapter to bind movieList to the movieListView
        movieGridView = (GridView) findViewById(R.id.movieGridView);
        movieArrayAdapter = new MovieArrayAdapter(this, movieList);
        movieGridView.setAdapter(movieArrayAdapter);

        // configure FAB to hide keyboard and initiate web service request
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // get text from titleEditText and create web service URL
                    final Switch popularOrTop = (Switch) findViewById(R.id.popularOrTop);



                    if (popularOrTop != null) {
                        popularOrTop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                URL url;

                                if (isChecked)
                                    url = createURL(popularOrTop.getTextOn().toString());
                                else
                                    url = createURL(popularOrTop.getTextOff().toString());

                                // hide keyboard and initiate a GetMovieTask to download movie data from
                                // TheMovieDB.org in a separate thread
                                if (url != null) {
                                    GetMovieTask getMovieTask = new GetMovieTask();
                                    getMovieTask.execute(url);
                                } else {
                                    Snackbar.make(findViewById(R.id.coordinatorLayout), R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    // programmatically dismiss keyboard when user touches FAB
    private void dismissKeyboard(View view) {
        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
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

    // makes the REST web service call to get movie data,
    // and saves the data to a local HTML file
    private class GetMovieTask extends AsyncTask<URL, Void, JSONObject> {
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
                        Snackbar.make(findViewById(R.id.coordinatorLayout),
                                      R.string.read_error, Snackbar.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                    return new JSONObject(builder.toString());
                } else {
                    Snackbar.make(findViewById(R.id.coordinatorLayout),
                                  R.string.connect_error,
                                  Snackbar.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Snackbar.make(findViewById(R.id.coordinatorLayout),
                              R.string.connect_error, Snackbar.LENGTH_LONG).show();
                e.printStackTrace();
            } finally {
                connection.disconnect(); // close the HttpURLConnection
            }

            return null;
        }

        // process JSON response and update ListView
        @Override
        protected void onPostExecute(JSONObject movies) {
            convertJSONtoArrayList(movies); // repopulate movieList
            movieArrayAdapter.notifyDataSetChanged(); // rebind to ListView
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
                movieList.add(new Movie(movie.getString("title"), movie.getDouble("vote_average"),
                                        movie.getInt("vote_count"), movie.getDouble("popularity"),
                                        movie.getString("overview"),
                                        movie.getString("poster_path")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
