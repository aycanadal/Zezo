package com.zezo.music.tabs.settings;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.zezo.music.MusicPlayerActivity;
import com.zezo.music.R;

/**
 * Created by 1 on 14.09.2016.
 */
public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        setPreferencesFromResource(R.xml.preferences, rootKey);

        Preference button = findPreference(getString(R.string.increaseTextSize));
        button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Log.d("Preference","Text ++");

                return true;
            }
        });

        Preference buttonDecrease = findPreference(getString(R.string.decreaseTextSize));
        buttonDecrease.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Log.d("Preference","Text --");

                return true;
            }
        });

    }
}