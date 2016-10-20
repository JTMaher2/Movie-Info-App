package com.example.movieinfoapp;

import android.util.Log;

class Trailer {
    final String name;
    String trailerURL;

    public Trailer(String name, String site, String key) {
        this.name = name;

        switch (site) {
            case "YouTube":
                this.trailerURL = "https://www.youtube.com/watch?v=" + key;
                break;
            case "Vimeo":
                this.trailerURL = "https://vimeo.com/" + key;
                break;
            default:
                Log.e("Trailer", "unknown trailer site");
                break;
        }
    }
}
