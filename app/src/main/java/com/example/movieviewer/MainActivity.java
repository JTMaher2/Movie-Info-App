// MainActivity.java
// Displays a list of popular or top rated movies
package com.example.movieviewer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

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
    // key for reading data from SharedPreferences
    public static final String DISPLAY_OPTIONS = "pref_displayOptions";

    public static final String FAVORITES = "Favorites";

    // List of Movie objects representing the search query
    private ArrayList<Movie> movieList = new ArrayList<>();

    // ArrayAdapter for binding Movie objects to a GridView
    private MovieArrayAdapter movieArrayAdapter;
    private GridView movieGridView; // displays movie info

    private FavoritesDataSource favoritesDataSource;

    private boolean displayFavorites;

    private View coordinatorLayout;

    // configure Toolbar and GridView
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        // create ArrayAdapter to bind movieList to the movieGridView
        movieArrayAdapter = new MovieArrayAdapter(this, movieList);

        movieGridView = (GridView) findViewById(R.id.movieGridView);

        if (movieGridView != null)
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

        // for retrieving favorites from database
        favoritesDataSource = new FavoritesDataSource(MainActivity.this);
        favoritesDataSource.open();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        URL url;

        String displayOption = prefs.getString(DISPLAY_OPTIONS, null);

        if (displayOption != null) {
            if (!displayOption.equals(FAVORITES)) { // not favorites, so okay to perform JSON request
                displayFavorites = false;

                url = createURL(displayOption);

                // initiate a GetMovieTask to download movie data from TheMovieDB.org in a
                // separate thread
                if (url != null) {
                    GetMovieTask getMovieTask = new GetMovieTask();
                    getMovieTask.execute(url);
                } else if (coordinatorLayout != null) {
                    Snackbar.make(coordinatorLayout, R.string.invalid_url, Snackbar.LENGTH_LONG).show();
                }
            } else { // favorites, so do not perform JSON request, and instead access the database
                displayFavorites = true;

                movieList.clear();
                movieList.addAll(favoritesDataSource.getAllFavorites());

                movieArrayAdapter.notifyDataSetChanged(); // rebind to GridView

                movieGridView.smoothScrollToPosition(0); // scroll to top
            }
        }
    }

    // create themoviedb.org web service URL using title
    private URL createURL(String displayOption) {
        String apiKey = BuildConfig.TMDB_API_KEY,
               baseUrl = getString(R.string.web_service_url);

        // in order to be compatible with API,
        // string must be lower case and have underscores instead of spaces
        displayOption = displayOption.toLowerCase().replace(' ', '_');

        try {
            // create URL for specified movie
            String urlString = baseUrl + URLEncoder.encode(displayOption, "UTF-8") + "?api_key=" + apiKey;

            return new URL(urlString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // URL was malformed
    }

    // makes the REST web service call to get movie data
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
        protected void onPostExecute(JSONObject movieInfo) {
            convertJSONtoArrayList(movieInfo); // repopulate movieList
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
                movieList.add(new Movie(movie.getInt("id"), movie.getString("title"),
                                        movie.getString("poster_path"), movie.getString("release_date"),
                                        movie.getDouble("vote_average"), movie.getString("overview")));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // if the user was previously on the favorites selection, update it
        if (displayFavorites) {
            movieList.clear();
            movieList.addAll(favoritesDataSource.getAllFavorites());

            movieArrayAdapter.notifyDataSetChanged(); // rebind to GridView

            movieGridView.smoothScrollToPosition(0); // scroll to top
        }
    }

    // show menu if app is running on a phone or a portrait-oriented tablet
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // get the device's current orientation
        int orientation = getResources().getConfiguration().orientation;

        // display the app's menu only in portrait orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        else
            return false;
    }

    // displays the SettingsActivity when running on a phone
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);

        return super.onOptionsItemSelected(item);
    }
}
