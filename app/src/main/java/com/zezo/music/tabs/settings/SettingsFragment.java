package com.zezo.music.tabs.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.zezo.music.Preferences;
import com.zezo.music.R;

/**
 * Created by 1 on 14.09.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat{


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);


        Preference pref = findPreference(Preferences.FONT_STYLE);
        pref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference,
                                              Object newValue) {

                Preferences preferences = new Preferences(getActivity());
                preferences.setFontStyle(FontStyle.valueOf(newValue.toString()));
                getActivity().getTheme().applyStyle(new Preferences(getActivity()).getFontStyle().getResId(), true);
                return true;
            }

        });

        /*Preference button = findPreference(getString(R.string.increaseTextSize));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Log.d("Preference","Text ++");

                return true;
            }
        });*/

    }

}