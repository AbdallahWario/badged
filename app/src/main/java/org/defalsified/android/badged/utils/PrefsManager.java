package org.defalsified.android.badged.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Simple utility class to manage SharedPreferences
 */
public class PrefsManager {
    // Shared Preferences object
    private final SharedPreferences preferences;

    // Preferences file name
    private static final String PREFS_NAME = "badgestay_prefs";

    /**
     * Constructor
     *
     * @param context Application context
     */
    public PrefsManager(Context context) {
        // Initialize SharedPreferences
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save a boolean value to SharedPreferences
     *
     * @param key Key for the preference
     * @param value Boolean value to save
     */
    public void setBoolean(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * Get a boolean value from SharedPreferences
     *
     * @param key Key for the preference
     * @param defaultValue Default value to return if preference does not exist
     * @return The stored boolean value or defaultValue
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }

    /**
     * Save a string value to SharedPreferences
     *
     * @param key Key for the preference
     * @param value String value to save
     */
    public void setString(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    /**
     * Get a string value from SharedPreferences
     *
     * @param key Key for the preference
     * @param defaultValue Default value to return if preference does not exist
     * @return The stored string value or defaultValue
     */
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }
}