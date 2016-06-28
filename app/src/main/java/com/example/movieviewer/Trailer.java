package com.example.movieviewer;

import android.util.Log;

public class Trailer {
    protected String name, trailerURL;

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
