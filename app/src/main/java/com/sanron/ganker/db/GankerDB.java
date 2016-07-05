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

    private HistoryTableHelper mHistoryTableHelper;
    private CollectionsTableHelper mCollectionsTableHelper;

    public GankerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        mHistoryTableHelper = new HistoryTableHelper(this);
        mCollectionsTableHelper = new CollectionsTableHelper(this);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        mHistoryTableHelper.onCreate(db);
        mCollectionsTableHelper.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        mHistoryTableHelper.onUpgrade(db, oldVersion, newVersion);
        mCollectionsTableHelper.onUpgrade(db, oldVersion, newVersion);
    }

    public HistoryTableHelper getHistoryTableHelper() {
        return mHistoryTableHelper;
    }

    public CollectionsTableHelper getCollectionsTableHelper() {
        return mCollectionsTableHelper;
    }
}
