package com.example.rss_reader.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteClient extends SQLiteOpenHelper {
    public enum Table {
        RSS(0);

        private final int value;

        Table(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private static final String DATABASE_NAME = "RSSReader";
    private static final int DATABASE_VERSION = 1;
    private static final String[] TABLE_NAME = {"RSS"};

    public static final String KEY_ID = "id";
    public static final String KEY_TITLE = "title";
    public static final String KEY_PUBDATE = "pubDate";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_LINK = "link";
    public static final String KEY_GUID = "guid";
    public static final String KEY_HTML = "html";

    public SQLiteClient(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createRSSTable = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT, %s TEXT)", TABLE_NAME[0], KEY_ID, KEY_TITLE, KEY_PUBDATE, KEY_DESCRIPTION, KEY_LINK, KEY_GUID, KEY_HTML);
        db.execSQL(createRSSTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        for (String table : TABLE_NAME) {
            String dropRSSTable = String.format("DROP TABLE IF EXISTS %s", table);
            db.execSQL(dropRSSTable);
        }

        onCreate(db);
    }

    public void insert(Table table, ContentValues record) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(TABLE_NAME[table.getValue()], null, record);
        db.close();
    }

    public Cursor query(Table table, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME[table.getValue()], null, selection, selectionArgs, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        return cursor;
    }

    public void update(Table table, String selection, String[] selectionArgs, ContentValues contentValues) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.update(TABLE_NAME[table.getValue()], contentValues, selection, selectionArgs);
        db.close();
    }

    public void delete(Table table, String selection, String[] selectionArgs) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME[table.getValue()], selection, selectionArgs);
        db.close();
    }

}
