package com.zezo.music;

import android.content.Context;
import android.content.SharedPreferences;

import com.zezo.music.tabs.settings.FontStyle;

/**
 * Created by 1 on 15.09.2016.
 */
public class Preferences {
    private final static String FONT_STYLE = "FONT_STYLE";

    private final Context context;

    public Preferences(Context context) {
        this.context = context;
    }

    protected SharedPreferences open() {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    }

    protected SharedPreferences.Editor edit() {
        return open().edit();
    }

    public FontStyle getFontStyle() {
        return FontStyle.valueOf(open().getString(FONT_STYLE,
                FontStyle.Medium.name()));
    }

    public void setFontStyle(FontStyle style) {
        edit().putString(FONT_STYLE, style.name()).commit();
    }

}