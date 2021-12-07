package com.example.rss_reader.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.rss_reader.R;

public class RSSDialogFactory {
    public enum RSSDialog {
        Loading
    }

    public static AlertDialog createDialog(@NonNull RSSDialog type, Context context, String message) {
        switch (type) {
            case Loading:
                AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.CustomDialog);
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View dialogView = inflater.inflate(R.layout.loading_dialog, null);
                builder.setView(dialogView);
                return builder.create();
        }

        return null;
    }
}