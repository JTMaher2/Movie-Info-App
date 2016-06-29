// Maintains one movie's information
package com.example.movieviewer;

import android.os.Parcel;
import android.os.Parcelable;

import java.text.NumberFormat;

public class Movie implements Parcelable {
    int id;
    String title, poster_url, release_date, vote_average, overview;

    private Movie(Parcel in) {
        readFromParcel(in);
    }

    // constructor
    public Movie(int id, double vote_average, String title, String poster_path, String release_date, String overview) {
        // NumberFormat to format double rating to 1 decimal place
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(1);

        this.id = id;
        this.vote_average = numberFormat.format(vote_average);
        this.title = title;
        this.poster_url = "http://image.tmdb.org/t/p/w185/" + poster_path;
        this.release_date = release_date;
        this.overview = overview;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
        out.writeString(title);
        out.writeString(poster_url);
        out.writeString(release_date);
        out.writeString(vote_average);
        out.writeString(overview);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        title = in.readString();
        poster_url = in.readString();
        release_date = in.readString();
        vote_average = in.readString();
        overview = in.readString();
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }
}
