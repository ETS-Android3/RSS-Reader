package com.example.rss_reader.databases;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

public class SharedPreferencesHandler {
    private SharedPreferences sharedPreferences;
    private static SharedPreferencesHandler instance;

    private SharedPreferencesHandler(Context context) {
        sharedPreferences = context.getSharedPreferences("RSS", Context.MODE_PRIVATE);

    }

    public static SharedPreferencesHandler getInstance(Context context) {
        if (instance == null)
            instance = new SharedPreferencesHandler(context);

        return instance;
    }

    public boolean saveID(@Nullable String id) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("id", id);

        return editor.commit();
    }

    public @Nullable
    String getId() {
        return sharedPreferences.getString("id", null);
    }
}
