package com.example.rss_reader.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.example.rss_reader.R;

public class RSSToastFactory {
    public enum RSSToast {
        Information
    }

    public static Toast createToast(@NonNull RSSToast type, Context context, String message) {
        switch (type) {
            case Information:
                Toast toast = new Toast(context);
                toast.setDuration(Toast.LENGTH_SHORT);
                View layout = LayoutInflater.from(context).inflate(R.layout.rss_toast, null, false);
                TextView text = layout.findViewById(R.id.toast_text);
                text.setText(message);
                toast.setView(layout);

                return toast;
        }

        return null;
    }
}