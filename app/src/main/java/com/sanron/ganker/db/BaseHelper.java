package com.sanron.ganker.db;

import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by sanron on 16-7-4.
 */
public abstract class BaseHelper {
    private GankerDB mGankerDB;

    public BaseHelper(GankerDB gankerDB) {
        mGankerDB = gankerDB;
    }

    protected SQLiteDatabase getDataBase() {
        return mGankerDB.getWritableDatabase();
    }

    public abstract void onCreate(SQLiteDatabase db);

    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public static <T> Observable<T> createObserver(final Callable<T> callable) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(Subscriber<? super T> subscriber) {
                try {
                    T t = callable.call();
                    subscriber.onNext(t);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

}
