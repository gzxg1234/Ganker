package com.sanron.ganker.util;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by sanron on 16-6-28.
 */
public class DimenTool {

    public static int dpToPx(Context context, int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dp, context.getResources().getDisplayMetrics());
    }
}
