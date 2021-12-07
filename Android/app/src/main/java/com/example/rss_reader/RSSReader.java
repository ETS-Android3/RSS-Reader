package com.example.rss_reader;

import android.app.Application;

import com.google.firebase.FirebaseApp;

public class RSSReader extends Application {
    @Override
    public void onCreate() {
        FirebaseApp.initializeApp(this);
        super.onCreate();
    }
}
