package com.sanron.ganker.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by sanron on 16-7-2.
 */
public class ImageUtil {

    public static String getFileName(String url) {
        try {
            String urlFile = new URL(url).getFile();
            int index = urlFile.lastIndexOf('/');
            index = (index == -1 ? 0 : index);
            return urlFile.substring(index);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return String.valueOf(url.hashCode());
    }

    public static Observable<String> saveImg(final Context context, final String imgUrl) {
        return Observable.create(new Observable.OnSubscribe<Bitmap>() {
            @Override
            public void call(final Subscriber<? super Bitmap> subscriber) {
                try {
                    Bitmap bitmap = Glide.with(context)
                            .load(imgUrl)
                            .asBitmap()
                            .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
                            .get();
                    subscriber.onNext(bitmap);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    e.printStackTrace();
                    subscriber.onError(e);
                }
            }
        }).flatMap(new Func1<Bitmap, Observable<String>>() {
            @Override
            public Observable<String> call(Bitmap bitmap) {
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    try {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File meizhi = new File(sdcard, "meizhi");
                        if (!meizhi.exists()) {
                            if (!meizhi.mkdir()) {
                                throw new Exception();
                            }
                        }
                        File save = new File(meizhi, getFileName(imgUrl));
                        if (!save.exists()) {
                            if (!save.createNewFile()) {
                                throw new Exception();
                            }
                        }
                        FileOutputStream fos = new FileOutputStream(save);
                        boolean result = bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        if (result) {
                            return Observable.just(save.getAbsolutePath());
                        }
                        fos.close();
                    } catch (Exception e) {
                        return Observable.error(e);
                    }
                }
                return Observable.error(new Exception("无外置存储"));
            }
        });
    }

}
