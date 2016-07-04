package com.sanron.ganker;

import android.app.Application;

import com.bumptech.glide.Glide;
import com.sanron.ganker.util.ToastUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by sanron on 16-6-28.
 */
public class Ganker extends Application {


    private static Ganker sAppContext;


    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
        ToastUtil.init(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    public static Ganker get() {
        return sAppContext;
    }

    public Observable<List<String>> getSearchHistory() {
        return Observable
                .create(new Observable.OnSubscribe<List<String>>() {
                    @Override
                    public void call(Subscriber<? super List<String>> subscriber) {
                        List<String> history = new ArrayList<>();
                        File file = new File(getFilesDir(), "search_history");
                        if (file.exists()) {
                            try {
                                FileInputStream fis = new FileInputStream(file);
                                ObjectInputStream ois = new ObjectInputStream(fis);
                                history = (List<String>) ois.readObject();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (StreamCorruptedException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (ClassNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                        subscriber.onNext(history);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Observable<Boolean> saveSearchHistory(final List<String> items) {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>() {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber) {
                        File file = new File(getFilesDir(), "search_history");
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            ObjectOutputStream oos = new ObjectOutputStream(fos);
                            oos.writeObject(items);
                            oos.flush();
                            oos.close();
                            fos.close();
                            subscriber.onNext(true);
                            return;
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (StreamCorruptedException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        subscriber.onNext(false);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
