package com.example.rss_reader.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.example.rss_reader.models.RSSArticle;
import com.example.rss_reader.models.RSSPost;

import java.util.ArrayList;
import java.util.UUID;

public class RSSSQLiteHandler {
    private final SQLiteClient client;
    private static volatile RSSSQLiteHandler handler;

    private RSSSQLiteHandler(Context context) {
        client = new SQLiteClient(context);
    }

    public static RSSSQLiteHandler getInstance(Context context) {
        if (handler == null)
            handler = new RSSSQLiteHandler(context);

        return handler;
    }

    public boolean saveRSS(Context context, RSSPost post) {
        String name = UUID.randomUUID().toString();
        if (InternalStorageHandler.saveHTML(context, name, post.getHtml())) {
            ContentValues record = new ContentValues();
            record.put(SQLiteClient.KEY_TITLE, post.getRSSArticle().getTitle());
            record.put(SQLiteClient.KEY_PUBDATE, post.getRSSArticle().getPubDate());
            record.put(SQLiteClient.KEY_DESCRIPTION, post.getRSSArticle().getDescription());
            record.put(SQLiteClient.KEY_LINK, post.getRSSArticle().getLink());
            record.put(SQLiteClient.KEY_GUID, post.getRSSArticle().getGuid());
            record.put(SQLiteClient.KEY_HTML, name);

            client.insert(SQLiteClient.Table.RSS, record);

            return true;
        }

        return false;
    }

    public ArrayList<RSSPost> getRSSs() {
        Cursor cursor = client.query(SQLiteClient.Table.RSS, "", new String[]{});
        ArrayList<RSSPost> posts = new ArrayList<>();
        if (cursor != null)
            cursor.moveToFirst();

        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                posts.add(new RSSPost(new RSSArticle(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5)), cursor.getString(6)));
                cursor.moveToNext();
            }
        }

        return posts;
    }

    public boolean removeRSS(Context context, RSSPost post) {
        if (InternalStorageHandler.delete(context, post.getHtml())) {
            client.delete(SQLiteClient.Table.RSS, SQLiteClient.KEY_HTML + " = ?", new String[]{post.getHtml()});

            return true;
        }

        return false;
    }
}
