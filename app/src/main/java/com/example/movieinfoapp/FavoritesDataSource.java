// FavoritesDataSource.java
// allows in which activities to interact with database
package com.example.movieinfoapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class FavoritesDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {
            MySQLiteHelper.COLUMN_ID, MySQLiteHelper.COLUMN_TITLE,
            MySQLiteHelper.COLUMN_POSTER_URL, MySQLiteHelper.COLUMN_RELEASE_DATE,
            MySQLiteHelper.COLUMN_RATING, MySQLiteHelper.COLUMN_OVERVIEW };

    public FavoritesDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public Movie createFavorite(int id, String title, String posterURL, String releaseDate,
                                double rating, String overview) {
        ContentValues values = new ContentValues();

        values.put(MySQLiteHelper.COLUMN_ID, id);
        values.put(MySQLiteHelper.COLUMN_TITLE, title);
        values.put(MySQLiteHelper.COLUMN_POSTER_URL, posterURL);
        values.put(MySQLiteHelper.COLUMN_RELEASE_DATE, releaseDate);
        values.put(MySQLiteHelper.COLUMN_RATING, rating);
        values.put(MySQLiteHelper.COLUMN_OVERVIEW, overview);

        long insertId = database.insert(MySQLiteHelper.TABLE_FAVORITES, null, values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_FAVORITES, allColumns,
                                       MySQLiteHelper.COLUMN_ID + " = " + insertId, null, null,
                                       null, null);
        cursor.moveToFirst();

        Movie favorite = cursorToFavorite(cursor);
        cursor.close();
        return favorite;
    }

    public void deleteFavorite(Movie favorite) {
        database.delete(MySQLiteHelper.TABLE_FAVORITES, MySQLiteHelper.COLUMN_ID + " = " + favorite.id, null);
    }

    public ArrayList<Movie> getAllFavorites() {
        ArrayList<Movie> favorites = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_FAVORITES, allColumns, null, null, null,
                                       null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Movie favorite = cursorToFavorite(cursor);
            favorites.add(favorite);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return favorites;
    }

    // is a movie a favorite?
    public boolean favoriteExists(int id) {
        boolean exists = false; // does the favorite exist in database?
        Cursor cursor = database.query(MySQLiteHelper.TABLE_FAVORITES, allColumns, null, null, null,
                null, null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            if (cursor.getInt(0) == id) // if ID matches favorite in database
                exists = true; // favorite does exist

            cursor.moveToNext();
        }

        cursor.close();
        return exists;
    }

    private Movie cursorToFavorite(Cursor cursor) {
        return new Movie(cursor.getInt(0), cursor.getString(1), cursor.getString(2),
                         cursor.getString(3), cursor.getDouble(4), cursor.getString(5));
    }
}
