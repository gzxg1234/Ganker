package com.sanron.ganker.ui.base;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.sanron.ganker.R;
import com.umeng.analytics.MobclickAgent;

/**
 * Created by sanron on 16-6-29.
 */
public class BaseActivity extends AppCompatActivity {

    protected SystemBarTintManager mSystemBarTintManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        mSystemBarTintManager = new SystemBarTintManager(this);
        mSystemBarTintManager.setStatusBarTintEnabled(true);
        mSystemBarTintManager.setNavigationBarTintEnabled(true);
        mSystemBarTintManager.setTintColor(getResources().getColor(R.color.colorPrimary));
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
