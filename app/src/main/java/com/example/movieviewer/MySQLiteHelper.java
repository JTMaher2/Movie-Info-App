// MySQLiteHelper.java
// provides database interaction services
package com.example.movieviewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {
    public static final String TABLE_FAVORITES = "favorites";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_POSTER_URL = "poster_url";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_RELEASE_DATE = "release_date";
    public static final String COLUMN_OVERVIEW = "overview";

    private static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation SQL statement
    private static final String DATABASE_CREATE = "create table " + TABLE_FAVORITES + "( " +
                                                  COLUMN_ID + " integer primary key, " +
                                                  COLUMN_TITLE + " text not null, " +
                                                  COLUMN_POSTER_URL + " text not null, " +
                                                  COLUMN_RATING + " text not null, " +
                                                  COLUMN_RELEASE_DATE + " text not null, " +
                                                  COLUMN_OVERVIEW + " text not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion +
                                              " to " + newVersion +
                                              ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        onCreate(db);
    }
}