package com.zezo.music.tabs.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.zezo.music.Preferences;
import com.zezo.music.R;

/**
 * Created by 1 on 14.09.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);

        /*Preference button = findPreference(getString(R.string.increaseTextSize));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Log.d("Preference","Text ++");

                return true;
            }
        });*/

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        getActivity().getTheme().applyStyle(new Preferences(getActivity()).getFontStyle().getResId(), true);

    }
}