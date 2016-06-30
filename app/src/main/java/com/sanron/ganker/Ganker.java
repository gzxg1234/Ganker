package com.sanron.ganker;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    public static Context getAppContext() {
        return sAppContext;
    }
}
