package com.github.a28hacks.driveby.usecase.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.github.a28hacks.driveby.R;

import java.util.Locale;

/**
 * Created by stefan on 03.12.16.
 */

public class UserSettings {

    private Context context;
    private SharedPreferences mSharedPreferences;

    public UserSettings(Context context) {
        this.mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
    }

    public boolean isTTSEnabled() {
        return mSharedPreferences.getBoolean(context.getString(R.string.pref_key_tts_enable), true);
    }

    public Locale getTTSLanguage() {
        return new Locale(mSharedPreferences.getString(
                        context.getString(R.string.pref_key_tss_language),
                        context.getString(R.string.pref_language_default)));
    }

    public boolean isSlowTTSEnabled() {
        return mSharedPreferences.getBoolean(
                context.getString(R.string.pref_key_tss_slower), false);
    }
}
