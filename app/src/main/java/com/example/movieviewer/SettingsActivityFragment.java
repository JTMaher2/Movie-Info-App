package com.example.movieviewer;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by james on 6/29/16.
 */
public class SettingsActivityFragment extends PreferenceFragment {
    ListPreference displayOps;

    // creates preferences GUI from preferences.xml file in res/xml
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences); // load from XML

        displayOps = (ListPreference) findPreference("pref_displayOptions");

        // summary is currently selected display option
        displayOps.setSummary(PreferenceManager.getDefaultSharedPreferences(getContext())
                                               .getString("pref_displayOptions", null));

        // update summary on change
        displayOps.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object value) {
                if (value.toString().equals("Popular"))
                    displayOps.setSummary(displayOps.getEntries()[0]);
                else if (value.toString().equals("Top Rated"))
                    displayOps.setSummary(displayOps.getEntries()[1]);
                else if (value.toString().equals("Favorites"))
                    displayOps.setSummary(displayOps.getEntries()[2]);
                else
                    displayOps.setSummary(displayOps.getEntries()[0]);

                return true;
            }
        });
    }
}
