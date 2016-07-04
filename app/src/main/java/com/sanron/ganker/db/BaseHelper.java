package com.sanron.ganker.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.Callable;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by sanron on 16-7-4.
 */
public class BaseHelper {
    private GankerDB mGankerDB;

    public BaseHelper(Context context) {
        mGankerDB = GankerDB.get(context);
    }

    protected SQLiteDatabase getDataBase() {
        return mGankerDB.getWritableDatabase();
    }

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
