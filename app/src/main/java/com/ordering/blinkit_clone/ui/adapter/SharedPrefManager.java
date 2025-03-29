package com.ordering.blinkit_clone.ui.adapter;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String PREF_NAME = "MyAppPrefs";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    // Save String data
    public void saveStringData(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    // Get String data
    public String getStringData(String key) {
        return sharedPreferences.getString(key, null); // Return null if key not found
    }

    // Save Integer data
    public void saveIntData(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    // Get Integer data
    public int getIntData(String key) {
        return sharedPreferences.getInt(key, 0); // Default value 0 if key not found
    }

    // Save Boolean data
    public void saveBooleanData(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    // Get Boolean data
    public boolean getBooleanData(String key) {
        return sharedPreferences.getBoolean(key, false); // Default false if key not found
    }

    // Save JSON data
    public void saveJsonData(String key, String json) {
        editor.putString(key, json);
        editor.apply();
    }

    public void removeData(String key) {
        editor.remove(key);
        editor.apply();
    }

    // Get JSON data
    public String getJsonData(String key) {
        return sharedPreferences.getString(key, "{}"); // Default empty JSON if key not found
    }

    // Remove a specific key
    public void removeKey(String key) {
        editor.remove(key);
        editor.apply();
    }

    // Clear all data
    public void clearAll() {
        editor.clear();
        editor.apply();
    }

}
