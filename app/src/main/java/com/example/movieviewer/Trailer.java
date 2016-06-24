package com.example.movieviewer;

import android.util.Log;

public class Trailer {
    protected String name, trailerURL;

    public Trailer(String name, String site, String id) {
        this.name = name;

        switch (site) {
            case "YouTube":
                this.trailerURL = "https://www.youtube.com/watch?v=" + id;
                break;
            case "Vimeo":
                this.trailerURL = "https://vimeo.com/" + id;
                break;
            default:
                Log.e("Trailer", "unknown trailer site");
                break;
        }
    }
}
