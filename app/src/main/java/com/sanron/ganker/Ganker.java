package com.sanron.ganker;

import android.app.Application;
import android.content.Context;

import com.sanron.ganker.util.StatusBarTool;
import com.sanron.ganker.util.ToastUtil;

/**
 * Created by sanron on 16-6-28.
 */
public class Ganker extends Application {


    private static Context sAppContext;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppContext = this;
        ToastUtil.init(this);
        StatusBarTool.init(this);
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
