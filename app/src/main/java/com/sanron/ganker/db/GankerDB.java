package com.sanron.ganker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sanron on 16-6-29.
 */
public class GankerDB extends SQLiteOpenHelper {


    public static final String DB_NAME = "ganker";
    public static final int DB_VERSION = 1;

    private static GankerDB INSTATNCE;

    public static GankerDB get(Context context) {
        if (INSTATNCE == null) {
            synchronized (GankerDB.class) {
                if (INSTATNCE == null) {
                    INSTATNCE = new GankerDB(context.getApplicationContext());
                }
            }
        }
        return INSTATNCE;
    }

    public GankerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        CollectionsTableHelper.onCreate(db);
        HistoryTableHelper.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
