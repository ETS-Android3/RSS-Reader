//package com.example.rss_reader.databases;
//
//import android.content.Context;
//import android.util.Log;
//
//import java.util.concurrent.CountDownLatch;
//
//import javax.annotation.Nullable;
//
//import io.realm.Realm;
//import io.realm.mongodb.App;
//import io.realm.mongodb.AppConfiguration;
//import io.realm.mongodb.Credentials;
//import io.realm.mongodb.User;
//import io.realm.mongodb.sync.SyncConfiguration;
//
//public class RealmClient {
//    private static User user;
//    private static volatile RealmClient instance;
//    private static final String appID = "rss-enrij";
//
//    private RealmClient() {
//    }
//
//    public static RealmClient getInstance() {
//        if (instance == null)
//            instance = new RealmClient();
//
//        return instance;
//    }
//
//    public Realm getRealm() {
//        String partitionValue = "";
//        SyncConfiguration config = new SyncConfiguration.Builder(
//                user,
//                partitionValue).allowQueriesOnUiThread(true).allowQueriesOnUiThread(true)
//                .build();
//        return Realm.getInstance(config);
//    }
//
//    public static void initializeRealm(Context context, @Nullable CountDownLatch latch) {
//        Log.i("Realm", "Connecting");
//        Realm.init(context);
//
//        App app = new App(new AppConfiguration.Builder(appID)
//                .build());
//
//        Credentials credentials = Credentials.anonymous();
//
//        app.loginAsync(credentials, result -> {
//            if (result.isSuccess()) {
//                Log.v("QUICKSTART", "Successfully authenticated anonymously.");
//                user = app.currentUser();
//                // interact with realm using your user object here
//                try {
//                    latch.countDown();
//                } catch (Exception e) {
//                    Log.e("Initialize Realm", "CountDownLatch null");
//                }
//
//            } else {
//                Log.e("QUICKSTART", "Failed to log in. Error: " + result.getError());
//            }
//        });
//    }
//}
