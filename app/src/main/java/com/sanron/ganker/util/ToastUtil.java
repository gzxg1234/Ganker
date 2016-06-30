package com.sanron.ganker.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by sanron on 16-6-28.
 */
public class ToastUtil {

    private static Context sContext;
    private static Toast mToast;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static void shortShow(String msg) {
        show(msg, Toast.LENGTH_SHORT);
    }

    public static void show(String msg, int length) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(sContext, msg, length);
        mToast.show();
    }

    public static void longShow(String msg) {
        show(msg, Toast.LENGTH_LONG);
    }
}
