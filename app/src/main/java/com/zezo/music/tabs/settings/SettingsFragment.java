package com.zezo.music.tabs.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zezo.music.MusicPlayerActivity;
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

                getActivity().finish();
                Intent intent = new Intent(getContext(), MusicPlayerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                return true;
            }

        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.black));
        return view;

    }

}