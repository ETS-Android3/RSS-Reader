package com.example.rss_reader.utils;

import android.annotation.SuppressLint;
import android.icu.text.SimpleDateFormat;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

public class Converter {
    public static String timeDifToPresent(String date) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        Date converted = format.parse(date);
        Date currentTime = Calendar.getInstance().getTime();
        String[] posfix = {"m", "h", "d"};
        int pos = 0;
        long time = currentTime.getTime() - converted.getTime();

        time /= 60000;
        if (time / 60 >= 1) {
            time /= 60;
            pos++;
        }
        if (time / 24 >= 1) {
            time /= 24;
            pos++;
        }

        return (int) time + posfix[pos];
    }

    public static long timeToPresent(String date) throws ParseException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
        Date converted = format.parse(date);
        Date currentTime = Calendar.getInstance().getTime();

        return currentTime.getTime() - converted.getTime();
    }

    public static String urlToPath(String url) {
        return url.replace(".", "^").replace("#", "~").replace("$", "`").replace("/", "|");
    }

    public static String pathToUrl(String url) {
        return url.replace("^", ".").replace("~", "#").replace("`", "$").replace("|", "/");
    }
}
