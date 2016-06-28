package com.sanron.ganker.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by sanron on 16-6-28.
 */
public class ToastUtil {

    private static Context sContext;

    public static void init(Context context) {
        sContext = context.getApplicationContext();
    }

    public static void shortShow(String msg) {
        Toast.makeText(sContext, msg, Toast.LENGTH_SHORT).show();
    }

    public static void longShow(String msg) {
        Toast.makeText(sContext, msg, Toast.LENGTH_LONG).show();
    }
}
