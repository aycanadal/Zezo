package com.ayco.player.tabs.settings;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;

public class FontSizeListPreference extends ListPreference {

    public FontSizeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        CharSequence[] entries = new CharSequence[FontStyle.values().length];
        CharSequence[] entryValues = new CharSequence[FontStyle.values().length];
        int i = 0;
        for (FontStyle fontStyle : FontStyle.values()) {
            entries[i] = fontStyle.getTitle();
            entryValues[i] = fontStyle.name();
            i++;
        }
        setEntries(entries);
        setEntryValues(entryValues);
    }

    public FontSizeListPreference(Context context) {
        this(context, null);
    }

}