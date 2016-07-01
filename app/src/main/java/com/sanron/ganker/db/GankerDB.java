package com.sanron.ganker.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.sanron.ganker.data.entity.Gank;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-29.
 */
public class GankerDB extends SQLiteOpenHelper {


    public static final String DB_NAME = "ganker";
    public static final int DB_VERSION = 1;

    public GankerDB(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table " + CollectionGank.TABLE_NAME + "(" +
                CollectionGank.COLUMN_ID + " integer primary key," +
                CollectionGank.COLUMN_ADD_TIME + " integer," +
                CollectionGank.COLUMN_GANK_ID + " text," +
                CollectionGank.COLUMN_WHO + " text," +
                CollectionGank.COLUMN_DESC + " text," +
                CollectionGank.COLUMN_PUBLISH_TIME + " integer," +
                CollectionGank.COLUMN_URL + " text," +
                CollectionGank.COLUMN_TYPE + " text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }


    public Observable<List<CollectionGank>> getCollectionGanks() {
        return Observable.create(new Observable.OnSubscribe<List<CollectionGank>>() {
            @Override
            public void call(Subscriber<? super List<CollectionGank>> subscriber) {
                List<CollectionGank> collectionGanks = new ArrayList<>();
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    Cursor c = db.query(CollectionGank.TABLE_NAME, null, null, null, null, null, CollectionGank.COLUMN_ADD_TIME);
                    while (c.moveToNext()) {
                        collectionGanks.add(toCollectionGank(c));
                    }
                    subscriber.onNext(collectionGanks);
                    subscriber.onCompleted();
                    c.close();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(Schedulers.io());
    }

    public Observable<Long> addCollection(final Gank gank) {
        return Observable.create(new Observable.OnSubscribe<Long>() {
            @Override
            public void call(Subscriber<? super Long> subscriber) {
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(CollectionGank.COLUMN_ADD_TIME, System.currentTimeMillis());
                    values.put(CollectionGank.COLUMN_DESC, gank.getDesc());
                    values.put(CollectionGank.COLUMN_GANK_ID, gank.getGankId());
                    values.put(CollectionGank.COLUMN_TYPE, gank.getType());
                    values.put(CollectionGank.COLUMN_PUBLISH_TIME, gank.getPublishedAt().getTime());
                    values.put(CollectionGank.COLUMN_URL, gank.getUrl());
                    values.put(CollectionGank.COLUMN_WHO, gank.getWho());
                    long id = db.insert(CollectionGank.TABLE_NAME, null, values);
                    subscriber.onNext(id);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(Schedulers.io());
    }

    public Observable<Boolean> deleteCollectionById(final long id) {
        return Observable.create(new Observable.OnSubscribe<Boolean>() {
            @Override
            public void call(Subscriber<? super Boolean> subscriber) {
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    String selection = CollectionGank.COLUMN_ID + "=?";
                    int result = db.delete(CollectionGank.TABLE_NAME,
                            selection,
                            new String[]{id + ""});
                    if (result > 0) {
                        subscriber.onNext(true);
                    } else {
                        subscriber.onNext(false);
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(Schedulers.io());
    }

    public Observable<CollectionGank> getCollectionByGankId(final String gankid) {
        return Observable.create(new Observable.OnSubscribe<CollectionGank>() {
            @Override
            public void call(Subscriber<? super CollectionGank> subscriber) {
                try {
                    SQLiteDatabase db = getWritableDatabase();
                    String selection = CollectionGank.COLUMN_GANK_ID + "=?";
                    Cursor c = db.query(CollectionGank.TABLE_NAME,
                            null,
                            selection,
                            new String[]{gankid},
                            null, null,
                            CollectionGank.COLUMN_ADD_TIME);
                    if (c.moveToFirst()) {
                        subscriber.onNext(toCollectionGank(c));
                    } else {
                        subscriber.onNext(null);
                    }
                    subscriber.onCompleted();
                    c.close();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).observeOn(Schedulers.io());
    }

    private CollectionGank toCollectionGank(Cursor cursor) {
        CollectionGank gank = new CollectionGank();
        gank.id = cursor.getLong(cursor.getColumnIndex(CollectionGank.COLUMN_ID));
        gank.addTime = cursor.getLong(cursor.getColumnIndex(CollectionGank.COLUMN_ADD_TIME));
        gank.setDesc(cursor.getString(cursor.getColumnIndex(CollectionGank.COLUMN_DESC)));
        gank.setGankId(cursor.getString(cursor.getColumnIndex(CollectionGank.COLUMN_GANK_ID)));
        gank.setPublishedAt(new Date(cursor.getLong(cursor.getColumnIndex(CollectionGank.COLUMN_PUBLISH_TIME))));
        gank.setType(cursor.getString(cursor.getColumnIndex(CollectionGank.COLUMN_TYPE)));
        gank.setWho(cursor.getString(cursor.getColumnIndex(CollectionGank.COLUMN_WHO)));
        gank.setUrl(cursor.getString(cursor.getColumnIndex(CollectionGank.COLUMN_URL)));
        return gank;
    }
}
