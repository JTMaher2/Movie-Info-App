package com.example.movieviewer;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by james on 6/25/16.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences); // load from XML
    }
}
