package com.example.rss_reader.views.activities;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;

import com.example.rss_reader.R;
import com.example.rss_reader.databases.SharedPreferencesHandler;
import com.example.rss_reader.utils.RSSToastFactory;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginBehavior;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.DeviceLoginButton;
import com.facebook.login.widget.LoginButton;
import com.google.android.material.button.MaterialButton;

import java.util.Collections;
import java.util.Objects;

public class AuthenticateActivity extends AppCompatActivity {
    private static final String EMAIL = "email";
    CallbackManager callbackManager;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Window window = getWindow();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//        window.setStatusBarColor(ContextCompat.getColor(getBaseContext(), R.color.base));

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        setContentView(R.layout.authenticate);

        callbackManager = CallbackManager.Factory.create();

        LoginButton loginButton = findViewById(R.id.login_button);
        loginButton.setReadPermissions(Collections.singletonList(EMAIL));
        MaterialButton guest = findViewById(R.id.guest_button);
        guest.setOnClickListener(v -> {
            if (SharedPreferencesHandler.getInstance(getBaseContext()).saveID("Guest-" + Settings.Secure.getString(getBaseContext().getContentResolver(),
                    Settings.Secure.ANDROID_ID))) {
                Intent intent = new Intent(AuthenticateActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            } else
                RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getBaseContext(), "Authenticate failed");
        });

        // If you are using in a fragment, call loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (SharedPreferencesHandler.getInstance(getBaseContext()).saveID(Objects.requireNonNull(AccessToken.getCurrentAccessToken()).getUserId())) {
                    Intent intent = new Intent(AuthenticateActivity.this, MainActivity.class);
                    startActivity(intent);
                    finishAffinity();
                } else
                    RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getBaseContext(), "Authenticate failed");
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(@NonNull FacebookException exception) {
                // App code
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null && !accessToken.isExpired()) {
            if (SharedPreferencesHandler.getInstance(getBaseContext()).saveID(AccessToken.getCurrentAccessToken().getUserId())) {
                Intent intent = new Intent(AuthenticateActivity.this, MainActivity.class);
                startActivity(intent);
                finishAffinity();
            } else
                RSSToastFactory.createToast(RSSToastFactory.RSSToast.Information, getBaseContext(), "Authenticate failed");
//            LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("public_profile"));
        } else if (SharedPreferencesHandler.getInstance(getBaseContext()).getId() != null) {
            Intent intent = new Intent(AuthenticateActivity.this, MainActivity.class);
            startActivity(intent);
            finishAffinity();
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }
}
