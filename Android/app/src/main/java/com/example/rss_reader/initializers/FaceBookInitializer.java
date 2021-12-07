package com.example.rss_reader.initializers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.startup.Initializer;

import com.facebook.FacebookSdk;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class FaceBookInitializer implements Initializer<Void> {

    @NonNull
    @Override
    public Void create(@NonNull Context context) {
        CountDownLatch latch = new CountDownLatch(1);
        FacebookSdk.sdkInitialize(context, latch::countDown);

        try {
            latch.await();
            return null;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }

    }

    @NonNull
    @Override
    public List<Class<? extends Initializer<?>>> dependencies() {
        return Collections.emptyList();
    }
}
