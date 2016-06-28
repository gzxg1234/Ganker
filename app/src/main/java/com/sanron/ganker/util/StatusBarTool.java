package com.sanron.ganker.util;

import android.content.Context;
import android.os.Build;

/**
 * Created by sanron on 16-6-28.
 */
public class StatusBarTool {

    private static int sStatusBarHeight = 0;

    public static void init(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            return;
        }

        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            sStatusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
    }

    public static int getStatusBarHeight() {
        return sStatusBarHeight;
    }
}
