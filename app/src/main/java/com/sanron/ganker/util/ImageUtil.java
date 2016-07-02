package com.sanron.ganker.util;

import android.graphics.Bitmap;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;

import rx.Observable;
import rx.Subscriber;

/**
 * Created by sanron on 16-7-2.
 */
public class ImageUtil {

    public static Observable<String> saveBitmap(final Bitmap bitmap, final String name) {
        return Observable
                .create(new Observable.OnSubscribe<String>() {
                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                                File sdcard = Environment.getExternalStorageDirectory();
                                File meizhi = new File(sdcard, "meizhi");
                                if (!meizhi.exists()) {
                                    if (!meizhi.mkdir()) {
                                        subscriber.onNext(null);
                                        return;
                                    }
                                }
                                File save = new File(meizhi, name);
                                if (!save.exists()) {
                                    if (!save.createNewFile()) {
                                        subscriber.onNext(null);
                                        return;
                                    }
                                }
                                FileOutputStream fos = new FileOutputStream(save);
                                boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                if (result) {
                                    subscriber.onNext(save.getAbsolutePath());
                                }
                                fos.close();
                            }
                        } catch (Exception e) {
                            subscriber.onError(e);
                        }
                    }
                });

    }
}
