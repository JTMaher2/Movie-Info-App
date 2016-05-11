// Maintains one movie's information
package com.example.movieviewer;

import java.text.NumberFormat;

class Movie {
    public final String title, rating, votes, popularity, description, iconURL;

    // constructor
    public Movie(String title, double rating, int votes, double popularity, String description,
                 String iconName) {
        // NumberFormat to format double rating rounded to integer
        NumberFormat numberFormat = NumberFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        this.title = title;
        this.rating = numberFormat.format(rating);
        this.votes = Integer.toString(votes);
        this.popularity = Double.toString(popularity);
        this.description = description;
        this.iconURL = "http://image.tmdb.org/t/p/w185/" + iconName;
    }
}
