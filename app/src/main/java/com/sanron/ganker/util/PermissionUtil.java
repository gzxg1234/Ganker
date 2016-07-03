package com.sanron.ganker.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sanron on 16-7-2.
 */
public class PermissionUtil {

    public static List<String> getDeniedPermissions(Context context, String... permissions) {
        List<String> result = new ArrayList<>();
        if (isMarshmallow()) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
                    result.add(permission);
                }
            }
        }
        return result;
    }

    public static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
