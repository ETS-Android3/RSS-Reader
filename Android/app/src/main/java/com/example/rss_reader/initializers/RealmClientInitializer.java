//package com.example.rss_reader.initializers;
//
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//import androidx.startup.Initializer;
//
//import com.example.rss_reader.databases.RealmClient;
//import com.facebook.FacebookSdk;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.concurrent.CountDownLatch;
//
//public class RealmClientInitializer implements Initializer<Void> {
//
//    @Override
//    public Void create(@NonNull Context context) {
//        CountDownLatch latch = new CountDownLatch(1);
//        RealmClient.initializeRealm(context.getApplicationContext(), latch);
////        try {
////            latch.await();
////            return null;
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////            return null;
////        }
//
//        return null;
//
//    }
//
//    @Override
//    public List<Class<? extends Initializer<?>>> dependencies() {
//        return Collections.emptyList();
//    }
//}
