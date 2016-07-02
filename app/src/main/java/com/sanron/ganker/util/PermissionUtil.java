package com.sanron.ganker.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

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

    public static void requestPermissions(final Activity activity, final String[] permissions, String[] rationales) {
        if (isMarshmallow()) {
            for (int i = 0; i < permissions.length; i++) {
                final String p = permissions[i];
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                    new AlertDialog.Builder(activity)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setMessage(rationales[i])
                            .setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    ActivityCompat.requestPermissions(activity, new String[]{p}, 102);
                                }
                            })
                            .show();
                } else {
                    ActivityCompat.requestPermissions(activity, new String[]{p}, 102);
                }
            }
        }
    }

    public static boolean isMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
