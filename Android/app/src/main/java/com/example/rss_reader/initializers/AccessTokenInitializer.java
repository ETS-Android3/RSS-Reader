package com.example.rss_reader.initializers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.facebook.AccessTokenManager;

import java.util.Collections;
import java.util.List;

public class AccessTokenInitializer implements Initializer<Void> {

    @NonNull
    @Override
    public Void create(@NonNull Context context) {
        AccessTokenManager.getInstance().loadCurrentAccessToken();
        return null;

    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.singletonList(FaceBookInitializer.class);
    }
}
