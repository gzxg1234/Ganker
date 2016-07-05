package com.sanron.ganker.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.sanron.ganker.data.entity.Gank;
import com.sanron.ganker.db.entity.SaveGank;

import java.util.concurrent.Callable;

import rx.Observable;

/**
 * Created by sanron on 16-7-4.
 */
public class CollectionsTableHelper extends HistoryTableHelper {


    CollectionsTableHelper(GankerDB gankerDB) {
        super(gankerDB);
        setTableName(SaveGank.TABLE_COLLECTION);
    }

    @Override
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

    public Observable<SaveGank> getByGankId(final String gankid) {
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
                    result = toSaveGank(c);
                }
                c.close();
                return result;
            }
        });
    }

}
