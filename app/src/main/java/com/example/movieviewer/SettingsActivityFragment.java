package com.example.movieviewer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by james on 6/29/16.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    // creates preferences GUI from preferences.xml file in res/xml
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences); // load from XML
    }
}
