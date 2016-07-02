package com.sanron.ganker;

import android.app.Application;
import android.content.Context;

import com.bumptech.glide.Glide;
import com.sanron.ganker.db.GankerDB;
import com.sanron.ganker.util.ToastUtil;

/**
 * Created by sanron on 16-6-28.
 */
public class Ganker extends Application {


    private static Context sAppContext;
    private GankerDB mGankerDB;
    public GankerDB getDB() {
        return mGankerDB;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mGankerDB = new GankerDB(this);
        sAppContext = this;
        ToastUtil.init(this);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
