package com.sanron.ganker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.db.entity.SaveGank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by sanron on 16-7-4.
 */
public class CollectionsTableHelper extends BaseHelper {

    public CollectionsTableHelper(Context context) {
        super(context);
    }

    public static void onCreate(SQLiteDatabase db) {
        String sql = "create table if not exists " + SaveGank.TABLE_COLLECTION + "(" +
                SaveGank.COLUMN_ID + " integer primary key," +
                SaveGank.COLUMN_ADD_TIME + " integer," +
                SaveGank.COLUMN_GANK_ID + " text," +
                SaveGank.COLUMN_WHO + " text," +
                SaveGank.COLUMN_DESC + " text," +
                SaveGank.COLUMN_PUBLISH_TIME + " integer," +
                SaveGank.COLUMN_URL + " text," +
                SaveGank.COLUMN_TYPE + " text)";
        db.execSQL(sql);
    }

    public Observable<List<SaveGank>> getAll() {
        return createObserver(new Callable<List<SaveGank>>() {
            @Override
            public List<SaveGank> call() throws Exception {
                List<SaveGank> result = new ArrayList<>();
                Cursor c = getDataBase().query(SaveGank.TABLE_COLLECTION,
                        null, null, null, null, null,
                        SaveGank.COLUMN_ADD_TIME + " desc");
                while (c.moveToNext()) {
                    result.add(toCollectionGank(c));
                }
                c.close();
                return result;
            }
        });
    }


    public Observable<Long> add(final Gank gank) {
        return createObserver(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                SQLiteDatabase db = getDataBase();
                ContentValues values = new ContentValues();
                values.put(SaveGank.COLUMN_ADD_TIME, System.currentTimeMillis());
                values.put(SaveGank.COLUMN_DESC, gank.getDesc());
                values.put(SaveGank.COLUMN_GANK_ID, gank.getGankId());
                values.put(SaveGank.COLUMN_TYPE, gank.getType());
                values.put(SaveGank.COLUMN_PUBLISH_TIME, gank.getPublishedAt().getTime());
                values.put(SaveGank.COLUMN_URL, gank.getUrl());
                values.put(SaveGank.COLUMN_WHO, gank.getWho());
                return db.insert(SaveGank.TABLE_COLLECTION, null, values);
            }
        });
    }

    public Observable<Boolean> deleteById(final long id) {
        return createObserver(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                boolean result = false;
                String selection = SaveGank.COLUMN_ID + "=?";
                int delCount = getDataBase().delete(SaveGank.TABLE_COLLECTION,
                        selection,
                        new String[]{id + ""});
                if (delCount > 0) {
                    result = true;
                }
                return result;
            }
        });
    }

    public Observable<SaveGank> deleteByGankId(final String gankid) {
        return createObserver(new Callable<SaveGank>() {
            @Override
            public SaveGank call() throws Exception {
                SaveGank result = null;
                String selection = SaveGank.COLUMN_GANK_ID + "=?";
                Cursor c = getDataBase().query(SaveGank.TABLE_COLLECTION,
                        null,
                        selection,
                        new String[]{gankid},
                        null, null,
                        SaveGank.COLUMN_ADD_TIME);
                if (c.moveToFirst()) {
                    result = toCollectionGank(c);
                }
                c.close();
                return result;
            }
        });
    }

    private static SaveGank toCollectionGank(Cursor cursor) {
        SaveGank gank = new SaveGank();
        gank.id = cursor.getLong(cursor.getColumnIndex(SaveGank.COLUMN_ID));
        gank.addTime = cursor.getLong(cursor.getColumnIndex(SaveGank.COLUMN_ADD_TIME));
        gank.setDesc(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_DESC)));
        gank.setGankId(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_GANK_ID)));
        gank.setPublishedAt(new Date(cursor.getLong(cursor.getColumnIndex(SaveGank.COLUMN_PUBLISH_TIME))));
        gank.setType(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_TYPE)));
        gank.setWho(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_WHO)));
        gank.setUrl(cursor.getString(cursor.getColumnIndex(SaveGank.COLUMN_URL)));
        return gank;
    }


}
